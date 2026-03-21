package com.tarotiq.app.data.billing

data class CoinPack(
    val productId: String,
    val coins: Int,
    val formattedPrice: String = "",
    val badge: String? = null
) {
    companion object {
        val PACKS = listOf(
            CoinPack(productId = "tarotiq.coins.5", coins = 5),
            CoinPack(productId = "tarotiq.coins.15", coins = 15, badge = "most_popular"),
            CoinPack(productId = "tarotiq.coins.50", coins = 50, badge = "best_value")
        )

        fun getCoinsForProduct(productId: String): Int {
            return PACKS.firstOrNull { it.productId == productId }?.coins ?: 0
        }
    }
}
