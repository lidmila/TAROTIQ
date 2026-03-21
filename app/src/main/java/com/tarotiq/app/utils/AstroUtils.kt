package com.tarotiq.app.utils

import java.util.Calendar
import kotlin.math.*

object AstroUtils {

    enum class MoonPhase(val emoji: String) {
        NEW_MOON("\uD83C\uDF11"),
        WAXING_CRESCENT("\uD83C\uDF12"),
        FIRST_QUARTER("\uD83C\uDF13"),
        WAXING_GIBBOUS("\uD83C\uDF14"),
        FULL_MOON("\uD83C\uDF15"),
        WANING_GIBBOUS("\uD83C\uDF16"),
        LAST_QUARTER("\uD83C\uDF17"),
        WANING_CRESCENT("\uD83C\uDF18")
    }

    fun getCurrentMoonPhase(): MoonPhase {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val age = calculateMoonAge(year, month, day)

        return when {
            age < 1.85 -> MoonPhase.NEW_MOON
            age < 7.38 -> MoonPhase.WAXING_CRESCENT
            age < 9.23 -> MoonPhase.FIRST_QUARTER
            age < 14.77 -> MoonPhase.WAXING_GIBBOUS
            age < 16.61 -> MoonPhase.FULL_MOON
            age < 22.15 -> MoonPhase.WANING_GIBBOUS
            age < 23.99 -> MoonPhase.LAST_QUARTER
            age < 29.53 -> MoonPhase.WANING_CRESCENT
            else -> MoonPhase.NEW_MOON
        }
    }

    private fun calculateMoonAge(year: Int, month: Int, day: Int): Double {
        var y = year.toDouble()
        var m = month.toDouble()
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100)
        val b = 2 - a + floor(a / 4)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
        val daysSinceNew = jd - 2451549.5
        val newMoons = daysSinceNew / 29.53058867
        val age = (newMoons - floor(newMoons)) * 29.53058867
        return age
    }
}
