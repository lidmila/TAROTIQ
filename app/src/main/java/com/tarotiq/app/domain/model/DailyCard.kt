package com.tarotiq.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "daily_cards")
data class DailyCard(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val cardId: Int,
    val isReversed: Boolean,
    val date: String,                 // "2026-03-21"
    val briefInsight: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
