package com.tarotiq.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,       // "first_reading", "ten_readings", etc.
    val userId: String,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val target: Int
)
