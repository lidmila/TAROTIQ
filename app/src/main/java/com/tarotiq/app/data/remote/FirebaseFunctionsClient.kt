package com.tarotiq.app.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.gson.Gson
import com.tarotiq.app.domain.model.ChatMessage
import com.tarotiq.app.domain.model.DrawnCard
import kotlinx.coroutines.tasks.await
import java.io.IOException

class FirebaseFunctionsClient(private val context: Context) {

    companion object {
        private const val TAG = "FirebaseFunctions"
    }

    private val functions = FirebaseFunctions.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()
    private val integrityProvider = IntegrityTokenProvider(context)

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Try to refresh the Firebase ID token. If it fails, proceed anyway —
     * the callable function will use whatever token is cached.
     * This prevents blocking on slow/restricted networks (hotel wifi, etc.).
     */
    private suspend fun ensureFreshToken() {
        val user = auth.currentUser ?: throw IllegalStateException("User not logged in")
        try {
            user.getIdToken(true).await()
            Log.d(TAG, "ID token refreshed for ${user.uid}")
        } catch (e: Exception) {
            Log.w(TAG, "Token refresh failed, proceeding with cached token", e)
            // Don't throw — let the function call itself determine if auth is valid
        }
    }

    /**
     * Wraps exceptions with user-friendly messages.
     */
    private fun wrapException(e: Exception): Exception {
        if (!isNetworkAvailable()) {
            return IOException("No internet connection")
        }
        if (e is FirebaseFunctionsException) {
            return when (e.code) {
                FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                    IOException("Authentication error. Try logging out and back in.")
                FirebaseFunctionsException.Code.UNAVAILABLE ->
                    IOException("Server temporarily unavailable. Please try again.")
                FirebaseFunctionsException.Code.DEADLINE_EXCEEDED ->
                    IOException("Request timed out. Check your connection and try again.")
                else -> e
            }
        }
        return e
    }

    suspend fun interpretTarotReading(
        topic: String,
        question: String?,
        spreadType: String,
        drawnCards: List<DrawnCard>,
        zodiacSign: String?,
        moonPhase: String?,
        language: String,
        conversationHistory: List<ChatMessage> = emptyList()
    ): String {
        ensureFreshToken()
        val integrityToken = integrityProvider.getToken()

        val data = hashMapOf(
            "topic" to topic,
            "question" to (question ?: ""),
            "spreadType" to spreadType,
            "drawnCards" to drawnCards.map { card ->
                hashMapOf(
                    "cardId" to card.cardId,
                    "isReversed" to card.isReversed,
                    "position" to card.position,
                    "positionMeaning" to card.positionMeaning
                )
            },
            "zodiacSign" to (zodiacSign ?: ""),
            "moonPhase" to (moonPhase ?: ""),
            "language" to language,
            "integrityToken" to integrityToken,
            "conversationHistory" to conversationHistory.map { msg ->
                hashMapOf("role" to msg.role.name.lowercase(), "content" to msg.content)
            }
        )

        Log.d(TAG, "Auth user: ${auth.currentUser?.uid}, email: ${auth.currentUser?.email}")

        return try {
            val result = functions.getHttpsCallable("interpretTarotReading")
                .withTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
                .call(data)
                .await()
            val response = result.data as? Map<*, *>
            response?.get("interpretation") as? String ?: "No interpretation received"
        } catch (e: Exception) {
            // Retry once on UNAUTHENTICATED — could be stale token or cold start race
            if (e is FirebaseFunctionsException && e.code == FirebaseFunctionsException.Code.UNAUTHENTICATED) {
                Log.w(TAG, "UNAUTHENTICATED — refreshing token and retrying...")
                try {
                    auth.currentUser?.getIdToken(true)?.await()
                    val retryResult = functions.getHttpsCallable("interpretTarotReading")
                        .withTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
                        .call(data)
                        .await()
                    val retryResponse = retryResult.data as? Map<*, *>
                    return retryResponse?.get("interpretation") as? String ?: "No interpretation received"
                } catch (retryError: Exception) {
                    Log.e(TAG, "Retry also failed", retryError)
                    throw wrapException(retryError)
                }
            }
            Log.e(TAG, "Error calling interpretTarotReading", e)
            throw wrapException(e)
        }
    }

    suspend fun spendCoins(readingType: String): Map<String, Any> {
        ensureFreshToken()
        val data = hashMapOf("readingType" to readingType)
        return try {
            val result = functions.getHttpsCallable("spendCoins").call(data).await()
            @Suppress("UNCHECKED_CAST")
            result.data as? Map<String, Any> ?: emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Error calling spendCoins", e)
            throw wrapException(e)
        }
    }

    suspend fun generateDailyInsight(cardId: Int, isReversed: Boolean, language: String): String {
        ensureFreshToken()
        val data = hashMapOf(
            "cardId" to cardId,
            "isReversed" to isReversed,
            "language" to language
        )
        return try {
            val result = functions.getHttpsCallable("generateDailyInsight").call(data).await()
            val response = result.data as? Map<*, *>
            response?.get("insight") as? String ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error calling generateDailyInsight", e)
            throw wrapException(e)
        }
    }
}
