package com.tarotiq.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tarotiq.app.domain.model.Achievement
import com.tarotiq.app.domain.model.ActiveChallenge
import com.tarotiq.app.domain.model.DailyCard
import com.tarotiq.app.domain.model.TarotCard
import com.tarotiq.app.domain.model.TarotReading

@Database(
    entities = [TarotCard::class, TarotReading::class, DailyCard::class, Achievement::class, ActiveChallenge::class],
    version = 3,
    exportSchema = false
)
abstract class TarotDatabase : RoomDatabase() {
    abstract fun tarotCardDao(): TarotCardDao
    abstract fun readingDao(): ReadingDao
    abstract fun dailyCardDao(): DailyCardDao
    abstract fun achievementDao(): AchievementDao
    abstract fun challengeDao(): ChallengeDao

    companion object {
        const val DATABASE_NAME = "tarotiq_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tarot_readings ADD COLUMN userNotes TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS active_challenges (
                        challengeId TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        startDate TEXT NOT NULL,
                        currentDay INTEGER NOT NULL DEFAULT 1,
                        completedDaysJson TEXT NOT NULL DEFAULT '[]',
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        lastActivityDate TEXT NOT NULL DEFAULT ''
                    )"""
                )
            }
        }
    }
}
