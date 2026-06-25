package com.tarotiq.app.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_challenges")
data class ActiveChallenge(
    @PrimaryKey val challengeId: String,
    val userId: String,
    val startDate: String,
    val currentDay: Int = 1,
    val completedDaysJson: String = "[]",
    val isCompleted: Boolean = false,
    val lastActivityDate: String = ""
)
