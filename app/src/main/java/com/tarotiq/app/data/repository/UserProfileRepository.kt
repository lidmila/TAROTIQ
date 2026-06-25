package com.tarotiq.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

/**
 * Syncs account-level settings (zodiac, streak, notifications, limits, ...) to
 * Firestore so they survive app reinstalls and follow the account across devices.
 * The local DataStore (SettingsManager) stays the source of truth for the UI;
 * this just backs it up and restores it on login.
 */
class UserProfileRepository {

    companion object {
        private const val TAG = "UserProfileRepository"
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun settingsRef(userId: String) =
        firestore.collection("users").document(userId)
            .collection("profile").document("settings")

    suspend fun saveSettings(data: Map<String, Any>) {
        val userId = auth.currentUser?.uid ?: return
        if (data.isEmpty()) return
        try {
            settingsRef(userId).set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save settings to Firestore", e)
        }
    }

    suspend fun fetchSettings(): Map<String, Any>? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val ref = settingsRef(userId)
            val doc = try { ref.get(Source.SERVER).await() } catch (_: Exception) { ref.get().await() }
            doc.data
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch settings from Firestore", e)
            null
        }
    }
}
