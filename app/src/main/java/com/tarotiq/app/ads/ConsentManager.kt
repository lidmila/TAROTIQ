package com.tarotiq.app.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.tarotiq.app.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * GDPR souhlas s reklamami přes Google User Messaging Platform (UMP).
 *
 * Google od ledna 2024 vyžaduje pro uživatele z EHP, Velké Británie a Švýcarska
 * souhlas posbíraný přes certifikovanou platformu — bez něj tam reklamy neservíruje.
 * Souhlas se proto musí posbírat dřív, než se inicializuje Mobile Ads SDK.
 *
 * Mimo tyto regiony se nic uživateli nezobrazí a [gatherConsent] rovnou pustí reklamy dál.
 */
object ConsentManager {

    private const val TAG = "ConsentManager"

    /**
     * Uživatel musí mít možnost souhlas kdykoliv změnit — v EHP to Google při
     * kontrole vyžaduje. Profil podle toho zobrazuje/skrývá položku.
     */
    private val _privacyOptionsRequired = MutableStateFlow(false)
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    private var consentInformation: ConsentInformation? = null

    /**
     * Posbírá souhlas a teprve pak zavolá [onConsentGathered].
     *
     * `loadAndShowConsentFormIfRequired` si formulář vyžádá a zobrazí sám, jen když
     * je potřeba. Callback dostane `canRequestAds()`, které platí i po chybě
     * (typicky bez sítě) — souhlas z minulé session zůstává uložený v UMP SDK.
     *
     * @param testDeviceHashedId ID testovacího zařízení z logcatu; v debug buildu
     *   vynutí chování jako v EHP, aby šel formulář vyzkoušet i z Česka
     */
    fun gatherConsent(
        activity: Activity,
        testDeviceHashedId: String? = null,
        onConsentGathered: (canRequestAds: Boolean) -> Unit
    ) {
        val info = UserMessagingPlatform.getConsentInformation(activity.applicationContext)
        consentInformation = info

        val params = ConsentRequestParameters.Builder()
            .apply {
                if (BuildConfig.DEBUG && testDeviceHashedId != null) {
                    setConsentDebugSettings(
                        ConsentDebugSettings.Builder(activity)
                            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                            .addTestDeviceHashedId(testDeviceHashedId)
                            .build()
                    )
                }
            }
            .build()

        info.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error ${formError.errorCode}: ${formError.message}")
                    }
                    finishGathering(info, onConsentGathered)
                }
            },
            { requestError ->
                Log.w(TAG, "Consent info update failed ${requestError.errorCode}: ${requestError.message}")
                finishGathering(info, onConsentGathered)
            }
        )
    }

    /**
     * Otevře formulář pro změnu souhlasu. Volat jen když [privacyOptionsRequired]
     * hlásí true — jinde žádný formulář neexistuje.
     */
    fun showPrivacyOptionsForm(activity: Activity, onFormDismissed: () -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w(TAG, "Privacy options form error ${formError.errorCode}: ${formError.message}")
            }
            refreshState()
            onFormDismissed()
        }
    }

    /** Smí se načítat reklama? Mimo EHP vrací true rovnou, uvnitř až po souhlasu. */
    fun canRequestAds(): Boolean = consentInformation?.canRequestAds() ?: false

    private fun finishGathering(
        info: ConsentInformation,
        onConsentGathered: (canRequestAds: Boolean) -> Unit
    ) {
        refreshState()
        val canRequestAds = info.canRequestAds()
        Log.d(TAG, "Consent gathered, canRequestAds=$canRequestAds")
        onConsentGathered(canRequestAds)
    }

    private fun refreshState() {
        _privacyOptionsRequired.value =
            consentInformation?.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }
}
