package com.tarotiq.app.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * Utilities for handling localization
 */
object LocaleUtils {

    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "app_language"

    private val supportedLanguages = setOf("cs", "en", "de", "pl", "es", "it")

    private val _languageFlow = MutableStateFlow("cs")
    val languageFlow: StateFlow<String> = _languageFlow.asStateFlow()

    /**
     * Initialize the language flow from saved preferences.
     * Call this early (e.g. in Application.onCreate).
     */
    fun initialize(context: Context) {
        _languageFlow.value = getCurrentLanguage(context)
    }

    /**
     * Get current app language code
     */
    fun getCurrentLanguage(context: Context): String {
        val saved = getSavedLanguage(context)
        if (saved != null) return saved
        val locale = context.resources.configuration.locales[0]
        val systemLang = locale.language
        return if (systemLang in supportedLanguages) systemLang else "cs"
    }

    /**
     * Get saved language preference (null = system default)
     */
    fun getSavedLanguage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, null)
    }

    /**
     * Save language preference and apply it via AppCompatDelegate.
     * This automatically triggers activity recreation.
     */
    fun saveAndApplyLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).commit()
        // Update reactive flow so ViewModels pick up the change immediately
        _languageFlow.value = languageCode
        // Use AppCompatDelegate for reliable locale switching (handles API 33+ automatically)
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    /**
     * Save language preference (legacy, without applying)
     */
    fun saveLanguage(context: Context, languageCode: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (languageCode == null) {
            prefs.edit().remove(KEY_LANGUAGE).apply()
        } else {
            prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
        }
        // Update reactive flow so ViewModels pick up the change immediately
        _languageFlow.value = languageCode ?: getCurrentLanguage(context)
    }

    /**
     * Apply saved language to context (call from attachBaseContext)
     */
    fun applyLocale(context: Context): Context {
        val languageCode = getSavedLanguage(context) ?: return context
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    /**
     * Get available languages
     */
    fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            "cs" to "Čeština",
            "en" to "English",
            "de" to "Deutsch",
            "pl" to "Polski",
            "es" to "Español",
            "it" to "Italiano"
        )
    }
}
