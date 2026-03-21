package com.tarotiq.app.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class CoinBillingRepository private constructor(
    private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "CoinBillingRepository"

        @Volatile
        private var INSTANCE: CoinBillingRepository? = null

        fun getInstance(context: Context): CoinBillingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CoinBillingRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private val _connectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()

    private val _coinPacks = MutableStateFlow<List<CoinPack>>(emptyList())
    val coinPacks: StateFlow<List<CoinPack>> = _coinPacks.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _coinBalance = MutableStateFlow(0)
    val coinBalance: StateFlow<Int> = _coinBalance.asStateFlow()

    init {
        connectToBillingService()
        observeCoinBalance()
    }

    private fun observeCoinBalance() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("coins").document("balance")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to coin balance", error)
                    return@addSnapshotListener
                }
                val balance = snapshot?.getLong("balance")?.toInt() ?: 0
                _coinBalance.value = balance
            }
    }

    fun connectToBillingService() {
        if (_connectionState.value == BillingConnectionState.CONNECTED) return
        _connectionState.value = BillingConnectionState.CONNECTING

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _connectionState.value = BillingConnectionState.CONNECTED
                    scope.launch { queryProducts() }
                } else {
                    _connectionState.value = BillingConnectionState.DISCONNECTED
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                _connectionState.value = BillingConnectionState.DISCONNECTED
                scope.launch {
                    delay(3000)
                    connectToBillingService()
                }
            }
        })
    }

    private suspend fun queryProducts() {
        val productIds = CoinPack.PACKS.map { it.productId }
        val productList = productIds.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val details = result.productDetailsList ?: emptyList()
            _coinPacks.value = CoinPack.PACKS.map { pack ->
                val detail = details.find { it.productId == pack.productId }
                pack.copy(
                    formattedPrice = detail?.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                )
            }
        }
    }

    fun launchPurchase(activity: Activity, productId: String) {
        val detail = billingClient.queryProductDetails(
            QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                ))
                .build()
        )
        // Use cached product details instead
        scope.launch {
            _purchaseState.value = PurchaseState.Processing

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                ))
                .build()

            val result = billingClient.queryProductDetails(params)
            val productDetails = result.productDetailsList?.firstOrNull()

            if (productDetails != null) {
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    ))
                    .build()
                billingClient.launchBillingFlow(activity, flowParams)
            } else {
                _purchaseState.value = PurchaseState.Error("Product not found")
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    scope.launch { handlePurchase(purchase) }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Idle
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        try {
            // Acknowledge the purchase
            if (!purchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                val ackResult = billingClient.acknowledgePurchase(ackParams)
                if (ackResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    _purchaseState.value = PurchaseState.Error("Failed to acknowledge purchase")
                    return
                }
            }

            // Grant coins via Firestore (in production, do this via Cloud Function)
            val userId = auth.currentUser?.uid ?: return
            val productId = purchase.products.firstOrNull() ?: return
            val coinsToGrant = CoinPack.getCoinsForProduct(productId)

            // Check if already claimed
            val ownerDoc = firestore.collection("purchase_owners")
                .document(purchase.purchaseToken)
                .get().await()

            if (ownerDoc.exists()) {
                Log.w(TAG, "Purchase already claimed")
                _purchaseState.value = PurchaseState.Success(coinsToGrant)
                return
            }

            // Grant coins atomically
            firestore.runTransaction { transaction ->
                val coinRef = firestore.collection("users").document(userId)
                    .collection("coins").document("balance")
                val coinDoc = transaction.get(coinRef)

                val currentBalance = coinDoc.getLong("balance")?.toInt() ?: 0
                val totalPurchased = coinDoc.getLong("totalPurchased")?.toInt() ?: 0

                transaction.set(coinRef, mapOf(
                    "balance" to currentBalance + coinsToGrant,
                    "totalPurchased" to totalPurchased + coinsToGrant,
                    "freeReadingsUsed" to (coinDoc.getLong("freeReadingsUsed")?.toInt() ?: 0),
                    "lastUpdated" to com.google.firebase.Timestamp.now()
                ))

                // Mark purchase as claimed
                val ownerRef = firestore.collection("purchase_owners")
                    .document(purchase.purchaseToken)
                transaction.set(ownerRef, mapOf(
                    "firebaseUid" to userId,
                    "userEmail" to (auth.currentUser?.email ?: ""),
                    "claimedAt" to com.google.firebase.Timestamp.now(),
                    "coinPackId" to productId,
                    "coinsGranted" to coinsToGrant
                ))
            }.await()

            // Consume the purchase so it can be bought again
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.consumePurchase(consumeParams)

            _purchaseState.value = PurchaseState.Success(coinsToGrant)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling purchase", e)
            _purchaseState.value = PurchaseState.Error(e.message ?: "Unknown error")
        }
    }

    fun resetPurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }
}
