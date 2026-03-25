package com.tarotiq.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.tarotiq.app.R
import com.tarotiq.app.data.preferences.SettingsManager
import com.tarotiq.app.data.remote.FirebaseFunctionsClient
import com.tarotiq.app.data.repository.CoinRepository
import com.tarotiq.app.data.repository.TarotReadingRepository
import com.tarotiq.app.domain.model.*
import com.tarotiq.app.utils.AstroUtils
import com.tarotiq.app.utils.CardUtils
import com.tarotiq.app.utils.DatabaseProvider
import com.tarotiq.app.utils.LocaleUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReadingUiState(
    val topic: String = "",
    val question: String? = null,
    val spread: ReadingSpread = ReadingSpread.SINGLE,
    val drawnCards: List<DrawnCard> = emptyList(),
    val revealedCardIndices: Set<Int> = emptySet(),
    val isInterpreting: Boolean = false,
    val interpretation: String = "",
    val readingId: String? = null,
    val error: String? = null,
    val coinSpent: Boolean = false
)

class ReadingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val readingRepo = TarotReadingRepository(db.readingDao())
    private val coinRepo = CoinRepository()
    private val functionsClient = FirebaseFunctionsClient(application)
    private val settingsManager = SettingsManager(application)
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()

    private val _uiState = MutableStateFlow(ReadingUiState())
    val uiState: StateFlow<ReadingUiState> = _uiState.asStateFlow()
    val coinBalance = coinRepo.coinBalance

    fun setTopic(topic: String) {
        _uiState.update { it.copy(topic = topic) }
    }

    fun setQuestion(question: String?) {
        _uiState.update { it.copy(question = question) }
    }

    fun setSpread(spread: ReadingSpread) {
        _uiState.update { it.copy(
            spread = spread,
            drawnCards = emptyList(),
            revealedCardIndices = emptySet(),
            interpretation = "",
            readingId = null,
            error = null,
            coinSpent = false
        ) }
    }

    fun drawCards() {
        viewModelScope.launch {
            val spread = _uiState.value.spread
            // Spend coins IMMEDIATELY on confirm
            if (!_uiState.value.coinSpent) {
                try {
                    functionsClient.spendCoins(spread.key)
                    _uiState.update { it.copy(coinSpent = true) }
                } catch (e: Exception) {
                    Log.e("ReadingVM", "spendCoins failed: ${e.message}", e)
                    val msg = e.message ?: ""
                    val error = when {
                        msg.contains("Insufficient coins", ignoreCase = true) -> "INSUFFICIENT_COINS"
                        msg.contains("unauthenticated", ignoreCase = true) -> "NOT_AUTHENTICATED"
                        else -> msg
                    }
                    _uiState.update { it.copy(error = error) }
                    return@launch
                }
            }
            // Only draw cards after coins are spent
            val cards = CardUtils.drawCards(spread, getApplication())
            _uiState.update { it.copy(drawnCards = cards, revealedCardIndices = emptySet()) }
        }
    }

    fun revealCard(index: Int) {
        _uiState.update { state ->
            state.copy(revealedCardIndices = state.revealedCardIndices + index)
        }
    }

    val allCardsRevealed: StateFlow<Boolean> = _uiState.map { state ->
        val spreadCount = state.spread.cardCount
        state.drawnCards.size >= spreadCount &&
            (0 until spreadCount).all { it in state.revealedCardIndices }
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun requestInterpretation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInterpreting = true, error = null) }
            try {
                val state = _uiState.value
                val zodiac = settingsManager.zodiacSignFlow.first()
                val language = LocaleUtils.getCurrentLanguage(getApplication())
                val moonPhase = AstroUtils.getCurrentMoonPhase().name

                val interpretation = functionsClient.interpretTarotReading(
                    topic = state.topic,
                    question = state.question,
                    spreadType = state.spread.key,
                    drawnCards = state.drawnCards,
                    zodiacSign = zodiac,
                    moonPhase = moonPhase,
                    language = language
                )
                val userId = auth.currentUser?.uid ?: ""
                val reading = TarotReading(
                    userId = userId,
                    topic = state.topic,
                    question = state.question,
                    spreadType = state.spread.key,
                    drawnCardsJson = gson.toJson(state.drawnCards),
                    aiInterpretation = interpretation,
                    coinsCost = state.spread.coinCost,
                    zodiacSign = zodiac
                )
                readingRepo.saveReading(reading)

                _uiState.update { it.copy(
                    isInterpreting = false,
                    interpretation = interpretation,
                    readingId = reading.id
                )}
            } catch (e: Exception) {
                Log.e("ReadingVM", "Interpretation failed: ${e.javaClass.simpleName}: ${e.message}", e)
                val userMessage = when {
                    e.message?.contains("No internet", ignoreCase = true) == true -> "NO_INTERNET"
                    e.message?.contains("unauthenticated", ignoreCase = true) == true -> "NOT_AUTHENTICATED"
                    e.message?.contains("timed out", ignoreCase = true) == true -> "TIMEOUT"
                    e.message?.contains("INTERNAL", ignoreCase = true) == true -> "SERVER_ERROR"
                    else -> e.message ?: "UNKNOWN_ERROR"
                }
                _uiState.update { it.copy(
                    isInterpreting = false,
                    error = userMessage
                )}
            }
        }
    }

    /**
     * Spend 1 coin for extra card. Suspend — blocks until server confirms.
     * Call from picker screen BEFORE navigating back.
     * Returns true on success, false on failure.
     */
    suspend fun spendCoinForExtraCard(): Boolean {
        return try {
            functionsClient.spendCoins("extra_card")
            true
        } catch (e: Exception) {
            Log.e("ReadingVM", "spendCoinForExtraCard failed: ${e.message}", e)
            false
        }
    }

    /**
     * Add the extra card and request interpretation.
     * Call AFTER coins have been spent and navigation returned to interpretation.
     */
    fun interpretExtraCard(cardId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInterpreting = true, error = null) }
            try {
                val app: Application = getApplication()
                val isReversed = kotlin.random.Random.nextBoolean()
                val newCard = DrawnCard(
                    cardId = cardId,
                    isReversed = isReversed,
                    position = "extra",
                    positionMeaning = app.getString(R.string.position_extra)
                )

                val state = _uiState.value
                val updatedCards = state.drawnCards + newCard
                _uiState.update { it.copy(drawnCards = updatedCards) }

                val language = LocaleUtils.getCurrentLanguage(app)
                val conversationHistory = listOf(
                    ChatMessage(role = MessageRole.ASSISTANT, content = state.interpretation)
                )

                val extraInterpretation = functionsClient.interpretTarotReading(
                    topic = state.topic,
                    question = state.question,
                    spreadType = state.spread.key,
                    drawnCards = listOf(newCard),
                    zodiacSign = settingsManager.zodiacSignFlow.first(),
                    moonPhase = AstroUtils.getCurrentMoonPhase().name,
                    language = language,
                    conversationHistory = conversationHistory
                )

                val updatedInterpretation = extraInterpretation + "\n\n---\n\n" + state.interpretation

                _uiState.update { it.copy(
                    interpretation = updatedInterpretation,
                    isInterpreting = false
                )}

                state.readingId?.let { id ->
                    val reading = readingRepo.getReadingById(id)
                    reading?.let {
                        readingRepo.updateReading(it.copy(
                            drawnCardsJson = gson.toJson(updatedCards),
                            aiInterpretation = updatedInterpretation
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.e("ReadingVM", "interpretExtraCard failed: ${e.message}", e)
                _uiState.update { it.copy(isInterpreting = false, error = e.message) }
            }
        }
    }

    fun resetReading() {
        _uiState.value = ReadingUiState()
    }
}
