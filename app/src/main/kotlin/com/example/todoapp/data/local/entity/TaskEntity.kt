// File: data/local/entity/TaskEntity.kt
package com.example.todoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity for a task.
 *
 * All timestamps are stored as epoch milliseconds (Long) so this class has
 * zero dependency on the Firebase SDK — mappers in [com.example.todoapp.data.mapper.TaskMapper]
 * handle conversions to/from the domain [com.example.todoapp.domain.model.Task].
 *
 * Sync columns:
 *  - [syncPending]   = true when a local write has not yet been pushed to Firestore.
 *  - [deletePending] = true when the record should be deleted from Firestore on next sync.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val dueDate: String,        // "YYYY-MM-DD"
    val dueTime: String,        // "HH:mm"
    val isDone: Boolean,
    val userId: String,
    val alarmSet: Boolean,
    val createdAt: Long,        // epoch millis
    val updatedAt: Long,        // epoch millis
    val syncPending: Boolean = false,
    val deletePending: Boolean = false,

    // --- Giai Đoạn 2 Fields ---
    val type: String = "DAILY", // "DAILY", "HABIT"
    val frequencyType: String? = null, // "FIXED", "FLEXIBLE"
    val fixedDays: String = "", // Comma-separated string of ints, e.g. "1,3,5"
    val flexibleCount: Int = 0,
    val flexibleInterval: String? = null, // "WEEK", "MONTH"
    val streak: Int = 0,
    val lastCompletedDate: String? = null // "YYYY-MM-DD"
)

