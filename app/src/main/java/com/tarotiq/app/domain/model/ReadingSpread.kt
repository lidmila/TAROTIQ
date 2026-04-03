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
        coinCost = 6,
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
    ),
    YEAR_AHEAD(
        key = "year_ahead",
        coinCost = 12,
        cardCount = 12,
        positions = listOf(
            SpreadPosition("january", R.string.position_january),
            SpreadPosition("february", R.string.position_february),
            SpreadPosition("march", R.string.position_march),
            SpreadPosition("april", R.string.position_april),
            SpreadPosition("may", R.string.position_may),
            SpreadPosition("june", R.string.position_june),
            SpreadPosition("july", R.string.position_july),
            SpreadPosition("august", R.string.position_august),
            SpreadPosition("september", R.string.position_september),
            SpreadPosition("october", R.string.position_october),
            SpreadPosition("november", R.string.position_november),
            SpreadPosition("december", R.string.position_december)
        )
    ),
    SHADOW_SELF(
        key = "shadow_self",
        coinCost = 6,
        cardCount = 6,
        positions = listOf(
            SpreadPosition("mask", R.string.position_mask),
            SpreadPosition("shadow", R.string.position_shadow),
            SpreadPosition("root", R.string.position_shadow_root),
            SpreadPosition("fear", R.string.position_fear),
            SpreadPosition("gift", R.string.position_shadow_gift),
            SpreadPosition("integration", R.string.position_integration)
        )
    ),
    CROSSROADS(
        key = "crossroads",
        coinCost = 6,
        cardCount = 7,
        positions = listOf(
            SpreadPosition("current_situation", R.string.position_current_situation),
            SpreadPosition("path_a_desc", R.string.position_path_a_desc),
            SpreadPosition("path_a_outcome", R.string.position_path_a_outcome),
            SpreadPosition("path_b_desc", R.string.position_path_b_desc),
            SpreadPosition("path_b_outcome", R.string.position_path_b_outcome),
            SpreadPosition("hidden_factor", R.string.position_hidden_factor),
            SpreadPosition("advice", R.string.position_advice)
        )
    ),
    CHAKRA(
        key = "chakra",
        coinCost = 7,
        cardCount = 7,
        positions = listOf(
            SpreadPosition("root_chakra", R.string.position_root_chakra),
            SpreadPosition("sacral_chakra", R.string.position_sacral_chakra),
            SpreadPosition("solar_plexus", R.string.position_solar_plexus),
            SpreadPosition("heart_chakra", R.string.position_heart_chakra),
            SpreadPosition("throat_chakra", R.string.position_throat_chakra),
            SpreadPosition("third_eye", R.string.position_third_eye),
            SpreadPosition("crown_chakra", R.string.position_crown_chakra)
        )
    ),
    TWIN_FLAME(
        key = "twin_flame",
        coinCost = 8,
        cardCount = 8,
        positions = listOf(
            SpreadPosition("your_energy", R.string.position_your_energy),
            SpreadPosition("their_energy", R.string.position_their_energy),
            SpreadPosition("karmic_bond", R.string.position_karmic_bond),
            SpreadPosition("your_lesson", R.string.position_your_lesson),
            SpreadPosition("their_lesson", R.string.position_their_lesson),
            SpreadPosition("shared_mission", R.string.position_shared_mission),
            SpreadPosition("obstacle", R.string.position_obstacle),
            SpreadPosition("future", R.string.position_future)
        )
    ),
    MOON_CYCLE(
        key = "moon_cycle",
        coinCost = 9,
        cardCount = 8,
        positions = listOf(
            SpreadPosition("new_moon", R.string.position_new_moon),
            SpreadPosition("waxing_crescent", R.string.position_waxing_crescent),
            SpreadPosition("first_quarter", R.string.position_first_quarter),
            SpreadPosition("waxing_gibbous", R.string.position_waxing_gibbous),
            SpreadPosition("full_moon", R.string.position_full_moon),
            SpreadPosition("waning_gibbous", R.string.position_waning_gibbous),
            SpreadPosition("last_quarter", R.string.position_last_quarter),
            SpreadPosition("dark_moon", R.string.position_dark_moon)
        )
    ),
    CAREER_COMPASS(
        key = "career_compass",
        coinCost = 6,
        cardCount = 6,
        positions = listOf(
            SpreadPosition("hidden_talents", R.string.position_hidden_talents),
            SpreadPosition("current_block", R.string.position_current_block),
            SpreadPosition("opportunity", R.string.position_opportunity),
            SpreadPosition("skill_to_develop", R.string.position_skill_to_develop),
            SpreadPosition("financial_energy", R.string.position_financial_energy),
            SpreadPosition("next_step", R.string.position_next_step)
        )
    ),
    INNER_CHILD(
        key = "inner_child",
        coinCost = 5,
        cardCount = 5,
        positions = listOf(
            SpreadPosition("inner_child", R.string.position_inner_child),
            SpreadPosition("wound", R.string.position_wound),
            SpreadPosition("protective_pattern", R.string.position_protective_pattern),
            SpreadPosition("healing", R.string.position_healing),
            SpreadPosition("gift", R.string.position_inner_gift)
        )
    ),
    TREE_OF_LIFE(
        key = "tree_of_life",
        coinCost = 12,
        cardCount = 10,
        positions = listOf(
            SpreadPosition("keter", R.string.position_keter),
            SpreadPosition("chokma", R.string.position_chokma),
            SpreadPosition("bina", R.string.position_bina),
            SpreadPosition("chesed", R.string.position_chesed),
            SpreadPosition("gevura", R.string.position_gevura),
            SpreadPosition("tiferet", R.string.position_tiferet),
            SpreadPosition("netzach", R.string.position_netzach),
            SpreadPosition("hod", R.string.position_hod),
            SpreadPosition("yesod", R.string.position_yesod),
            SpreadPosition("malkhut", R.string.position_malkhut)
        )
    ),
    WEEK_AHEAD(
        key = "week_ahead",
        coinCost = 5,
        cardCount = 7,
        positions = listOf(
            SpreadPosition("monday", R.string.position_monday),
            SpreadPosition("tuesday", R.string.position_tuesday),
            SpreadPosition("wednesday", R.string.position_wednesday),
            SpreadPosition("thursday", R.string.position_thursday),
            SpreadPosition("friday", R.string.position_friday),
            SpreadPosition("saturday", R.string.position_saturday),
            SpreadPosition("sunday", R.string.position_sunday)
        )
    ),
    KARMIC(
        key = "karmic",
        coinCost = 8,
        cardCount = 7,
        positions = listOf(
            SpreadPosition("karmic_debt", R.string.position_karmic_debt),
            SpreadPosition("karmic_gift", R.string.position_karmic_gift),
            SpreadPosition("current_lesson", R.string.position_current_lesson),
            SpreadPosition("repeating_pattern", R.string.position_repeating_pattern),
            SpreadPosition("what_to_break", R.string.position_what_to_break),
            SpreadPosition("karmic_reward", R.string.position_karmic_reward),
            SpreadPosition("life_purpose", R.string.position_life_purpose)
        )
    ),
    SELF_LOVE(
        key = "self_love",
        coinCost = 6,
        cardCount = 6,
        positions = listOf(
            SpreadPosition("how_i_see_myself", R.string.position_how_i_see_myself),
            SpreadPosition("how_others_see_me", R.string.position_how_others_see_me),
            SpreadPosition("false_guilt", R.string.position_false_guilt),
            SpreadPosition("hidden_strength", R.string.position_hidden_strength),
            SpreadPosition("what_i_need", R.string.position_what_i_need),
            SpreadPosition("affirmation", R.string.position_affirmation)
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
