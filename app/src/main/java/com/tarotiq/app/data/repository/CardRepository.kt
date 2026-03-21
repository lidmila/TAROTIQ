package com.tarotiq.app.data.repository

import com.tarotiq.app.data.local.TarotCardDao
import com.tarotiq.app.domain.model.TarotCard
import kotlinx.coroutines.flow.Flow

class CardRepository(private val tarotCardDao: TarotCardDao) {

    fun getAllCards(): Flow<List<TarotCard>> = tarotCardDao.getAllCards()

    suspend fun getCardById(id: Int): TarotCard? = tarotCardDao.getCardById(id)

    fun getCardsByArcana(arcana: String): Flow<List<TarotCard>> = tarotCardDao.getCardsByArcana(arcana)

    fun getCardsBySuit(suit: String): Flow<List<TarotCard>> = tarotCardDao.getCardsBySuit(suit)

    fun searchCards(query: String): Flow<List<TarotCard>> = tarotCardDao.searchCards(query)

    suspend fun getCardCount(): Int = tarotCardDao.getCardCount()

    suspend fun insertAll(cards: List<TarotCard>) = tarotCardDao.insertAll(cards)
}
