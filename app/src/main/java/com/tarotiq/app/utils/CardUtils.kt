package com.tarotiq.app.utils

import com.tarotiq.app.domain.model.DrawnCard
import com.tarotiq.app.domain.model.ReadingSpread
import kotlin.random.Random

object CardUtils {

    private const val TOTAL_CARDS = 78

    fun drawCards(spread: ReadingSpread, seed: Long = System.currentTimeMillis()): List<DrawnCard> {
        val random = Random(seed)
        val allCardIds = (0 until TOTAL_CARDS).toMutableList()
        allCardIds.shuffle(random)

        return spread.positions.mapIndexed { index, position ->
            DrawnCard(
                cardId = allCardIds[index],
                isReversed = random.nextBoolean(),
                position = position.key,
                positionMeaning = position.nameEn
            )
        }
    }

    fun drawSingleCard(seed: Long = System.currentTimeMillis()): Pair<Int, Boolean> {
        val random = Random(seed)
        return Pair(random.nextInt(TOTAL_CARDS), random.nextBoolean())
    }
}
