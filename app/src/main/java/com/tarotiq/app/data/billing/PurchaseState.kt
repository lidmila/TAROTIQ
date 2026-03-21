package com.tarotiq.app.data.billing

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Processing : PurchaseState()
    data class Success(val coinsGranted: Int) : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}
