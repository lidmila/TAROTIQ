package com.tarotiq.app.data.local

import androidx.room.*
import com.tarotiq.app.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE userId = :userId")
    fun getAchievements(userId: String): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("SELECT * FROM achievements WHERE userId = :userId AND id = :achievementId")
    suspend fun getAchievement(userId: String, achievementId: String): Achievement?
}
