package com.tarotiq.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class Settings(
    val appLanguage: String? = null,
    val zodiacSign: String? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val cachedCoinBalance: Int = 0
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_HAS_RATED_APP = booleanPreferencesKey("has_rated_app")
        private val KEY_APP_OPEN_COUNT = intPreferencesKey("app_open_count")
        private val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val KEY_NOTIFICATION_TIME = stringPreferencesKey("notification_time")
        private val KEY_HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        private val KEY_ZODIAC_SIGN = stringPreferencesKey("zodiac_sign")
        private val KEY_CURRENT_STREAK = intPreferencesKey("current_streak")
        private val KEY_LONGEST_STREAK = intPreferencesKey("longest_streak")
        private val KEY_LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        private val KEY_CACHED_COIN_BALANCE = intPreferencesKey("cached_coin_balance")
        private val KEY_REWARDED_ADS_THIS_WEEK = intPreferencesKey("rewarded_ads_this_week")
        private val KEY_REWARDED_ADS_WEEK_START = stringPreferencesKey("rewarded_ads_week_start")
        private val KEY_SEEN_WHATS_NEW_VERSION = stringPreferencesKey("seen_whats_new_version")
        private val KEY_EVENING_NOTIFICATION_ENABLED = booleanPreferencesKey("evening_notification_enabled")
        private val KEY_STREAK_FREEZES = intPreferencesKey("streak_freezes_available")
        private val KEY_LAST_STREAK_FREEZE_DATE = stringPreferencesKey("last_streak_freeze_date")
        private val KEY_COMEBACK_ELIGIBLE = booleanPreferencesKey("comeback_bonus_eligible")
        private val KEY_READINGS_COMPLETED = intPreferencesKey("readings_completed_count")
        private val KEY_WELCOME_OFFER_SHOWN = booleanPreferencesKey("welcome_offer_shown")
        private val KEY_FIRST_READING_DATE = stringPreferencesKey("first_reading_date")
    }

    // === Settings Flow ===

    val settingsFlow: Flow<Settings> = context.dataStore.data
        .map { preferences ->
            Settings(
                appLanguage = preferences[KEY_APP_LANGUAGE],
                zodiacSign = preferences[KEY_ZODIAC_SIGN],
                currentStreak = preferences[KEY_CURRENT_STREAK] ?: 0,
                longestStreak = preferences[KEY_LONGEST_STREAK] ?: 0,
                cachedCoinBalance = preferences[KEY_CACHED_COIN_BALANCE] ?: 0
            )
        }

    // === App Language ===

    suspend fun saveAppLanguage(language: String?) {
        context.dataStore.edit { preferences ->
            if (language == null) {
                preferences.remove(KEY_APP_LANGUAGE)
            } else {
                preferences[KEY_APP_LANGUAGE] = language
            }
        }
    }

    suspend fun getAppLanguage(): String? {
        return context.dataStore.data
            .map { it[KEY_APP_LANGUAGE] }
            .first()
    }

    // === Rate App ===

    val hasRatedAppFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_HAS_RATED_APP] ?: false
        }

    suspend fun setHasRatedApp(rated: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_RATED_APP] = rated
        }
    }

    // === App Open Count ===

    val appOpenCountFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_APP_OPEN_COUNT] ?: 0
        }

    suspend fun incrementAppOpenCount(): Int {
        var newCount = 0
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_APP_OPEN_COUNT] ?: 0
            newCount = current + 1
            preferences[KEY_APP_OPEN_COUNT] = newCount
        }
        return newCount
    }

    // === Notification Settings ===

    val notificationEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_ENABLED] ?: true
        }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_ENABLED] = enabled
        }
    }

    val notificationTimeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_NOTIFICATION_TIME] ?: "07:00"
        }

    suspend fun setNotificationTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_TIME] = time
        }
    }

    // === Onboarding ===

    val hasSeenOnboardingFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_HAS_SEEN_ONBOARDING] ?: false
        }

    suspend fun setHasSeenOnboarding(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAS_SEEN_ONBOARDING] = seen
        }
    }

    // === Zodiac Sign ===

    val zodiacSignFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_ZODIAC_SIGN]
        }

    suspend fun setZodiacSign(sign: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ZODIAC_SIGN] = sign
        }
    }

    suspend fun getZodiacSign(): String? {
        return context.dataStore.data
            .map { it[KEY_ZODIAC_SIGN] }
            .first()
    }

    // === Streak ===

    val currentStreakFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_CURRENT_STREAK] ?: 0
        }

    val longestStreakFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_LONGEST_STREAK] ?: 0
        }

    val lastActiveDateFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_LAST_ACTIVE_DATE]
        }

    suspend fun setCurrentStreak(streak: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENT_STREAK] = streak
        }
    }

    suspend fun setLongestStreak(streak: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LONGEST_STREAK] = streak
        }
    }

    suspend fun setLastActiveDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_ACTIVE_DATE] = date
        }
    }

    suspend fun getLastActiveDate(): String? {
        return context.dataStore.data
            .map { it[KEY_LAST_ACTIVE_DATE] }
            .first()
    }

    // === Cached Coin Balance ===

    val cachedCoinBalanceFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_CACHED_COIN_BALANCE] ?: 0
        }

    suspend fun setCachedCoinBalance(balance: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CACHED_COIN_BALANCE] = balance
        }
    }

    suspend fun getCachedCoinBalance(): Int {
        return context.dataStore.data
            .map { it[KEY_CACHED_COIN_BALANCE] ?: 0 }
            .first()
    }

    // === Rewarded Ads ===

    val rewardedAdsThisWeekFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            val weekStart = preferences[KEY_REWARDED_ADS_WEEK_START] ?: ""
            val currentWeekStart = getCurrentWeekStart()
            if (weekStart == currentWeekStart) {
                preferences[KEY_REWARDED_ADS_THIS_WEEK] ?: 0
            } else {
                0 // New week, reset counter
            }
        }

    suspend fun incrementRewardedAdsCount(): Int {
        var newCount = 0
        context.dataStore.edit { preferences ->
            val currentWeekStart = getCurrentWeekStart()
            val savedWeekStart = preferences[KEY_REWARDED_ADS_WEEK_START] ?: ""

            if (savedWeekStart != currentWeekStart) {
                // New week - reset
                preferences[KEY_REWARDED_ADS_WEEK_START] = currentWeekStart
                preferences[KEY_REWARDED_ADS_THIS_WEEK] = 1
                newCount = 1
            } else {
                val current = preferences[KEY_REWARDED_ADS_THIS_WEEK] ?: 0
                newCount = current + 1
                preferences[KEY_REWARDED_ADS_THIS_WEEK] = newCount
            }
        }
        return newCount
    }

    suspend fun getRewardedAdsThisWeek(): Int {
        return context.dataStore.data
            .map { preferences ->
                val weekStart = preferences[KEY_REWARDED_ADS_WEEK_START] ?: ""
                if (weekStart == getCurrentWeekStart()) {
                    preferences[KEY_REWARDED_ADS_THIS_WEEK] ?: 0
                } else 0
            }
            .first()
    }

    private fun getCurrentWeekStart(): String {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        return "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.WEEK_OF_YEAR)}"
    }

    // === What's New ===

    suspend fun getSeenWhatsNewVersion(): String? {
        return context.dataStore.data
            .map { it[KEY_SEEN_WHATS_NEW_VERSION] }
            .first()
    }

    suspend fun setSeenWhatsNewVersion(version: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SEEN_WHATS_NEW_VERSION] = version
        }
    }

    // === Evening Notification ===

    val eveningNotificationEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_EVENING_NOTIFICATION_ENABLED] ?: false
        }

    suspend fun setEveningNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_EVENING_NOTIFICATION_ENABLED] = enabled
        }
    }

    // === Streak Freeze ===

    val streakFreezesFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_STREAK_FREEZES] ?: 0
        }

    suspend fun getStreakFreezes(): Int {
        return context.dataStore.data
            .map { it[KEY_STREAK_FREEZES] ?: 0 }
            .first()
    }

    suspend fun setStreakFreezes(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_STREAK_FREEZES] = count
        }
    }

    suspend fun getLastStreakFreezeDate(): String? {
        return context.dataStore.data
            .map { it[KEY_LAST_STREAK_FREEZE_DATE] }
            .first()
    }

    suspend fun setLastStreakFreezeDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_STREAK_FREEZE_DATE] = date
        }
    }

    // === Comeback Bonus ===

    val comebackEligibleFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_COMEBACK_ELIGIBLE] ?: false
        }

    suspend fun setComebackEligible(eligible: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_COMEBACK_ELIGIBLE] = eligible
        }
    }

    // === Readings Completed ===

    val readingsCompletedFlow: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_READINGS_COMPLETED] ?: 0 }

    suspend fun incrementReadingsCompleted(): Int {
        var newCount = 0
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_READINGS_COMPLETED] ?: 0
            newCount = current + 1
            preferences[KEY_READINGS_COMPLETED] = newCount
        }
        return newCount
    }

    // === Welcome Offer ===

    val welcomeOfferShownFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_WELCOME_OFFER_SHOWN] ?: false }

    suspend fun setWelcomeOfferShown() {
        context.dataStore.edit { preferences ->
            preferences[KEY_WELCOME_OFFER_SHOWN] = true
        }
    }

    suspend fun isWelcomeOfferShown(): Boolean {
        return context.dataStore.data.map { it[KEY_WELCOME_OFFER_SHOWN] ?: false }.first()
    }

    suspend fun setFirstReadingDate(date: String) {
        context.dataStore.edit { preferences ->
            if (preferences[KEY_FIRST_READING_DATE] == null) {
                preferences[KEY_FIRST_READING_DATE] = date
            }
        }
    }

    suspend fun getFirstReadingDate(): String? {
        return context.dataStore.data.map { it[KEY_FIRST_READING_DATE] }.first()
    }

    // === Cloud Sync (account-level backup) ===

    /**
     * Snapshot of all account-level settings as a Firestore-friendly map.
     * Only keys that are actually set are included, so a merge write never
     * clobbers cloud values with local defaults. Device-only state
     * (app open count, language, cached coin balance) is intentionally excluded.
     */
    val cloudSnapshotFlow: Flow<Map<String, Any>> = context.dataStore.data
        .map { prefs ->
            buildMap {
                prefs[KEY_ZODIAC_SIGN]?.let { put("zodiacSign", it) }
                prefs[KEY_NOTIFICATION_ENABLED]?.let { put("notificationEnabled", it) }
                prefs[KEY_NOTIFICATION_TIME]?.let { put("notificationTime", it) }
                prefs[KEY_EVENING_NOTIFICATION_ENABLED]?.let { put("eveningNotificationEnabled", it) }
                prefs[KEY_CURRENT_STREAK]?.let { put("currentStreak", it) }
                prefs[KEY_LONGEST_STREAK]?.let { put("longestStreak", it) }
                prefs[KEY_LAST_ACTIVE_DATE]?.let { put("lastActiveDate", it) }
                prefs[KEY_STREAK_FREEZES]?.let { put("streakFreezes", it) }
                prefs[KEY_LAST_STREAK_FREEZE_DATE]?.let { put("lastStreakFreezeDate", it) }
                prefs[KEY_COMEBACK_ELIGIBLE]?.let { put("comebackEligible", it) }
                prefs[KEY_READINGS_COMPLETED]?.let { put("readingsCompleted", it) }
                prefs[KEY_WELCOME_OFFER_SHOWN]?.let { put("welcomeOfferShown", it) }
                prefs[KEY_FIRST_READING_DATE]?.let { put("firstReadingDate", it) }
                prefs[KEY_REWARDED_ADS_THIS_WEEK]?.let { put("rewardedAdsThisWeek", it) }
                prefs[KEY_REWARDED_ADS_WEEK_START]?.let { put("rewardedAdsWeekStart", it) }
                prefs[KEY_HAS_RATED_APP]?.let { put("hasRatedApp", it) }
                prefs[KEY_SEEN_WHATS_NEW_VERSION]?.let { put("seenWhatsNewVersion", it) }
                prefs[KEY_HAS_SEEN_ONBOARDING]?.let { put("hasSeenOnboarding", it) }
            }
        }

    /**
     * Apply a settings snapshot fetched from Firestore into local DataStore.
     * Only keys present in [data] are written, so missing cloud keys leave
     * local values untouched. Cloud wins for the keys it does contain.
     */
    suspend fun applyCloudSnapshot(data: Map<String, Any?>) {
        context.dataStore.edit { prefs ->
            (data["zodiacSign"] as? String)?.let { prefs[KEY_ZODIAC_SIGN] = it }
            (data["notificationEnabled"] as? Boolean)?.let { prefs[KEY_NOTIFICATION_ENABLED] = it }
            (data["notificationTime"] as? String)?.let { prefs[KEY_NOTIFICATION_TIME] = it }
            (data["eveningNotificationEnabled"] as? Boolean)?.let { prefs[KEY_EVENING_NOTIFICATION_ENABLED] = it }
            (data["currentStreak"] as? Number)?.let { prefs[KEY_CURRENT_STREAK] = it.toInt() }
            (data["longestStreak"] as? Number)?.let { prefs[KEY_LONGEST_STREAK] = it.toInt() }
            (data["lastActiveDate"] as? String)?.let { prefs[KEY_LAST_ACTIVE_DATE] = it }
            (data["streakFreezes"] as? Number)?.let { prefs[KEY_STREAK_FREEZES] = it.toInt() }
            (data["lastStreakFreezeDate"] as? String)?.let { prefs[KEY_LAST_STREAK_FREEZE_DATE] = it }
            (data["comebackEligible"] as? Boolean)?.let { prefs[KEY_COMEBACK_ELIGIBLE] = it }
            (data["readingsCompleted"] as? Number)?.let { prefs[KEY_READINGS_COMPLETED] = it.toInt() }
            (data["welcomeOfferShown"] as? Boolean)?.let { prefs[KEY_WELCOME_OFFER_SHOWN] = it }
            (data["firstReadingDate"] as? String)?.let { prefs[KEY_FIRST_READING_DATE] = it }
            (data["rewardedAdsThisWeek"] as? Number)?.let { prefs[KEY_REWARDED_ADS_THIS_WEEK] = it.toInt() }
            (data["rewardedAdsWeekStart"] as? String)?.let { prefs[KEY_REWARDED_ADS_WEEK_START] = it }
            (data["hasRatedApp"] as? Boolean)?.let { prefs[KEY_HAS_RATED_APP] = it }
            (data["seenWhatsNewVersion"] as? String)?.let { prefs[KEY_SEEN_WHATS_NEW_VERSION] = it }
            (data["hasSeenOnboarding"] as? Boolean)?.let { prefs[KEY_HAS_SEEN_ONBOARDING] = it }
        }
    }

    // === Clear Settings ===

    suspend fun clearSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
