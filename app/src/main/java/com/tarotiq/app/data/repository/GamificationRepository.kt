package com.tarotiq.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tarotiq.app.data.local.AchievementDao
import com.tarotiq.app.data.local.DailyCardDao
import com.tarotiq.app.data.preferences.SettingsManager
import com.tarotiq.app.domain.model.Achievement
import com.tarotiq.app.domain.model.DailyCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GamificationRepository(
    private val dailyCardDao: DailyCardDao,
    private val achievementDao: AchievementDao,
    private val settingsManager: SettingsManager
) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getTodaysDailyCard(): DailyCard? {
        val userId = auth.currentUser?.uid ?: return null
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return dailyCardDao.getDailyCard(userId, today)
    }

    suspend fun saveDailyCard(dailyCard: DailyCard) {
        dailyCardDao.insertDailyCard(dailyCard)
        // Sync to Firestore
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .collection("dailyCards").document(dailyCard.date)
                .set(dailyCard).await()
        } catch (_: Exception) { }
    }

    fun getAllDailyCards(): Flow<List<DailyCard>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return dailyCardDao.getAllDailyCards(userId)
    }

    fun getAchievements(): Flow<List<Achievement>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return achievementDao.getAchievements(userId)
    }

    suspend fun updateStreak() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val lastActive = settingsManager.lastActiveDateFlow.first()
        val currentStreak = settingsManager.currentStreakFlow.first()

        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

        val newStreak = when (lastActive) {
            today -> currentStreak // Already updated today
            yesterday -> currentStreak + 1 // Continue streak
            else -> 1 // Reset streak
        }

        settingsManager.setCurrentStreak(newStreak)
        settingsManager.setLastActiveDate(today)

        val longestStreak = settingsManager.longestStreakFlow.first()
        if (newStreak > longestStreak) {
            settingsManager.setLongestStreak(newStreak)
        }
    }

    suspend fun checkAndUnlockAchievements(totalReadings: Int) {
        val userId = auth.currentUser?.uid ?: return
        val achievements = listOf(
            "first_reading" to 1,
            "ten_readings" to 10,
            "fifty_readings" to 50
        )
        for ((id, target) in achievements) {
            val existing = achievementDao.getAchievement(userId, id)
            if (existing == null) {
                achievementDao.insertAchievement(
                    Achievement(id = id, userId = userId, target = target, progress = totalReadings,
                        unlockedAt = if (totalReadings >= target) System.currentTimeMillis() else null)
                )
            } else if (existing.unlockedAt == null && totalReadings >= target) {
                achievementDao.updateAchievement(existing.copy(progress = totalReadings, unlockedAt = System.currentTimeMillis()))
            } else {
                achievementDao.updateAchievement(existing.copy(progress = totalReadings))
            }
        }
    }
}
