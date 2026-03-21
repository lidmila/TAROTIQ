package com.tarotiq.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tarotiq.app.domain.model.CoinBalance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class CoinRepository {

    companion object {
        private const val TAG = "CoinRepository"
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _coinBalance = MutableStateFlow(CoinBalance())
    val coinBalance: StateFlow<CoinBalance> = _coinBalance.asStateFlow()

    fun startListening() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .collection("coins").document("balance")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to coins", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    _coinBalance.value = CoinBalance(
                        balance = snapshot.getLong("balance")?.toInt() ?: 0,
                        totalPurchased = snapshot.getLong("totalPurchased")?.toInt() ?: 0,
                        totalSpent = snapshot.getLong("totalSpent")?.toInt() ?: 0,
                        freeReadingsUsed = snapshot.getLong("freeReadingsUsed")?.toInt() ?: 0
                    )
                }
            }
    }

    suspend fun initializeCoinDocument() {
        val userId = auth.currentUser?.uid ?: return
        val coinRef = firestore.collection("users").document(userId)
            .collection("coins").document("balance")
        val doc = coinRef.get().await()
        if (!doc.exists()) {
            coinRef.set(mapOf(
                "balance" to 0,
                "totalPurchased" to 0,
                "totalSpent" to 0,
                "freeReadingsUsed" to 0
            )).await()
        }
    }
}
