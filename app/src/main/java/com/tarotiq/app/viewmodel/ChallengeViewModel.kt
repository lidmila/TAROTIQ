package com.tarotiq.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tarotiq.app.domain.model.ActiveChallenge
import com.tarotiq.app.domain.model.ChallengeDefinition
import com.tarotiq.app.utils.DatabaseProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class ChallengeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val userId: String? get() = auth.currentUser?.uid

    private val _activeChallenge = MutableStateFlow<ActiveChallenge?>(null)
    val activeChallenge: StateFlow<ActiveChallenge?> = _activeChallenge.asStateFlow()

    init {
        viewModelScope.launch {
            userId?.let { uid ->
                db.challengeDao().getActiveChallenge(uid).collect { challenge ->
                    _activeChallenge.value = challenge
                }
            }
        }
    }

    private val _completionMessage = MutableStateFlow<String?>(null)
    val completionMessage: StateFlow<String?> = _completionMessage.asStateFlow()

    fun startChallenge(challengeId: String) {
        val uid = userId ?: return
        viewModelScope.launch {
            val existing = db.challengeDao().getActiveChallengeOnce(uid)
            if (existing != null) return@launch
            val today = dateFormat.format(Date())
            db.challengeDao().insertChallenge(
                ActiveChallenge(
                    challengeId = challengeId,
                    userId = uid,
                    startDate = today,
                    currentDay = 1,
                    completedDaysJson = "[]",
                    isCompleted = false,
                    lastActivityDate = ""
                )
            )
        }
    }

    fun abandonChallenge() {
        val uid = userId ?: return
        viewModelScope.launch {
            val challenge = db.challengeDao().getActiveChallengeOnce(uid) ?: return@launch
            db.challengeDao().deleteChallenge(uid, challenge.challengeId)
        }
    }

    fun completeToday(challengeDef: ChallengeDefinition) {
        val uid = userId ?: return
        viewModelScope.launch {
            val challenge = db.challengeDao().getActiveChallengeOnce(uid) ?: return@launch
            val today = dateFormat.format(Date())
            if (challenge.lastActivityDate == today) return@launch

            val completedDays = JSONArray(challenge.completedDaysJson).let { arr ->
                mutableListOf<Int>().also { list ->
                    for (i in 0 until arr.length()) list.add(arr.getInt(i))
                }
            }
            completedDays.add(challenge.currentDay)
            val newJson = JSONArray(completedDays).toString()

            val nextDay = challenge.currentDay + 1
            val isFinished = nextDay > challengeDef.durationDays

            val updated = challenge.copy(
                currentDay = if (isFinished) challenge.currentDay else nextDay,
                completedDaysJson = newJson,
                isCompleted = isFinished,
                lastActivityDate = today
            )
            db.challengeDao().updateChallenge(updated)

            if (isFinished) {
                grantChallengeReward(uid, challengeDef)
                _completionMessage.value = challengeDef.id
            }
        }
    }

    private suspend fun grantChallengeReward(uid: String, challengeDef: ChallengeDefinition) {
        try {
            val coinRef = firestore.collection("users").document(uid)
                .collection("coins").document("balance")
            firestore.runTransaction { transaction ->
                val doc = transaction.get(coinRef)
                val current = doc.getLong("balance")?.toInt() ?: 0
                transaction.update(coinRef, "balance", current + challengeDef.coinReward)
            }.await()
        } catch (e: Exception) {
            Log.e("ChallengeVM", "Failed to grant reward", e)
        }
    }

    fun clearCompletionMessage() {
        _completionMessage.value = null
    }

    fun isTodayCompleted(challenge: ActiveChallenge): Boolean {
        val today = dateFormat.format(Date())
        return challenge.lastActivityDate == today
    }
}
