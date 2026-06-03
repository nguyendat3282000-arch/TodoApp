// File: domain/model/Task.kt
package com.example.todoapp.domain.model

/**
 * Pure domain entity representing a single to-do task.
 *
 * This class is in the innermost layer of the Clean Architecture and has
 * ZERO dependencies on Android, Room, Firebase, or any other framework.
 * All timestamps are stored as epoch milliseconds (Long) so the domain
 * layer never needs to know about Firestore's Timestamp class.
 *
 * @property id          Unique identifier (UUID string). Generated locally on create.
 * @property title       Short title. Required.
 * @property description Optional longer description.
 * @property dueDate     ISO-8601 date string "YYYY-MM-DD".
 * @property dueTime     24-hour time string "HH:mm".
 * @property isDone      Whether the task has been completed.
 * @property userId      UID of the owning user.
 * @property alarmSet    True when an exact alarm is scheduled for this task.
 * @property createdAt   Creation epoch millis. 0 if unknown.
 * @property updatedAt   Last-update epoch millis. 0 if unknown.
 * @property syncPending True when local changes have not yet been pushed to the remote.
 * @property deletePending True when this record is soft-deleted and awaiting remote deletion.
 */
enum class TaskType {
    DAILY,
    HABIT
}

enum class FrequencyType {
    FIXED,
    FLEXIBLE
}

enum class FlexibleInterval {
    WEEK,
    MONTH
}

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: String = "",       // "YYYY-MM-DD"
    val dueTime: String = "",       // "HH:mm"
    val isDone: Boolean = false,
    val userId: String = "",
    val alarmSet: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val syncPending: Boolean = false,
    val deletePending: Boolean = false,
    
    // --- Giai Đoạn 2 Fields ---
    val type: TaskType = TaskType.DAILY,
    val frequencyType: FrequencyType? = null,
    val fixedDays: List<Int> = emptyList(), // 1 = Monday, ..., 7 = Sunday
    val flexibleCount: Int = 0,
    val flexibleInterval: FlexibleInterval? = null,
    val streak: Int = 0,
    val lastCompletedDate: String? = null    // "YYYY-MM-DD"
)

