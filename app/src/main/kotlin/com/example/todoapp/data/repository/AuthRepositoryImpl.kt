// File: data/repository/AuthRepositoryImpl.kt
package com.example.todoapp.data.repository

import com.example.todoapp.data.mapper.toAuthUser
import com.example.todoapp.domain.model.AuthUser
import com.example.todoapp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Concrete implementation of [AuthRepository] backed by Firebase Auth.
 *
 * This is the ONLY class in the codebase that imports Firebase Auth SDK types
 * ([FirebaseAuth], [FirebaseUser]). All outgoing values are mapped to domain
 * types ([AuthUser]) via [com.example.todoapp.data.mapper.AuthMapper].
 *
 * All suspend functions wrap Firebase Task API calls with [await] and surface
 * errors as [Result.failure] so callers don't need try/catch.
 *
 * @param auth Firebase Auth instance. Defaulted to [FirebaseAuth.getInstance] so
 *             tests can inject a fake/mock instance.
 */
class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AuthRepository {

    // ── Auth state as a Flow ──────────────────────────────────────────────────

    /**
     * Emits the current [AuthUser] immediately on collection, then on every
     * subsequent auth state change (sign-in, sign-out, token refresh).
     * Returns null when no user is authenticated.
     */
    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toAuthUser())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun getCurrentUser(): AuthUser? = auth.currentUser?.toAuthUser()

    // ── Email / Password ──────────────────────────────────────────────────────

    override suspend fun registerWithEmail(
        email: String,
        password: String,
    ): Result<Unit> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await()
        Unit
    }

    override suspend fun loginWithEmail(
        email: String,
        password: String,
    ): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    // ── Google Sign-In ────────────────────────────────────────────────────────

    /**
     * Exchanges a raw Google ID token (from the Credential Manager API) for a
     * Firebase credential and signs in.
     */
    override suspend fun loginWithGoogle(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        Unit
    }

    // ── Password Reset ────────────────────────────────────────────────────────

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    // ── Sign Out ──────────────────────────────────────────────────────────────

    override suspend fun signOut() {
        auth.signOut()
    }
}
