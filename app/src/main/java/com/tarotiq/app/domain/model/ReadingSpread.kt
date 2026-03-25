package com.tarotiq.app.domain.model

import com.tarotiq.app.R

enum class ReadingSpread(
    val key: String,
    val coinCost: Int,
    val cardCount: Int,
    val positions: List<SpreadPosition>
) {
    SINGLE(
        key = "single",
        coinCost = 1,
        cardCount = 1,
        positions = listOf(
            SpreadPosition("insight", R.string.position_insight)
        )
    ),
    THREE_CARD(
        key = "three_card",
        coinCost = 2,
        cardCount = 3,
        positions = listOf(
            SpreadPosition("past", R.string.position_past),
            SpreadPosition("present", R.string.position_present),
            SpreadPosition("future", R.string.position_future)
        )
    ),
    RELATIONSHIP(
        key = "relationship",
        coinCost = 5,
        cardCount = 5,
        positions = listOf(
            SpreadPosition("you", R.string.position_you),
            SpreadPosition("partner", R.string.position_partner),
            SpreadPosition("relationship", R.string.position_relationship),
            SpreadPosition("challenges", R.string.position_challenges),
            SpreadPosition("future", R.string.position_future)
        )
    ),
    CELTIC_CROSS(
        key = "celtic_cross",
        coinCost = 5,
        cardCount = 10,
        positions = listOf(
            SpreadPosition("present", R.string.position_present_situation),
            SpreadPosition("challenge", R.string.position_challenge),
            SpreadPosition("past", R.string.position_past),
            SpreadPosition("future", R.string.position_future),
            SpreadPosition("above", R.string.position_conscious),
            SpreadPosition("below", R.string.position_subconscious),
            SpreadPosition("advice", R.string.position_advice),
            SpreadPosition("influences", R.string.position_influences),
            SpreadPosition("hopes", R.string.position_hopes),
            SpreadPosition("outcome", R.string.position_outcome)
        )
    );

    companion object {
        fun fromKey(key: String): ReadingSpread = entries.first { it.key == key }
    }
}

data class SpreadPosition(
    val key: String,
    val nameResId: Int
)
