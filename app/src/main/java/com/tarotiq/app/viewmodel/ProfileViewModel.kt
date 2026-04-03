package com.tarotiq.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tarotiq.app.data.preferences.SettingsManager
import com.tarotiq.app.data.repository.CoinRepository
import com.tarotiq.app.data.repository.GamificationRepository
import com.tarotiq.app.data.repository.TarotReadingRepository
import com.tarotiq.app.domain.model.Achievement
import com.tarotiq.app.domain.model.CoinBalance
import com.tarotiq.app.utils.DatabaseProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val settingsManager = SettingsManager(application)
    private val readingRepo = TarotReadingRepository(db.readingDao())
    private val coinRepo = CoinRepository()
    private val gamificationRepo = GamificationRepository(db.dailyCardDao(), db.achievementDao(), settingsManager)

    val zodiacSign: StateFlow<String?> = settingsManager.zodiacSignFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val currentStreak: StateFlow<Int> = settingsManager.currentStreakFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val longestStreak: StateFlow<Int> = settingsManager.longestStreakFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val coinBalance: StateFlow<CoinBalance> = coinRepo.coinBalance

    val achievements: StateFlow<List<Achievement>> = gamificationRepo.getAchievements()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _totalReadings = MutableStateFlow(0)
    val totalReadings: StateFlow<Int> = _totalReadings.asStateFlow()

    init {
        viewModelScope.launch {
            coinRepo.startListening()
            _totalReadings.value = readingRepo.getReadingCount()
        }
    }

    override fun onCleared() {
        super.onCleared()
        coinRepo.stopListening()
    }

    fun setZodiacSign(sign: String) {
        viewModelScope.launch {
            settingsManager.setZodiacSign(sign)
        }
    }
}
