package com.tarotiq.app.domain.model

data class DrawnCard(
    val cardId: Int,
    val isReversed: Boolean,
    val position: String,             // "past"|"present"|"future"|"significator" etc.
    val positionMeaning: String,
    val cardInterpretation: String? = null
)
