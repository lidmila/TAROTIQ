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

    init {
        connectToBillingService()
    }

    fun connectToBillingService() {
        if (_connectionState.value == BillingConnectionState.CONNECTED) return
        _connectionState.value = BillingConnectionState.CONNECTING

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d(TAG, "Billing setup finished: ${billingResult.responseCode} - ${billingResult.debugMessage}")
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

    private var cachedProductDetails = mutableListOf<ProductDetails>()

    private suspend fun queryProducts() {
        val productList = CoinPack.PACKS.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        Log.d(TAG, "Querying ${productList.size} products: ${CoinPack.PACKS.map { it.productId }}")

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, queryResult ->
            val products = queryResult.productDetailsList
            Log.d(TAG, "Query result: code=${billingResult.responseCode}, message=${billingResult.debugMessage}, products=${products.size}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                cachedProductDetails.clear()
                cachedProductDetails.addAll(products)

                for (detail in products) {
                    val price = detail.oneTimePurchaseOfferDetails?.formattedPrice
                    Log.d(TAG, "Product: ${detail.productId}, price=$price")
                }

                _coinPacks.value = CoinPack.PACKS.map { pack ->
                    val detail = cachedProductDetails.firstOrNull { d -> d.productId == pack.productId }
                    pack.copy(
                        formattedPrice = detail?.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                    )
                }
            } else {
                Log.e(TAG, "Product query failed: ${billingResult.responseCode} - ${billingResult.debugMessage}")
            }
        }
    }

    fun launchPurchase(activity: Activity, productId: String) {
        _purchaseState.value = PurchaseState.Processing

        val productDetails = cachedProductDetails.firstOrNull { it.productId == productId }
        Log.d(TAG, "Launch purchase: $productId, found=${productDetails != null}, cached=${cachedProductDetails.size}")

        if (productDetails != null) {
            startBillingFlow(activity, productDetails)
        } else {
            Log.w(TAG, "Product not found, retrying query...")
            scope.launch {
                if (_connectionState.value != BillingConnectionState.CONNECTED) {
                    connectToBillingService()
                    delay(2000)
                }
                queryProducts()
                val retryDetails = cachedProductDetails.firstOrNull { it.productId == productId }
                if (retryDetails != null) {
                    startBillingFlow(activity, retryDetails)
                } else {
                    _purchaseState.value = PurchaseState.Error("Product not found. Please try again later.")
                }
            }
        }
    }

    private fun startBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            ))
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
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
            if (!purchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(ackParams) { billingResult ->
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "Failed to acknowledge: ${billingResult.debugMessage}")
                    }
                }
            }

            val userId = auth.currentUser?.uid ?: return
            val productId = purchase.products.firstOrNull() ?: return
            val coinsToGrant = CoinPack.getCoinsForProduct(productId)

            val ownerDoc = firestore.collection("purchase_owners")
                .document(purchase.purchaseToken)
                .get().await()

            if (ownerDoc.exists()) {
                Log.w(TAG, "Purchase already claimed")
                _purchaseState.value = PurchaseState.Success(coinsToGrant)
                return
            }

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

            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                Log.d(TAG, "Consume result: ${billingResult.responseCode}")
            }

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
