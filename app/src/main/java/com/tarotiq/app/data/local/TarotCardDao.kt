package com.tarotiq.app.data.local

import androidx.room.*
import com.tarotiq.app.domain.model.TarotCard
import kotlinx.coroutines.flow.Flow

@Dao
interface TarotCardDao {
    @Query("SELECT * FROM tarot_cards ORDER BY id ASC")
    fun getAllCards(): Flow<List<TarotCard>>

    @Query("SELECT * FROM tarot_cards WHERE id = :id")
    suspend fun getCardById(id: Int): TarotCard?

    @Query("SELECT * FROM tarot_cards WHERE arcana = :arcana ORDER BY id ASC")
    fun getCardsByArcana(arcana: String): Flow<List<TarotCard>>

    @Query("SELECT * FROM tarot_cards WHERE suit = :suit ORDER BY number ASC")
    fun getCardsBySuit(suit: String): Flow<List<TarotCard>>

    @Query("SELECT * FROM tarot_cards WHERE nameKey LIKE '%' || :query || '%'")
    fun searchCards(query: String): Flow<List<TarotCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<TarotCard>)

    @Query("SELECT COUNT(*) FROM tarot_cards")
    suspend fun getCardCount(): Int
}
