package com.tarotiq.app.data.local

import androidx.room.*
import com.tarotiq.app.domain.model.TarotReading
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Query("SELECT * FROM tarot_readings WHERE userId = :userId ORDER BY timestamp DESC")
    fun getReadingsByUser(userId: String): Flow<List<TarotReading>>

    @Query("SELECT * FROM tarot_readings WHERE id = :id")
    suspend fun getReadingById(id: String): TarotReading?

    @Query("SELECT * FROM tarot_readings WHERE userId = :userId AND topic = :topic ORDER BY timestamp DESC")
    fun getReadingsByTopic(userId: String, topic: String): Flow<List<TarotReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: TarotReading)

    @Update
    suspend fun updateReading(reading: TarotReading)

    @Delete
    suspend fun deleteReading(reading: TarotReading)

    @Query("SELECT COUNT(*) FROM tarot_readings WHERE userId = :userId")
    suspend fun getReadingCount(userId: String): Int
}
