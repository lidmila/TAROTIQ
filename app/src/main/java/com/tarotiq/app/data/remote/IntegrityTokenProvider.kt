package com.tarotiq.app.data.remote

import android.content.Context
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import kotlinx.coroutines.tasks.await

class IntegrityTokenProvider(private val context: Context) {

    companion object {
        private const val TAG = "IntegrityTokenProvider"
        // TODO: Replace with your Google Cloud project number
        private const val CLOUD_PROJECT_NUMBER = 0L
    }

    private var integrityManager: StandardIntegrityManager? = null
    private var tokenProvider: StandardIntegrityManager.StandardIntegrityTokenProvider? = null

    suspend fun getToken(): String {
        return try {
            if (integrityManager == null) {
                integrityManager = IntegrityManagerFactory.createStandard(context)
            }

            if (tokenProvider == null) {
                val prepareRequest = StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                    .setCloudProjectNumber(CLOUD_PROJECT_NUMBER)
                    .build()
                tokenProvider = integrityManager!!.prepareIntegrityToken(prepareRequest).await()
            }

            val requestHash = System.currentTimeMillis().toString()
            val request = StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                .setRequestHash(requestHash)
                .build()

            val response = tokenProvider!!.request(request).await()
            response.token()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get integrity token", e)
            ""
        }
    }
}
