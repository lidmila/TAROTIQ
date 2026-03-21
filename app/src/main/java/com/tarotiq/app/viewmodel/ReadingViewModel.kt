package com.tarotiq.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
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
    val followUpMessages: List<ChatMessage> = emptyList(),
    val isFollowUpLoading: Boolean = false,
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

    fun setTopic(topic: String) {
        _uiState.update { it.copy(topic = topic) }
    }

    fun setQuestion(question: String?) {
        _uiState.update { it.copy(question = question) }
    }

    fun setSpread(spread: ReadingSpread) {
        _uiState.update { it.copy(spread = spread) }
    }

    fun drawCards() {
        val spread = _uiState.value.spread
        val cards = CardUtils.drawCards(spread)
        _uiState.update { it.copy(drawnCards = cards, revealedCardIndices = emptySet()) }
    }

    fun revealCard(index: Int) {
        _uiState.update { state ->
            state.copy(revealedCardIndices = state.revealedCardIndices + index)
        }
    }

    val allCardsRevealed: StateFlow<Boolean> = _uiState.map { state ->
        state.drawnCards.isNotEmpty() && state.revealedCardIndices.size == state.drawnCards.size
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun requestInterpretation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInterpreting = true, error = null) }
            try {
                // Spend coins first
                if (!_uiState.value.coinSpent) {
                    functionsClient.spendCoins(_uiState.value.spread.key)
                    _uiState.update { it.copy(coinSpent = true) }
                }

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

                // Save reading
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
                _uiState.update { it.copy(
                    isInterpreting = false,
                    error = e.message
                )}
            }
        }
    }

    fun sendFollowUp(message: String) {
        viewModelScope.launch {
            val userMsg = ChatMessage(role = MessageRole.USER, content = message)
            _uiState.update { it.copy(
                followUpMessages = it.followUpMessages + userMsg,
                isFollowUpLoading = true
            )}

            try {
                val state = _uiState.value
                val allMessages = listOf(
                    ChatMessage(role = MessageRole.ASSISTANT, content = state.interpretation)
                ) + state.followUpMessages

                val zodiac = settingsManager.zodiacSignFlow.first()
                val language = LocaleUtils.getCurrentLanguage(getApplication())

                val response = functionsClient.interpretTarotReading(
                    topic = state.topic,
                    question = state.question,
                    spreadType = state.spread.key,
                    drawnCards = state.drawnCards,
                    zodiacSign = zodiac,
                    moonPhase = null,
                    language = language,
                    conversationHistory = allMessages
                )

                val assistantMsg = ChatMessage(role = MessageRole.ASSISTANT, content = response)
                _uiState.update { it.copy(
                    followUpMessages = it.followUpMessages + assistantMsg,
                    isFollowUpLoading = false
                )}

                // Update saved reading
                state.readingId?.let { id ->
                    val reading = readingRepo.getReadingById(id)
                    reading?.let {
                        readingRepo.updateReading(it.copy(
                            followUpMessages = gson.toJson(state.followUpMessages + assistantMsg)
                        ))
                    }
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessage(role = MessageRole.ASSISTANT, content = "Error: ${e.message}", isError = true)
                _uiState.update { it.copy(
                    followUpMessages = it.followUpMessages + errorMsg,
                    isFollowUpLoading = false
                )}
            }
        }
    }

    fun resetReading() {
        _uiState.value = ReadingUiState()
    }
}
