package com.tarotiq.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tarotiq.app.data.local.ReadingDao
import com.tarotiq.app.domain.model.TarotReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class TarotReadingRepository(private val readingDao: ReadingDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getReadings(): Flow<List<TarotReading>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return readingDao.getReadingsByUser(userId)
    }

    fun getReadingsByTopic(topic: String): Flow<List<TarotReading>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return readingDao.getReadingsByTopic(userId, topic)
    }

    suspend fun getReadingById(id: String): TarotReading? = readingDao.getReadingById(id)

    suspend fun saveReading(reading: TarotReading) {
        readingDao.insertReading(reading)
        // Sync to Firestore
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .collection("readings").document(reading.id)
                .set(reading).await()
        } catch (_: Exception) { }
    }

    suspend fun updateReading(reading: TarotReading) {
        readingDao.updateReading(reading)
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .collection("readings").document(reading.id)
                .set(reading).await()
        } catch (_: Exception) { }
    }

    suspend fun deleteReading(reading: TarotReading) {
        readingDao.deleteReading(reading)
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .collection("readings").document(reading.id)
                .delete().await()
        } catch (_: Exception) { }
    }

    suspend fun getReadingCount(): Int {
        val userId = auth.currentUser?.uid ?: return 0
        return readingDao.getReadingCount(userId)
    }
}
