package com.tarotiq.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tarotiq.app.data.repository.CardRepository
import com.tarotiq.app.domain.model.TarotCard
import com.tarotiq.app.utils.DatabaseProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class CardLibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val cardRepo = CardRepository(db.tarotCardDao())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val cards: StateFlow<List<TarotCard>> = combine(_searchQuery, _selectedFilter) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        when {
            query.isNotBlank() -> cardRepo.searchCards(query)
            filter == "major" -> cardRepo.getCardsByArcana("major")
            filter in listOf("cups", "pentacles", "swords", "wands") -> cardRepo.getCardsBySuit(filter)
            else -> cardRepo.getAllCards()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setFilter(filter: String) { _selectedFilter.value = filter }

    suspend fun getCardById(id: Int): TarotCard? = cardRepo.getCardById(id)
}
