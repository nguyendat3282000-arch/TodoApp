// File: domain/model/TaskLog.kt
package com.example.todoapp.domain.model

/**
 * Domain model representing a log entry for a completed task.
 * Used for analytics and habit tracking progress.
 */
data class TaskLog(
    val id: String = "",
    val taskId: String = "",
    val userId: String = "",
    val completedDate: String = "", // "YYYY-MM-DD"
    val pointsEarned: Int = 0
)
