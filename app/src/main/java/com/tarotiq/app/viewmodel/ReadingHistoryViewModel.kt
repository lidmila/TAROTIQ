package com.tarotiq.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tarotiq.app.data.repository.TarotReadingRepository
import com.tarotiq.app.domain.model.TarotReading
import com.tarotiq.app.utils.DatabaseProvider
import kotlinx.coroutines.flow.Flow

class ReadingHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val readingRepo = TarotReadingRepository(db.readingDao())

    val readings: Flow<List<TarotReading>> = readingRepo.getReadings()

    fun getReadingsByTopic(topic: String): Flow<List<TarotReading>> = readingRepo.getReadingsByTopic(topic)

    suspend fun getReadingById(id: String): TarotReading? = readingRepo.getReadingById(id)

    suspend fun deleteReading(reading: TarotReading) = readingRepo.deleteReading(reading)
}
