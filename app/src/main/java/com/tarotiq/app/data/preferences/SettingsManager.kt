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

    // === Clear Settings ===

    suspend fun clearSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
