package com.tarotiq.app.domain.model

data class CoinBalance(
    val balance: Int = 0,
    val totalPurchased: Int = 0,
    val totalSpent: Int = 0,
    val freeReadingsUsed: Int = 0
) {
    val freeReadingsRemaining: Int get() = (3 - freeReadingsUsed).coerceAtLeast(0)
    val hasFreeReadings: Boolean get() = freeReadingsUsed < 3
}
