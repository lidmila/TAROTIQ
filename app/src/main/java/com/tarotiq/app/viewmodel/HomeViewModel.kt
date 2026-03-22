package com.tarotiq.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tarotiq.app.data.local.DailyCardDao
import com.tarotiq.app.data.preferences.SettingsManager
import com.tarotiq.app.data.repository.CoinRepository
import com.tarotiq.app.data.repository.GamificationRepository
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
    private val gamificationRepo = GamificationRepository(db.dailyCardDao(), db.achievementDao(), settingsManager)
    private val coinRepo = CoinRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _dailyCard = MutableStateFlow<DailyCard?>(null)
    val dailyCard: StateFlow<DailyCard?> = _dailyCard.asStateFlow()

    private val _dailyCardRevealed = MutableStateFlow(false)
    val dailyCardRevealed: StateFlow<Boolean> = _dailyCardRevealed.asStateFlow()

    val currentStreak: StateFlow<Int> = settingsManager.currentStreakFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val coinBalance: StateFlow<CoinBalance> = coinRepo.coinBalance

    val moonPhase = AstroUtils.getCurrentMoonPhase()

    init {
        viewModelScope.launch {
            coinRepo.initializeCoinDocument()
            coinRepo.startListening()
            gamificationRepo.updateStreak()
            checkDailyCard()
        }
    }

    private suspend fun checkDailyCard() {
        val existing = gamificationRepo.getTodaysDailyCard()
        if (existing != null) {
            _dailyCard.value = existing
            _dailyCardRevealed.value = true
        }
    }

    fun drawDailyCard() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
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
        }
    }
}
