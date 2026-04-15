package com.tarotiq.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tarotiq.app.data.local.DailyCardDao
import com.tarotiq.app.data.preferences.SettingsManager
import com.tarotiq.app.data.remote.FirebaseFunctionsClient
import com.tarotiq.app.data.repository.CoinRepository
import com.tarotiq.app.data.repository.GamificationRepository
import com.tarotiq.app.data.repository.StreakUpdateResult
import com.tarotiq.app.utils.LocaleUtils
import com.tarotiq.app.domain.model.CoinBalance
import com.tarotiq.app.domain.model.DailyCard
import com.tarotiq.app.utils.AstroUtils
import com.tarotiq.app.utils.CardUtils
import com.tarotiq.app.utils.DatabaseProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.firebase.auth.FirebaseAuth

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val settingsManager = SettingsManager(application)
    private val gamificationRepo = GamificationRepository(db.dailyCardDao(), db.achievementDao(), settingsManager, db.readingDao())
    private val coinRepo = CoinRepository()
    private val functionsClient = FirebaseFunctionsClient(application)
    private val auth = FirebaseAuth.getInstance()

    private val _dailyCard = MutableStateFlow<DailyCard?>(null)
    val dailyCard: StateFlow<DailyCard?> = _dailyCard.asStateFlow()

    private val _dailyCardRevealed = MutableStateFlow(false)
    val dailyCardRevealed: StateFlow<Boolean> = _dailyCardRevealed.asStateFlow()

    val currentStreak: StateFlow<Int> = settingsManager.currentStreakFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val coinBalance: StateFlow<CoinBalance> = coinRepo.coinBalance

    val moonPhase = AstroUtils.getCurrentMoonPhase()

    // Streak features
    private val _showComebackBanner = MutableStateFlow(false)
    val showComebackBanner: StateFlow<Boolean> = _showComebackBanner.asStateFlow()

    private val _showFreezeNotice = MutableStateFlow(false)
    val showFreezeNotice: StateFlow<Boolean> = _showFreezeNotice.asStateFlow()

    private val _freezesRemaining = MutableStateFlow(0)
    val freezesRemaining: StateFlow<Int> = _freezesRemaining.asStateFlow()

    private val _streakMilestone = MutableStateFlow<Int?>(null)
    val streakMilestone: StateFlow<Int?> = _streakMilestone.asStateFlow()

    val streakFreezes: StateFlow<Int> = settingsManager.streakFreezesFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Daily horoscope
    private val _dailyHoroscope = MutableStateFlow<String?>(null)
    val dailyHoroscope: StateFlow<String?> = _dailyHoroscope.asStateFlow()

    init {
        viewModelScope.launch {
            coinRepo.initializeCoinDocument()
            coinRepo.startListening()
            val result = gamificationRepo.updateStreak()
            handleStreakResult(result)
            checkDailyCard()
            loadDailyHoroscope()
        }
    }

    private fun loadDailyHoroscope() {
        viewModelScope.launch {
            val zodiac = settingsManager.getZodiacSign() ?: return@launch
            val moonPhaseName = moonPhase.name
            val language = LocaleUtils.getCurrentLanguage(getApplication())
            val horoscope = functionsClient.generateDailyHoroscope(zodiac, moonPhaseName, language)
            if (horoscope.isNotBlank()) {
                _dailyHoroscope.value = horoscope
            }
        }
    }

    private suspend fun handleStreakResult(result: StreakUpdateResult) {
        if (result.freezeUsed) {
            _freezesRemaining.value = settingsManager.getStreakFreezes()
            _showFreezeNotice.value = true
        }
        if (result.comebackEligible) {
            _showComebackBanner.value = true
        }
        if (result.milestoneReached != null) {
            _streakMilestone.value = result.milestoneReached
        }
    }

    fun claimComebackReward() {
        viewModelScope.launch {
            settingsManager.setComebackEligible(false)
            _showComebackBanner.value = false
            // Coins are granted via Firestore in a real implementation
            // For now, the banner itself is the engagement hook
        }
    }

    fun dismissFreezeNotice() {
        _showFreezeNotice.value = false
    }

    fun dismissMilestone() {
        _streakMilestone.value = null
    }

    private suspend fun checkDailyCard() {
        val existing = gamificationRepo.getTodaysDailyCard()
        if (existing != null) {
            _dailyCard.value = existing
            _dailyCardRevealed.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        coinRepo.stopListening()
    }

    fun drawDailyCard() {
        if (_dailyCardRevealed.value) return
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            // Double-check DB in case init hasn't finished yet
            val existing = gamificationRepo.getTodaysDailyCard()
            if (existing != null) {
                _dailyCard.value = existing
                _dailyCardRevealed.value = true
                return@launch
            }
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
            _dailyCardRevealed.value = true

            // Check achievements after daily card draw
            val ctx = gamificationRepo.buildAchievementContext()
            gamificationRepo.checkAndUnlockAchievements(ctx)
        }
    }
}
