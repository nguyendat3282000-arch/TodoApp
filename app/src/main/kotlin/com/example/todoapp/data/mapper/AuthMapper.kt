// File: data/mapper/AuthMapper.kt
package com.example.todoapp.data.mapper

import com.example.todoapp.domain.model.AuthUser
import com.google.firebase.auth.FirebaseUser

/**
 * Maps a Firebase SDK [FirebaseUser] to the domain [AuthUser].
 *
 * This is the only place in the codebase that is allowed to reference
 * [FirebaseUser]. All layers above the data layer (domain, presentation)
 * work exclusively with [AuthUser].
 *
 * Returns `null` if [this] is `null`, allowing safe chained calls:
 * ```kotlin
 * auth.currentUser?.toAuthUser()
 * ```
 */
fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
    uid         = uid,
    email       = email,
    displayName = displayName,
)
