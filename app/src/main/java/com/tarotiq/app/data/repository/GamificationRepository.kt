package com.tarotiq.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tarotiq.app.data.local.AchievementDao
import com.tarotiq.app.data.local.DailyCardDao
import com.tarotiq.app.data.local.ReadingDao
import com.tarotiq.app.data.preferences.SettingsManager
import com.tarotiq.app.domain.model.Achievement
import com.tarotiq.app.domain.model.AchievementDef
import com.tarotiq.app.domain.model.DailyCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object AchievementEvents {
    private val _unlocked = MutableSharedFlow<AchievementDef>(extraBufferCapacity = 5)
    val unlocked: SharedFlow<AchievementDef> = _unlocked.asSharedFlow()

    fun emit(def: AchievementDef) {
        _unlocked.tryEmit(def)
    }
}

data class StreakUpdateResult(
    val newStreak: Int,
    val freezeUsed: Boolean = false,
    val comebackEligible: Boolean = false,
    val milestoneReached: Int? = null // 7, 14, 30, 60, 100
)

data class AchievementContext(
    val totalReadings: Int = 0,
    val currentStreak: Int = 0,
    val distinctSpreads: Int = 0,
    val dailyCardCount: Int = 0,
    val distinctMajorArcana: Int = 0,
    val currentHour: Int = -1
)

