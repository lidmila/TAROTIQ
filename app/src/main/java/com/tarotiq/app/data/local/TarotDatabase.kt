package com.tarotiq.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tarotiq.app.domain.model.Achievement
import com.tarotiq.app.domain.model.DailyCard
import com.tarotiq.app.domain.model.TarotCard
import com.tarotiq.app.domain.model.TarotReading

@Database(
    entities = [TarotCard::class, TarotReading::class, DailyCard::class, Achievement::class],
    version = 1,
    exportSchema = false
)
abstract class TarotDatabase : RoomDatabase() {
    abstract fun tarotCardDao(): TarotCardDao
    abstract fun readingDao(): ReadingDao
    abstract fun dailyCardDao(): DailyCardDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        const val DATABASE_NAME = "tarotiq_database"
    }
}
