// File: data/local/entity/TaskLogEntity.kt
package com.example.todoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing completion log of a task.
 * Used for drawing dashboard charts and checking flexible habit progress.
 */
@Entity(tableName = "task_logs")
data class TaskLogEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val userId: String,
    val completedDate: String, // "YYYY-MM-DD"
    val pointsEarned: Int
)
