package com.tarotiq.app.utils

import android.content.Context

/**
 * Centralized card name translation using string resources.
 * Looks up card_{nameKey} in resources, falls back to formatted English name.
 */
object CardNameTranslator {

    private val majorNameKeys = listOf(
        "the_fool", "the_magician", "the_high_priestess",
        "the_empress", "the_emperor", "the_hierophant",
        "the_lovers", "the_chariot", "strength",
        "the_hermit", "wheel_of_fortune", "justice",
        "the_hanged_man", "death", "temperance",
        "the_devil", "the_tower", "the_star",
        "the_moon", "the_sun", "judgement", "the_world"
    )

    private val ranks = listOf(
        "ace", "two", "three", "four", "five", "six", "seven",
        "eight", "nine", "ten", "page", "knight", "queen", "king"
    )

    private val suits = listOf("cups", "pentacles", "swords", "wands")

    fun getNameKey(cardId: Int): String {
        return if (cardId < 22) {
            majorNameKeys[cardId]
        } else {
            val minorIndex = cardId - 22
            "${ranks[minorIndex % 14]}_of_${suits[minorIndex / 14]}"
        }
    }

    fun getDisplayName(context: Context, cardId: Int): String {
        val nameKey = getNameKey(cardId)
        val resId = context.resources.getIdentifier("card_$nameKey", "string", context.packageName)
        return if (resId != 0) {
            context.getString(resId)
        } else {
            nameKey.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }
}
