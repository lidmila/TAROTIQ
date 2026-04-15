package com.tarotiq.app.domain.model

import com.tarotiq.app.R

enum class AchievementCategory {
    READING, STREAK, COLLECTION, DAILY
}

enum class AchievementDef(
    val id: String,
    val titleResId: Int,
    val descResId: Int,
    val target: Int,
    val category: AchievementCategory,
    val coinReward: Int
) {
    FIRST_READING(
        id = "first_reading",
        titleResId = R.string.achievement_first_reading,
        descResId = R.string.achievement_first_reading_desc,
        target = 1,
        category = AchievementCategory.READING,
        coinReward = 1
    ),
    TEN_READINGS(
        id = "ten_readings",
        titleResId = R.string.achievement_ten_readings,
        descResId = R.string.achievement_ten_readings_desc,
        target = 10,
        category = AchievementCategory.READING,
        coinReward = 2
    ),
    FIFTY_READINGS(
        id = "fifty_readings",
        titleResId = R.string.achievement_fifty_readings,
        descResId = R.string.achievement_fifty_readings_desc,
        target = 50,
        category = AchievementCategory.READING,
        coinReward = 3
    ),
    HUNDRED_READINGS(
        id = "hundred_readings",
        titleResId = R.string.achievement_hundred_readings,
        descResId = R.string.achievement_hundred_readings_desc,
        target = 100,
        category = AchievementCategory.READING,
        coinReward = 5
    ),
    WEEK_STREAK(
        id = "week_streak",
        titleResId = R.string.achievement_week_streak,
        descResId = R.string.achievement_week_streak_desc,
        target = 7,
        category = AchievementCategory.STREAK,
        coinReward = 2
    ),
    MONTH_STREAK(
        id = "month_streak",
        titleResId = R.string.achievement_month_streak,
        descResId = R.string.achievement_month_streak_desc,
        target = 30,
        category = AchievementCategory.STREAK,
        coinReward = 5
    ),
    ALL_SPREADS(
        id = "all_spreads",
        titleResId = R.string.achievement_all_spreads,
        descResId = R.string.achievement_all_spreads_desc,
        target = ReadingSpread.entries.size,
        category = AchievementCategory.COLLECTION,
        coinReward = 5
    ),
    MAJOR_ARCANA(
        id = "major_arcana",
        titleResId = R.string.achievement_major_arcana,
        descResId = R.string.achievement_major_arcana_desc,
        target = 22,
        category = AchievementCategory.COLLECTION,
        coinReward = 5
    ),
    FIRST_DAILY(
        id = "first_daily",
        titleResId = R.string.achievement_first_daily,
        descResId = R.string.achievement_first_daily_desc,
        target = 1,
        category = AchievementCategory.DAILY,
        coinReward = 1
    ),
    THIRTY_DAILIES(
        id = "thirty_dailies",
        titleResId = R.string.achievement_thirty_dailies,
        descResId = R.string.achievement_thirty_dailies_desc,
        target = 30,
        category = AchievementCategory.DAILY,
        coinReward = 3
    ),
    NIGHT_OWL(
        id = "night_owl",
        titleResId = R.string.achievement_night_owl,
        descResId = R.string.achievement_night_owl_desc,
        target = 1,
        category = AchievementCategory.READING,
        coinReward = 1
    ),
    EARLY_BIRD(
        id = "early_bird",
        titleResId = R.string.achievement_early_bird,
        descResId = R.string.achievement_early_bird_desc,
        target = 1,
        category = AchievementCategory.READING,
        coinReward = 1
    );

    companion object {
        fun fromId(id: String): AchievementDef? = entries.find { it.id == id }
    }
}
