package com.tarotiq.app.utils

import android.content.Context
import androidx.room.Room
import com.tarotiq.app.data.local.TarotDatabase

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
                .build()
            INSTANCE = instance
            instance
        }
    }
}
