package com.tarotiq.app.data.billing

data class CoinPack(
    val productId: String,
    val coins: Int,
    val formattedPrice: String = "",
    val badge: String? = null,
    val savingsPercent: Int = 0
) {
    companion object {
        val PACKS = listOf(
            CoinPack(productId = "tarotiq_coins_5", coins = 5),
            CoinPack(productId = "tarotiq_coins_15", coins = 15, badge = "most_popular", savingsPercent = 10),
            CoinPack(productId = "tarotiq_coins_50", coins = 50, badge = "best_value", savingsPercent = 12),
            CoinPack(productId = "tarotiq_coins_300", coins = 300, badge = "best_value", savingsPercent = 20)
        )

        fun getCoinsForProduct(productId: String): Int {
            return PACKS.firstOrNull { it.productId == productId }?.coins ?: 0
        }
    }
}
