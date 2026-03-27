package com.tarotiq.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.tarotiq.app.data.preferences.SettingsManager
import com.tarotiq.app.data.remote.FirebaseFunctionsClient
import com.tarotiq.app.data.repository.GamificationRepository
import com.tarotiq.app.domain.model.DailyCard
import com.tarotiq.app.utils.CardUtils
import com.tarotiq.app.utils.DatabaseProvider
import com.tarotiq.app.utils.LocaleUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DailyCardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val settingsManager = SettingsManager(application)
    private val gamificationRepo = GamificationRepository(db.dailyCardDao(), db.achievementDao(), settingsManager)
    private val functionsClient = FirebaseFunctionsClient(application)
    private val auth = FirebaseAuth.getInstance()

    private val _dailyCard = MutableStateFlow<DailyCard?>(null)
    val dailyCard: StateFlow<DailyCard?> = _dailyCard.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasDrawnToday = MutableStateFlow(false)
    val hasDrawnToday: StateFlow<Boolean> = _hasDrawnToday.asStateFlow()

    /** True once the initial DB check is done — prevents interaction before we know the state. */
    private val _isInitDone = MutableStateFlow(false)
    val isInitDone: StateFlow<Boolean> = _isInitDone.asStateFlow()

    /** True when the card was loaded from a previous session (skip animation, show face immediately). */
    private val _restoredFromDb = MutableStateFlow(false)
    val restoredFromDb: StateFlow<Boolean> = _restoredFromDb.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = gamificationRepo.getTodaysDailyCard()
            if (existing != null) {
                _dailyCard.value = existing
                _hasDrawnToday.value = true
                _restoredFromDb.value = true
            }
            _isInitDone.value = true
        }
    }

    fun drawDailyCard() {
        if (_hasDrawnToday.value) return
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Double-check DB in case init hasn't finished yet
            val existing = gamificationRepo.getTodaysDailyCard()
            if (existing != null) {
                _dailyCard.value = existing
                _hasDrawnToday.value = true
                _isLoading.value = false
                return@launch
            }

            _isLoading.value = true
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val (cardId, isReversed) = CardUtils.drawSingleCard()

            val card = DailyCard(
                userId = userId,
                cardId = cardId,
                isReversed = isReversed,
                date = today
            )
            gamificationRepo.saveDailyCard(card)
            _dailyCard.value = card
            _hasDrawnToday.value = true

            // Try to get AI insight (optional, don't block on failure)
            try {
                val language = LocaleUtils.getCurrentLanguage(getApplication())
                val insight = functionsClient.generateDailyInsight(cardId, isReversed, language)
                val updated = card.copy(briefInsight = insight)
                gamificationRepo.saveDailyCard(updated)
                _dailyCard.value = updated
            } catch (_: Exception) { }

            _isLoading.value = false
        }
    }
}
