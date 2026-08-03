package com.tarotiq.app

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import android.util.Log
import com.tarotiq.app.ads.ConsentManager
import com.tarotiq.app.navigation.AppNavigation
import com.tarotiq.app.ui.theme.TarotIQTheme
import com.tarotiq.app.utils.LocaleUtils

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LocaleUtils.applyLocale(it) } ?: newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initializeAdsAfterConsent()

        val navigateTo = intent?.getStringExtra("navigate_to")

        setContent {
            TarotIQTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(initialRoute = navigateTo)
                }
            }
        }
    }

    /**
     * Nejdřív souhlas, teprve pak reklamy.
     *
     * Google od ledna 2024 vyžaduje pro uživatele z EHP, Velké Británie a Švýcarska
     * souhlas posbíraný přes certifikovanou platformu (u nás UMP); bez něj tam
     * reklamy neservíruje. Mobile Ads SDK se proto smí inicializovat až potom —
     * mimo EHP se `gatherConsent` vrátí prakticky okamžitě a nic nezobrazí.
     */
    private fun initializeAdsAfterConsent() {
        ConsentManager.gatherConsent(this) { canRequestAds ->
            if (canRequestAds) {
                TarotIQApp.initializeAdSdk(applicationContext)
            } else {
                Log.d("MainActivity", "Souhlas nezískán — reklamy se nenačítají")
            }
        }
    }

}
