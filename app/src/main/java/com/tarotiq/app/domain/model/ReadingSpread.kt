package com.tarotiq.app.domain.model

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
            SpreadPosition("insight", "Vhled", "Insight")
        )
    ),
    THREE_CARD(
        key = "three_card",
        coinCost = 2,
        cardCount = 3,
        positions = listOf(
            SpreadPosition("past", "Minulost", "Past"),
            SpreadPosition("present", "Přítomnost", "Present"),
            SpreadPosition("future", "Budoucnost", "Future")
        )
    ),
    RELATIONSHIP(
        key = "relationship",
        coinCost = 2,
        cardCount = 5,
        positions = listOf(
            SpreadPosition("you", "Vy", "You"),
            SpreadPosition("partner", "Partner", "Partner"),
            SpreadPosition("relationship", "Vztah", "Relationship"),
            SpreadPosition("challenges", "Výzvy", "Challenges"),
            SpreadPosition("future", "Budoucnost", "Future")
        )
    ),
    CELTIC_CROSS(
        key = "celtic_cross",
        coinCost = 3,
        cardCount = 10,
        positions = listOf(
            SpreadPosition("present", "Přítomnost", "Present Situation"),
            SpreadPosition("challenge", "Výzva", "Challenge"),
            SpreadPosition("past", "Minulost", "Past"),
            SpreadPosition("future", "Budoucnost", "Future"),
            SpreadPosition("above", "Vědomí", "Conscious"),
            SpreadPosition("below", "Podvědomí", "Subconscious"),
            SpreadPosition("advice", "Rada", "Advice"),
            SpreadPosition("influences", "Vlivy", "External Influences"),
            SpreadPosition("hopes", "Naděje a obavy", "Hopes and Fears"),
            SpreadPosition("outcome", "Výsledek", "Outcome")
        )
    );

    companion object {
        fun fromKey(key: String): ReadingSpread = entries.first { it.key == key }
    }
}

data class SpreadPosition(
    val key: String,
    val nameCz: String,
    val nameEn: String
)
