// File: domain/repository/TaskRepository.kt
package com.example.todoapp.domain.repository

import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.UserStats
import com.example.todoapp.domain.model.TaskLog
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for all Task persistence and retrieval operations.
 *
 * Design principles enforced here:
 *  - **Offline-first**: All reads come from the local database (Room), which is
 *    the Single Source of Truth. Remote sync is a background concern and is NOT
 *    exposed through this interface.
 *  - **Dependency Rule**: This interface lives in the domain layer and references
 *    only domain types. Data-layer implementations (Room, Firestore) are hidden
 *    behind this abstraction.
 *  - **Flow for reactive reads**: [observeTasksForUser] emits a new list on every
 *    change so the UI can react without polling.
 */
interface TaskRepository {

    /**
     * A reactive [Flow] of all non-deleted tasks belonging to [userId], ordered
     * by due date ascending, then due time ascending.
     *
     * Backed by a Room snapshot — emits immediately from local cache, and again
     * on every write (including writes triggered by the background sync worker).
     *
     * @param userId The authenticated user's UID.
     */
    fun observeTasksForUser(userId: String): Flow<List<Task>>

    /**
     * One-shot fetch of today's tasks for [userId].
     * Used by the Glance widget and notification helper where collecting a Flow
     * is not practical.
     *
     * @param userId    The authenticated user's UID.
     * @param todayDate ISO-8601 date string "YYYY-MM-DD".
     */
    suspend fun getTodayTasksOnce(userId: String, todayDate: String): List<Task>

    /**
     * Fetches a single task by [taskId] from the local database.
     * Returns null if the task does not exist locally.
     */
    suspend fun getTaskById(taskId: String): Task?

    /**
     * Persists a new [task] to the local database and enqueues a sync job.
     * A UUID is generated locally so the result is available immediately without
     * waiting for a network round-trip.
     *
     * @return [Result] wrapping the locally generated task ID on success.
     */
    suspend fun addTask(task: Task): Result<String>

    /**
     * Fully replaces the local record for [task] using [Task.id] as the key,
     * and enqueues a sync job.
     */
    suspend fun updateTask(task: Task): Result<Unit>

    /**
     * Toggles the [Task.isDone] flag for the task identified by [taskId],
     * and enqueues a sync job.
     *
     * @param taskId Unique task identifier.
     * @param isDone The new completion state.
     */
    suspend fun toggleTaskDone(taskId: String, isDone: Boolean): Result<Unit>

    /**
     * Updates the [Task.alarmSet] flag for the task identified by [taskId],
     * and enqueues a sync job.
     *
     * @param taskId   Unique task identifier.
     * @param alarmSet Whether an exact alarm is currently scheduled.
     */
    suspend fun setAlarmFlag(taskId: String, alarmSet: Boolean): Result<Unit>

    /**
     * Soft-deletes the task identified by [taskId] (marks [Task.deletePending] = true)
     * and enqueues a sync job that will hard-delete the remote record.
     */
    suspend fun deleteTask(taskId: String): Result<Unit>

    /**
     * Returns a [Flow] of all tasks (across all users) that have [Task.alarmSet] = true
     * and are not pending deletion.
     *
     * Used exclusively by [com.example.todoapp.alarm.BootReceiver] to re-schedule
     * alarms after a device reboot, when the current user session is not available.
     */
    fun observeAlarmSetTasks(): Flow<List<Task>>

    // ── Giai Đoạn 2: Gamification & Logs ──────────────────────────────────────

    fun observeUserStats(userId: String): Flow<UserStats?>

    suspend fun getUserStats(userId: String): UserStats?

    suspend fun saveUserStats(stats: UserStats): Result<Unit>

    fun observeTaskLogsForPeriod(userId: String, startDate: String, endDate: String): Flow<List<TaskLog>>

    suspend fun getTaskLogsForPeriod(userId: String, startDate: String, endDate: String): List<TaskLog>

    suspend fun addTaskLog(log: TaskLog): Result<Unit>

    suspend fun deleteTaskLogsForTask(taskId: String): Result<Unit>

    suspend fun deleteTaskLogForDate(taskId: String, date: String): Result<Unit>
}
