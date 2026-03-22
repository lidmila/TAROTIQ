package com.tarotiq.app.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.tarotiq.app.domain.model.ChatMessage
import com.tarotiq.app.domain.model.DrawnCard
import kotlinx.coroutines.tasks.await

class FirebaseFunctionsClient(private val context: Context) {

    companion object {
        private const val TAG = "FirebaseFunctions"
    }

    private val functions = FirebaseFunctions.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()
    private val integrityProvider = IntegrityTokenProvider(context)

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
                .call(data)
                .await()
            val response = result.data as? Map<*, *>
            response?.get("interpretation") as? String ?: "No interpretation received"
        } catch (e: Exception) {
            Log.e(TAG, "Error calling interpretTarotReading", e)
            throw e
        }
    }

    suspend fun spendCoins(readingType: String): Map<String, Any> {
        val data = hashMapOf("readingType" to readingType)
        return try {
            val result = functions.getHttpsCallable("spendCoins").call(data).await()
            @Suppress("UNCHECKED_CAST")
            result.data as? Map<String, Any> ?: emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Error calling spendCoins", e)
            throw e
        }
    }

    suspend fun generateDailyInsight(cardId: Int, isReversed: Boolean, language: String): String {
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
            throw e
        }
    }
}