class GamificationRepository(
    private val dailyCardDao: DailyCardDao,
    private val achievementDao: AchievementDao,
    private val settingsManager: SettingsManager,
    private val readingDao: ReadingDao? = null
) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val achievementUnlocked: SharedFlow<AchievementDef> = AchievementEvents.unlocked

    suspend fun getTodaysDailyCard(): DailyCard? {
        val userId = auth.currentUser?.uid ?: return null
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return dailyCardDao.getDailyCard(userId, today)
    }

    /**
     * Dnešní karta jako Flow – emituje znovu, jakmile ji kdokoli uloží.
     * Portál díky tomu nemusí čekat na znovuvytvoření ViewModelu.
     */
    fun observeTodaysDailyCard(): Flow<DailyCard?> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(null)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return dailyCardDao.observeDailyCard(userId, today)
    }

    suspend fun saveDailyCard(dailyCard: DailyCard) {
        dailyCardDao.insertDailyCard(dailyCard)
        // Sync to Firestore
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .collection("dailyCards").document(dailyCard.date)
                .set(dailyCard).await()
        } catch (e: Exception) {
            android.util.Log.w("Sync", "Firestore sync failed", e)
        }
    }

    fun getAllDailyCards(): Flow<List<DailyCard>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return dailyCardDao.getAllDailyCards(userId)
    }

    fun getAchievements(): Flow<List<Achievement>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return achievementDao.getAchievements(userId)
    }

    private suspend fun syncAchievementToFirestore(achievement: Achievement) {
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .collection("achievements").document(achievement.id)
                .set(achievement).await()
        } catch (e: Exception) {
            android.util.Log.w("Sync", "Achievement sync failed", e)
        }
    }

    /**
     * Pull daily cards and achievements from Firestore into Room.
     * Called after login to restore data that was cleared on logout / reinstall.
     * Restoring unlocked achievements here prevents the "you unlocked X" popup
     * from firing again, and restoring today's daily card prevents the app from
     * offering a fresh card the user already drew.
     */
    suspend fun syncFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        try {
            val cards = firestore.collection("users").document(userId)
                .collection("dailyCards").get().await()
            for (doc in cards.documents) {
                dailyCardDao.insertDailyCard(
                    DailyCard(
                        id = doc.getString("id") ?: doc.id,
                        userId = doc.getString("userId") ?: userId,
                        cardId = (doc.getLong("cardId") ?: 0L).toInt(),
                        isReversed = doc.getBoolean("isReversed") ?: false,
                        date = doc.getString("date") ?: doc.id,
                        briefInsight = doc.getString("briefInsight"),
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.w("Sync", "Daily card pull failed", e)
        }

        try {
            val achievements = firestore.collection("users").document(userId)
                .collection("achievements").get().await()
            for (doc in achievements.documents) {
                achievementDao.insertAchievement(
                    Achievement(
                        id = doc.getString("id") ?: doc.id,
                        userId = doc.getString("userId") ?: userId,
                        unlockedAt = doc.getLong("unlockedAt"),
                        progress = (doc.getLong("progress") ?: 0L).toInt(),
                        target = (doc.getLong("target") ?: 0L).toInt()
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.w("Sync", "Achievement pull failed", e)
        }
    }

    suspend fun updateStreak(): StreakUpdateResult {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val lastActive = settingsManager.lastActiveDateFlow.first()
        val currentStreak = settingsManager.currentStreakFlow.first()
        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dayBeforeYesterday = LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)

        var freezeUsed = false
        var comebackEligible = false

        val newStreak = when (lastActive) {
            today -> currentStreak // Already updated today
            yesterday -> currentStreak + 1 // Continue streak
            dayBeforeYesterday -> {
                // Missed exactly 1 day -- try streak freeze
                val freezes = settingsManager.getStreakFreezes()
                if (freezes > 0 && currentStreak > 1) {
                    settingsManager.setStreakFreezes(freezes - 1)
                    settingsManager.setLastStreakFreezeDate(today)
                    freezeUsed = true
                    currentStreak + 1 // Continue as if not missed
                } else {
                    comebackEligible = currentStreak >= 3
                    if (comebackEligible) settingsManager.setComebackEligible(true)
                    1 // Reset
                }
            }
            else -> {
                // Inactive 3+ days
                comebackEligible = lastActive != null
                if (comebackEligible) settingsManager.setComebackEligible(true)
                1 // Reset
            }
        }

        settingsManager.setCurrentStreak(newStreak)
        settingsManager.setLastActiveDate(today)

        val longestStreak = settingsManager.longestStreakFlow.first()
        if (newStreak > longestStreak) {
            settingsManager.setLongestStreak(newStreak)
        }

        // Award streak freeze at every 7-day milestone
        val milestones = listOf(7, 14, 30, 60, 100)
        val milestoneReached = milestones.firstOrNull { newStreak == it }
        if (milestoneReached != null && milestoneReached % 7 == 0) {
            val currentFreezes = settingsManager.getStreakFreezes()
            if (currentFreezes < 3) {
                settingsManager.setStreakFreezes(currentFreezes + 1)
            }
        }

        return StreakUpdateResult(
            newStreak = newStreak,
            freezeUsed = freezeUsed,
            comebackEligible = comebackEligible,
            milestoneReached = milestoneReached
        )
    }

    suspend fun buildAchievementContext(currentHour: Int = -1): AchievementContext {
        val userId = auth.currentUser?.uid ?: return AchievementContext()
        val totalReadings = readingDao?.getReadingCount(userId) ?: 0
        val currentStreak = settingsManager.currentStreakFlow.first()
        val distinctSpreads = readingDao?.getDistinctSpreadCount(userId) ?: 0
        val dailyCardCount = dailyCardDao.getDailyCardCount(userId)
        val distinctCardIds = dailyCardDao.getDistinctCardIds(userId)
        // Major Arcana cards have IDs 1-22
        val distinctMajorArcana = distinctCardIds.count { it in 1..22 }
        return AchievementContext(
            totalReadings = totalReadings,
            currentStreak = currentStreak,
            distinctSpreads = distinctSpreads,
            dailyCardCount = dailyCardCount,
            distinctMajorArcana = distinctMajorArcana,
            currentHour = currentHour
        )
    }

    suspend fun checkAndUnlockAchievements(context: AchievementContext) {
        val userId = auth.currentUser?.uid ?: return

        for (def in AchievementDef.entries) {
            val progress = getProgressForAchievement(def, context)
            val existing = achievementDao.getAchievement(userId, def.id)

            if (existing == null) {
                val unlocked = progress >= def.target
                val achievement = Achievement(
                    id = def.id,
                    userId = userId,
                    target = def.target,
                    progress = progress,
                    unlockedAt = if (unlocked) System.currentTimeMillis() else null
                )
                achievementDao.insertAchievement(achievement)
                syncAchievementToFirestore(achievement)
                if (unlocked) {
                    AchievementEvents.emit(def)
                }
            } else if (existing.unlockedAt == null && progress >= def.target) {
                val achievement = existing.copy(progress = progress, unlockedAt = System.currentTimeMillis())
                achievementDao.updateAchievement(achievement)
                syncAchievementToFirestore(achievement)
                AchievementEvents.emit(def)
            } else if (existing.progress != progress) {
                val achievement = existing.copy(progress = progress)
                achievementDao.updateAchievement(achievement)
                syncAchievementToFirestore(achievement)
            }
        }
    }

    @Suppress("DEPRECATION")
    suspend fun checkAndUnlockAchievements(totalReadings: Int) {
        val ctx = buildAchievementContext().copy(totalReadings = totalReadings)
        checkAndUnlockAchievements(ctx)
    }

    private fun getProgressForAchievement(def: AchievementDef, context: AchievementContext): Int {
        return when (def) {
            AchievementDef.FIRST_READING,
            AchievementDef.TEN_READINGS,
            AchievementDef.FIFTY_READINGS,
            AchievementDef.HUNDRED_READINGS -> context.totalReadings

            AchievementDef.WEEK_STREAK,
            AchievementDef.MONTH_STREAK -> context.currentStreak

            AchievementDef.ALL_SPREADS -> context.distinctSpreads

            AchievementDef.MAJOR_ARCANA -> context.distinctMajorArcana

            AchievementDef.FIRST_DAILY,
            AchievementDef.THIRTY_DAILIES -> context.dailyCardCount

            AchievementDef.NIGHT_OWL -> if (context.currentHour in 23..23 || context.currentHour in 0..4) 1 else 0
            AchievementDef.EARLY_BIRD -> if (context.currentHour in 4..6) 1 else 0
        }
    }
}
