// File: domain/repository/AuthRepository.kt
package com.example.todoapp.domain.repository

import com.example.todoapp.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for all authentication operations.
 *
 * This interface lives in the domain layer and uses only domain types ([AuthUser]).
 * It knows nothing about Firebase, Google Identity, or any concrete auth SDK.
 *
 * The concrete implementation ([com.example.todoapp.data.repository.AuthRepositoryImpl])
 * wraps Firebase Auth and maps [com.google.firebase.auth.FirebaseUser] → [AuthUser].
 */
interface AuthRepository {

    /**
     * A hot [Flow] that emits the currently signed-in [AuthUser] whenever the
     * authentication state changes, or `null` when no user is signed in.
     *
     * Backed by a Firebase AuthStateListener. Callers should collect this in a
     * lifecycle-aware scope to avoid leaks.
     */
    val currentUser: Flow<AuthUser?>

    /**
     * Returns the currently signed-in [AuthUser] synchronously, or `null` if
     * no user is signed in. Does not suspend.
     */
    fun getCurrentUser(): AuthUser?

    /**
     * Creates a new account with [email] and [password].
     * @return [Result.success] on success; [Result.failure] with the underlying
     *         exception on failure (e.g. email already in use, weak password).
     */
    suspend fun registerWithEmail(email: String, password: String): Result<Unit>

    /**
     * Signs in an existing user with [email] and [password].
     * @return [Result.success] on success; [Result.failure] on wrong credentials
     *         or network error.
     */
    suspend fun loginWithEmail(email: String, password: String): Result<Unit>

    /**
     * Signs in (or links) a Google account using the raw Google ID token obtained
     * from the modern Credential Manager API.
     *
     * @param idToken The Google ID token string.
     * @return [Result.success] on success; [Result.failure] on error.
     */
    suspend fun loginWithGoogle(idToken: String): Result<Unit>

    /**
     * Sends a password-reset email to [email].
     * @return [Result.success] if the email was dispatched; [Result.failure] otherwise.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit>

    /**
     * Signs out the currently authenticated user. Safe to call even if no user
     * is signed in.
     */
    suspend fun signOut()
}
