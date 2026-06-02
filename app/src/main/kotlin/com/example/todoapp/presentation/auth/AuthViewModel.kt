// File: presentation/auth/AuthViewModel.kt
package com.example.todoapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.domain.model.AuthUser
import com.example.todoapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Login and Register screens.
 *
 * Works exclusively with domain types ([AuthUser], [AuthRepository]).
 * Has zero knowledge of Firebase, Android SDK singletons, or Firestore.
 *
 * @param authRepo Domain-level auth contract. Injected via [ViewModelFactory].
 */
class AuthViewModel(
    private val authRepo: AuthRepository,
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** Reactive stream of the currently signed-in user. Null when signed out. */
    val currentUser: Flow<AuthUser?> = authRepo.currentUser

    // ── Actions ───────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepo.loginWithEmail(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Login failed") }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepo.registerWithEmail(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Registration failed") }
        }
    }

    /**
     * Completes the Google Sign-In flow with the raw ID token obtained from the
     * Credential Manager API (launched in MainActivity, not here).
     */
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepo.loginWithGoogle(idToken)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Google sign-in failed") }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            authRepo.sendPasswordReset(email)
                .onSuccess { _uiState.value = AuthUiState.Error("Reset email sent 📧") }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Failed to send reset email") }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepo.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }

    /** Signals that the Credential Manager Google Sign-In flow is starting. */
    fun startGoogleSignIn() {
        _uiState.value = AuthUiState.Loading
    }

    /**
     * Called by MainActivity when the Credential Manager flow fails, so the
     * Auth screen can show a helpful snackbar.
     */
    fun handleGoogleSignInError(e: Throwable) {
        val message = when {
            e.message?.contains("No credentials", ignoreCase = true) == true ||
            e.message?.contains("No Google", ignoreCase = true) == true ->
                "No Google accounts found. Please add one in Settings."
            e.message?.contains("cancel", ignoreCase = true) == true ->
                "Sign-in cancelled."
            else -> "Google sign-in failed: ${e.message}"
        }
        _uiState.value = AuthUiState.Error(message)
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
