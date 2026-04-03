package com.tarotiq.app.data.local

import androidx.room.*
import com.tarotiq.app.domain.model.TarotReading
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Query("SELECT * FROM tarot_readings WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getReadingsByUser(userId: String, limit: Int = 100): Flow<List<TarotReading>>

    @Query("SELECT * FROM tarot_readings WHERE id = :id")
    suspend fun getReadingById(id: String): TarotReading?

    @Query("SELECT * FROM tarot_readings WHERE userId = :userId AND topic = :topic ORDER BY timestamp DESC LIMIT :limit")
    fun getReadingsByTopic(userId: String, topic: String, limit: Int = 100): Flow<List<TarotReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: TarotReading)

    @Update
    suspend fun updateReading(reading: TarotReading)

    @Delete
    suspend fun deleteReading(reading: TarotReading)

    @Query("SELECT COUNT(*) FROM tarot_readings WHERE userId = :userId")
    suspend fun getReadingCount(userId: String): Int

    @Query("DELETE FROM tarot_readings WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
}
