package com.tarotiq.app.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tarotiq.app.data.billing.CoinBillingRepository
import com.tarotiq.app.data.billing.CoinPack
import com.tarotiq.app.data.billing.PurchaseState
import com.tarotiq.app.data.repository.CoinRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class CoinShopViewModel(application: Application) : AndroidViewModel(application) {

    private val billingRepo = CoinBillingRepository.getInstance(application)
    private val coinRepo = CoinRepository()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val coinPacks: StateFlow<List<CoinPack>> = billingRepo.coinPacks
    val purchaseState: StateFlow<PurchaseState> = billingRepo.purchaseState
    val coinBalance: StateFlow<Int> = coinRepo.coinBalance
        .map { it.balance }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    init {
        coinRepo.startListening()
    }

    fun purchaseCoinPack(activity: Activity, productId: String) {
        billingRepo.launchPurchase(activity, productId)
    }

    fun resetPurchaseState() {
        billingRepo.resetPurchaseState()
    }
}
