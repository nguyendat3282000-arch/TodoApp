// File: data/local/entity/UserStatsEntity.kt
package com.example.todoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing user gamification stats.
 */
@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val userId: String,
    val healthScore: Int,
    val totalStreak: Int,
    val lastResetDate: String // "YYYY-MM-DD" to prevent double reset per day
)
