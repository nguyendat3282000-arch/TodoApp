// File: domain/model/UserStats.kt
package com.example.todoapp.domain.model

/**
 * Domain model representing gamification statistics for a user.
 *
 * @property userId            The unique identifier of the user.
 * @property healthScore       The current score (0 to 100). Default is 100.
 * @property totalStreak       The longest consecutive streak of completing daily tasks.
 * @property lastResetDate     ISO-8601 date string "YYYY-MM-DD" of the last time points reset checker ran.
 */
data class UserStats(
    val userId: String = "",
    val healthScore: Int = 100,
    val totalStreak: Int = 0,
    val lastResetDate: String = ""
)
