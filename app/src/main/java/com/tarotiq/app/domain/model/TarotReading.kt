package com.tarotiq.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tarot_readings")
data class TarotReading(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val topic: String = "",           // "love"|"career"|"general"|"yes_no"|"spiritual"
    val question: String? = null,
    val spreadType: String = "",      // "single"|"three_card"|"celtic_cross"|"relationship"
    val drawnCardsJson: String = "",  // JSON array of DrawnCard
    val aiInterpretation: String = "",
    val aiSynthesis: String? = null,
    val followUpMessages: String? = null,
    val mood: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val coinsCost: Int = 0,
    val zodiacSign: String? = null
)
