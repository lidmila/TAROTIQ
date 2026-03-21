package com.tarotiq.app.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tarotiq.app.data.billing.CoinBillingRepository
import com.tarotiq.app.data.billing.CoinPack
import com.tarotiq.app.data.billing.PurchaseState
import kotlinx.coroutines.flow.StateFlow

class CoinShopViewModel(application: Application) : AndroidViewModel(application) {

    private val billingRepo = CoinBillingRepository.getInstance(application)

    val coinPacks: StateFlow<List<CoinPack>> = billingRepo.coinPacks
    val purchaseState: StateFlow<PurchaseState> = billingRepo.purchaseState
    val coinBalance: StateFlow<Int> = billingRepo.coinBalance

    fun purchaseCoinPack(activity: Activity, productId: String) {
        billingRepo.launchPurchase(activity, productId)
    }

    fun resetPurchaseState() {
        billingRepo.resetPurchaseState()
    }
}
