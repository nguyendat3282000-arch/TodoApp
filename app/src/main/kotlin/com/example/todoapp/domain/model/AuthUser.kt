// File: domain/model/AuthUser.kt
package com.example.todoapp.domain.model

/**
 * Domain representation of a signed-in user.
 *
 * Intentionally minimal — the domain layer only needs the UID for scoping
 * queries, and optional display fields for the UI. No Firebase types are used.
 *
 * @property uid         Unique user identifier from Firebase Auth.
 * @property email       Email address, null for anonymous / social-only accounts.
 * @property displayName Human-readable name, null if not set.
 */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
)
