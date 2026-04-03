package com.tarotiq.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.tarotiq.app.data.local.CardDataInitializer
import com.tarotiq.app.data.preferences.SettingsManager
import com.tarotiq.app.data.remote.IntegrityTokenProvider
import com.tarotiq.app.utils.DatabaseProvider
import com.tarotiq.app.utils.LocaleUtils
import com.tarotiq.app.worker.DailyCardReminderWorker
import com.tarotiq.app.worker.ReminderScheduler
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TarotIQApp : Application() {

    companion object {
        lateinit var instance: TarotIQApp
            private set

        private val _isAdSdkInitialized = MutableStateFlow(false)
        val isAdSdkInitialized: StateFlow<Boolean> = _isAdSdkInitialized.asStateFlow()
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base?.let { LocaleUtils.applyLocale(it) } ?: base)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize locale flow
        LocaleUtils.initialize(this)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Enable Firestore offline persistence
        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                .setSizeBytes(50L * 1024L * 1024L) // 50 MB cache
                .build())
            .build()

        // Initialize Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)
            setCustomKey("app_version", BuildConfig.VERSION_NAME)
            setCustomKey("app_version_code", BuildConfig.VERSION_CODE)
        }

        // Initialize AdMob
        MobileAds.initialize(this) {
            _isAdSdkInitialized.value = true
        }

        // Create notification channel
        createNotificationChannel()

        // Initialize card database on first launch
        applicationScope.launch(Dispatchers.IO) {
            val db = DatabaseProvider.getDatabase(this@TarotIQApp)
            CardDataInitializer.initializeCards(db.tarotCardDao())
        }

        // Warm up Play Integrity token provider
        applicationScope.launch(Dispatchers.IO) {
            IntegrityTokenProvider(this@TarotIQApp).warmUp()
        }

        // Schedule daily card reminder if enabled
        applicationScope.launch {
            val settingsManager = SettingsManager(this@TarotIQApp)
            val enabled = settingsManager.notificationEnabledFlow.first()
            if (enabled) {
                val time = settingsManager.notificationTimeFlow.first()
                ReminderScheduler.schedule(this@TarotIQApp, time)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            DailyCardReminderWorker.CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}
