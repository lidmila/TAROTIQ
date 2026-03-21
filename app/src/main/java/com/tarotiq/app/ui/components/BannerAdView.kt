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

    private const val PROD_BANNER = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
    private const val PROD_INTERSTITIAL = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
    private const val PROD_NATIVE = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"

    val BANNER: String get() = if (BuildConfig.DEBUG) TEST_BANNER else PROD_BANNER
    val INTERSTITIAL: String get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL else PROD_INTERSTITIAL
    val NATIVE: String get() = if (BuildConfig.DEBUG) TEST_NATIVE else PROD_NATIVE
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
