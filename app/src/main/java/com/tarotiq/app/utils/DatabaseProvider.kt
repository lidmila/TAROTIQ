package com.tarotiq.app.utils

import android.content.Context
import androidx.room.Room
import com.tarotiq.app.data.local.TarotDatabase
import com.tarotiq.app.data.local.TarotDatabase.Companion.MIGRATION_1_2
import com.tarotiq.app.data.local.TarotDatabase.Companion.MIGRATION_2_3

object DatabaseProvider {
    @Volatile
    private var INSTANCE: TarotDatabase? = null

    fun getDatabase(context: Context): TarotDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                TarotDatabase::class.java,
                TarotDatabase.DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
            INSTANCE = instance
            instance
        }
    }
}
