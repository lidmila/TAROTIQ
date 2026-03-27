package com.tarotiq.app.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

data class AuthState(
    val user: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "AuthViewModel"
        // TODO: Replace with your Google Web Client ID from Firebase Console
        private const val WEB_CLIENT_ID = "2788944933-4kutd0a6msnm89gcl8a490upg89acfsv.apps.googleusercontent.com"
    }

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(AuthState(user = auth.currentUser))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _emailLinkSent = MutableStateFlow(false)
    val emailLinkSent: StateFlow<Boolean> = _emailLinkSent.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = _authState.value.copy(user = firebaseAuth.currentUser)
        }
    }

    fun loginWithEmail(email: String, password: String) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = _authState.value.copy(error = "Invalid email format")
            return
        }
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = _authState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(error = mapAuthError(e), isLoading = false)
            }
        }
    }

    fun registerWithEmail(email: String, password: String) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = _authState.value.copy(error = "Invalid email format")
            return
        }
        if (password.length < 6) {
            _authState.value = _authState.value.copy(error = "Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = _authState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(error = mapAuthError(e), isLoading = false)
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun sendSignInLinkToEmail(email: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                val actionCodeSettings = ActionCodeSettings.newBuilder()
                    .setUrl("https://tarotiq.page.link/login")
                    .setHandleCodeInApp(true)
                    .setAndroidPackageName("com.tarotiq.app", true, null)
                    .build()
                auth.sendSignInLinkToEmail(email, actionCodeSettings).await()
                _emailLinkSent.value = true
                _authState.value = _authState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(error = mapAuthError(e), isLoading = false)
            }
        }
    }

    fun signInWithEmailLink(email: String, link: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                if (auth.isSignInWithEmailLink(link)) {
                    auth.signInWithEmailLink(email, link).await()
                }
                _authState.value = _authState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(error = mapAuthError(e), isLoading = false)
            }
        }
    }

    suspend fun signInWithGoogle(credentialManager: CredentialManager, activityContext: Context): Boolean {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        return try {
            val rawNonce = UUID.randomUUID().toString()
            val bytes = MessageDigest.getInstance("SHA-256").digest(rawNonce.toByteArray())
            val hashedNonce = bytes.joinToString("") { "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            handleGoogleSignIn(response, rawNonce)
            _authState.value = _authState.value.copy(isLoading = false)
            true
        } catch (e: GetCredentialCancellationException) {
            _authState.value = _authState.value.copy(isLoading = false)
            false
        } catch (e: NoCredentialException) {
            Log.e(TAG, "Google sign-in: no credential available", e)
            _authState.value = _authState.value.copy(
                error = "No Google account available. Please add a Google account to your device.",
                isLoading = false
            )
            false
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed", e)
            _authState.value = _authState.value.copy(error = mapAuthError(e), isLoading = false)
            false
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private suspend fun handleGoogleSignIn(response: GetCredentialResponse, rawNonce: String) {
        val credential = response.credential
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        auth.signInWithCredential(firebaseCredential).await()
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState()
    }

    fun deleteAccount(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                auth.currentUser?.delete()?.await()
                _authState.value = AuthState()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }

    private fun mapAuthError(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException ->
                "Invalid email or password. Please check your credentials and try again."
            is FirebaseAuthInvalidUserException -> when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "No account found with this email. Please sign up first."
                "ERROR_USER_DISABLED" -> "This account has been disabled. Please contact support."
                else -> "Account error. Please try again or sign up."
            }
            is FirebaseAuthUserCollisionException ->
                "An account with this email already exists. Please sign in instead."
            else -> {
                val msg = e.message ?: return "An unexpected error occurred. Please try again."
                when {
                    "INVALID_LOGIN_CREDENTIALS" in msg || "credential is incorrect" in msg ->
                        "Invalid email or password. Please check your credentials and try again."
                    "network" in msg.lowercase() || "NETWORK_ERROR" in msg ->
                        "Network error. Please check your connection and try again."
                    "too many" in msg.lowercase() || "BLOCKING" in msg ->
                        "Too many attempts. Please wait a moment and try again."
                    else -> msg
                }
            }
        }
    }
}
