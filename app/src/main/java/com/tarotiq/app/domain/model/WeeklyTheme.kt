package com.tarotiq.app.domain.model

import com.tarotiq.app.R
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

data class WeeklyTheme(
    val titleResId: Int,
    val descResId: Int,
    val spreadKey: String
) {
    companion object {
        private val themes = listOf(
            WeeklyTheme(R.string.theme_shadow_work, R.string.theme_shadow_work_desc, "shadow_self"),
            WeeklyTheme(R.string.theme_heart_opening, R.string.theme_heart_opening_desc, "self_love"),
            WeeklyTheme(R.string.theme_career_focus, R.string.theme_career_focus_desc, "career_compass"),
            WeeklyTheme(R.string.theme_inner_child, R.string.theme_inner_child_desc, "inner_child"),
            WeeklyTheme(R.string.theme_cosmic_connection, R.string.theme_cosmic_connection_desc, "twin_flame"),
            WeeklyTheme(R.string.theme_karmic_clearing, R.string.theme_karmic_clearing_desc, "karmic"),
            WeeklyTheme(R.string.theme_lunar_wisdom, R.string.theme_lunar_wisdom_desc, "moon_cycle"),
            WeeklyTheme(R.string.theme_crossroads, R.string.theme_crossroads_desc, "crossroads"),
            WeeklyTheme(R.string.theme_energy_balance, R.string.theme_energy_balance_desc, "chakra"),
            WeeklyTheme(R.string.theme_deeper_knowledge, R.string.theme_deeper_knowledge_desc, "tree_of_life"),
            WeeklyTheme(R.string.theme_week_planning, R.string.theme_week_planning_desc, "week_ahead"),
            WeeklyTheme(R.string.theme_relationships, R.string.theme_relationships_desc, "relationship")
        )

        fun getCurrentTheme(): WeeklyTheme {
            val weekOfYear = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
            return themes[weekOfYear % themes.size]
        }
    }
}
