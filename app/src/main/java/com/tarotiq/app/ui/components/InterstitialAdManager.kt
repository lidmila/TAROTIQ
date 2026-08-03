package com.tarotiq.app.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.tarotiq.app.TarotIQApp

class InterstitialAdManager(private val context: Context) {

    companion object {
        private const val TAG = "InterstitialAdManager"
    }

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    /**
     * Načítá se až po inicializaci SDK, která proběhne po získání GDPR souhlasu —
     * dřív by šla reklama v EHP bez souhlasu. `whenAdSdkReady` počká, když je
     * uživatel zrovna ve formuláři souhlasu.
     */
    fun preload() {
        if (interstitialAd != null || isLoading) return
        isLoading = true

        TarotIQApp.whenAdSdkReady {
            InterstitialAd.load(
                context,
                AdUnitIds.INTERSTITIAL,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoading = false
                        Log.d(TAG, "Interstitial ad loaded")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoading = false
                        Log.d(TAG, "Interstitial ad failed to load: ${error.message}")
                    }
                }
            )
        }
    }

    fun showIfReady(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    preload()
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    preload()
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            preload()
            onDismissed()
        }
    }
}

@Composable
fun rememberInterstitialAdManager(): InterstitialAdManager {
    val context = LocalContext.current
    return remember { InterstitialAdManager(context) }
}
