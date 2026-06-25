package com.tarotiq.app.data.local

import androidx.room.*
import com.tarotiq.app.domain.model.ActiveChallenge
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM active_challenges WHERE userId = :userId AND isCompleted = 0 LIMIT 1")
    fun getActiveChallenge(userId: String): Flow<ActiveChallenge?>

    @Query("SELECT * FROM active_challenges WHERE userId = :userId AND isCompleted = 0 LIMIT 1")
    suspend fun getActiveChallengeOnce(userId: String): ActiveChallenge?

    @Query("SELECT * FROM active_challenges WHERE userId = :userId ORDER BY startDate DESC")
    fun getAllChallenges(userId: String): Flow<List<ActiveChallenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: ActiveChallenge)

    @Update
    suspend fun updateChallenge(challenge: ActiveChallenge)

    @Query("DELETE FROM active_challenges WHERE userId = :userId AND challengeId = :challengeId")
    suspend fun deleteChallenge(userId: String, challengeId: String)

    @Query("SELECT COUNT(*) FROM active_challenges WHERE userId = :userId AND isCompleted = 1")
    suspend fun getCompletedCount(userId: String): Int
}
