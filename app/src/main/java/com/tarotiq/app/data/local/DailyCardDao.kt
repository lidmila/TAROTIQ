package com.tarotiq.app.data.local

import androidx.room.*
import com.tarotiq.app.domain.model.DailyCard
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCardDao {
    @Query("SELECT * FROM daily_cards WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getDailyCard(userId: String, date: String): DailyCard?

    @Query("SELECT * FROM daily_cards WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllDailyCards(userId: String): Flow<List<DailyCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyCard(dailyCard: DailyCard)
}
