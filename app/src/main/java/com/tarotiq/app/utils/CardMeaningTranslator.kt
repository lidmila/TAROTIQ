package com.tarotiq.app.utils

import android.content.Context

/**
 * Translates card meanings from English to the user's language at display time.
 * Uses string resources for translations, falling back to original English if no translation exists.
 */
object CardMeaningTranslator {

    /**
     * Translate a card meaning text based on current locale.
     * @param context Android context for accessing resources
     * @param cardId Card ID (0-77)
     * @param field Field name: "love_up", "love_rev", "career_up", "career_rev",
     *              "finances_up", "finances_rev", "feelings_up", "feelings_rev",
     *              "actions_up", "actions_rev"
     * @param englishText Original English text as fallback
     * @return Translated text or original English if no translation found
     */
    fun translate(context: Context, cardId: Int, field: String, englishText: String): String {
        val locale = LocaleUtils.getCurrentLanguage(context)
        if (locale == "en") return englishText

        // Try to find a string resource named "card_{cardId}_{field}"
        val resName = "card_${cardId}_${field}"
        val resId = context.resources.getIdentifier(resName, "string", context.packageName)
        return if (resId != 0) {
            context.getString(resId)
        } else {
            englishText // Fallback to English
        }
    }
}
