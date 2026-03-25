package com.tarotiq.app.data.remote

import android.content.Context
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class IntegrityTokenProvider(private val context: Context) {

    companion object {
        private const val TAG = "IntegrityTokenProvider"
        private const val CLOUD_PROJECT_NUMBER = 2788944933L
    }

    private val mutex = Mutex()
    private var integrityManager: StandardIntegrityManager? = null
    private var tokenProvider: StandardIntegrityManager.StandardIntegrityTokenProvider? = null

    suspend fun warmUp() {
        try {
            ensureProvider()
            Log.d(TAG, "Token provider warmed up")
        } catch (e: Exception) {
            Log.e(TAG, "Warm-up failed", e)
        }
    }

    suspend fun getToken(requestHash: String? = null): String {
        return try {
            ensureProvider()

            val hash = requestHash ?: System.currentTimeMillis().toString()
            val request = StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                .setRequestHash(hash)
                .build()

            val response = tokenProvider!!.request(request).await()
            response.token()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get integrity token", e)
            tokenProvider = null
            ""
        }
    }

    private suspend fun ensureProvider() {
        mutex.withLock {
            if (integrityManager == null) {
                integrityManager = IntegrityManagerFactory.createStandard(context)
            }
            if (tokenProvider == null) {
                val prepareRequest = StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                    .setCloudProjectNumber(CLOUD_PROJECT_NUMBER)
                    .build()
                tokenProvider = integrityManager!!.prepareIntegrityToken(prepareRequest).await()
            }
        }
    }
}
