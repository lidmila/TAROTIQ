package com.tarotiq.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tarot_cards")
data class TarotCard(
    @PrimaryKey val id: Int,          // 0-77
    val nameKey: String,              // "the_fool", "ace_of_cups"
    val arcana: String,               // "major" | "minor"
    val suit: String?,                // null | "cups"|"pentacles"|"swords"|"wands"
    val number: Int,                  // 0-21 major, 1-14 minor
    val imageRes: String,             // drawable resource name
    val element: String?,             // "water"|"earth"|"air"|"fire"
    val uprightKeywords: String,      // JSON array
    val reversedKeywords: String,
    val loveMeaningUpright: String,
    val loveMeaningReversed: String,
    val careerMeaningUpright: String,
    val careerMeaningReversed: String,
    val financesMeaningUpright: String,
    val financesMeaningReversed: String,
    val feelingsMeaningUpright: String,
    val feelingsMeaningReversed: String,
    val actionsMeaningUpright: String,
    val actionsMeaningReversed: String
)
