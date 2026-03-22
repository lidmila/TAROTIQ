package com.tarotiq.app.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdManager(private val context: Context) {

    companion object {
        private const val TAG = "RewardedAdManager"
    }

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    fun preload() {
        if (rewardedAd != null || isLoading) return
        isLoading = true

        RewardedAd.load(
            context,
            AdUnitIds.REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    Log.d(TAG, "Rewarded ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.d(TAG, "Rewarded ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun isReady(): Boolean = rewardedAd != null

    fun show(activity: Activity, onRewarded: () -> Unit, onDismissed: () -> Unit = {}) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    preload()
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    rewardedAd = null
                    preload()
                    onDismissed()
                }
            }
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewarded()
            }
        } else {
            preload()
            onDismissed()
        }
    }
}
