package com.tarotiq.app.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory
import com.tarotiq.app.data.preferences.SettingsManager
import kotlinx.coroutines.flow.first

object InAppReviewManager {

    private const val TAG = "InAppReview"
    private const val MIN_APP_OPENS = 5

    suspend fun maybeRequestReview(activity: Activity, settingsManager: SettingsManager) {
        val hasRated = settingsManager.hasRatedAppFlow.first()
        if (hasRated) return

        val appOpenCount = settingsManager.appOpenCountFlow.first()
        if (appOpenCount < MIN_APP_OPENS) return

        try {
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener {
                        Log.d(TAG, "Review flow completed")
                    }
                } else {
                    Log.w(TAG, "Review request failed", task.exception)
                }
            }
            settingsManager.setHasRatedApp(true)
        } catch (e: Exception) {
            Log.w(TAG, "Review request error", e)
        }
    }
}
