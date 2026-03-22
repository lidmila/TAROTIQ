package com.tarotiq.app.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.tarotiq.app.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

object AdUnitIds {
    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"

    private const val PROD_BANNER = "ca-app-pub-9561317089977080/2891039313"
    private const val PROD_INTERSTITIAL = "ca-app-pub-9561317089977080/8410626313"
    private const val PROD_NATIVE = "ca-app-pub-9561317089977080/2891039313"

    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    private const val PROD_REWARDED = "ca-app-pub-9561317089977080/4397170574"

    val BANNER: String get() = if (BuildConfig.DEBUG) TEST_BANNER else PROD_BANNER
    val INTERSTITIAL: String get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL else PROD_INTERSTITIAL
    val NATIVE: String get() = if (BuildConfig.DEBUG) TEST_NATIVE else PROD_NATIVE
    val REWARDED: String get() = if (BuildConfig.DEBUG) TEST_REWARDED else PROD_REWARDED
}

@SuppressLint("MissingPermission")
@Composable
fun BannerAdComposable(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = AdUnitIds.BANNER
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
