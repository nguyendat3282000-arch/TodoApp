// File: presentation/auth/AuthUiState.kt
package com.example.todoapp.presentation.auth

/**
 * Represents all possible UI states for the authentication screens (Login / Register).
 *
 * Used by [AuthViewModel] to drive the Compose UI reactively.
 */
sealed class AuthUiState {
    /** Initial state — no operation in progress. */
    object Idle : AuthUiState()

    /** An async auth operation (login, register, Google sign-in) is in progress. */
    object Loading : AuthUiState()

    /** Auth completed successfully. The NavGraph should navigate to Home. */
    object Success : AuthUiState()

    /**
     * An error occurred.
     * @property message Human-readable error message to show in a snackbar.
     */
    data class Error(val message: String) : AuthUiState()
}
