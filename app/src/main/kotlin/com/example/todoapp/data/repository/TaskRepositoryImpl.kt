// File: data/repository/TaskRepositoryImpl.kt
package com.example.todoapp.data.repository

import android.content.Context
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.data.mapper.toDomain
import com.example.todoapp.data.mapper.toEntity
import com.example.todoapp.data.remote.FirestoreTaskSource
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.UserStats
import com.example.todoapp.domain.model.TaskLog
import com.example.todoapp.domain.repository.TaskRepository
import com.example.todoapp.sync.TaskSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Offline-first implementation of [TaskRepository].
 *
 * **Architecture contract:**
 *  - ALL reads and writes go through [TaskDao] (Room) as the Single Source of Truth.
 *  - [FirestoreTaskSource] is NEVER called directly from this class for reads;
 *    all Firestore communication is delegated to [TaskSyncWorker] running in the background.
 *  - Each mutating operation writes to Room first (for instant UI feedback), then
 *    enqueues a one-time WorkManager sync job via [triggerSync].
 *
 * @param dao               Room DAO for local persistence.
 * @param remoteSource      Firestore data source (used only during sync, stored here
 *                          so ServiceLocator can share the same instance with the worker).
 * @param context           Application context needed for WorkManager.
 */
class TaskRepositoryImpl(
    private val dao: TaskDao,
    private val remoteSource: FirestoreTaskSource,
    private val context: Context,
) : TaskRepository {

    // ── Reactive reads ────────────────────────────────────────────────────────

    override fun observeTasksForUser(userId: String): Flow<List<Task>> {
        triggerSync()
        return dao.observeTasksForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeAlarmSetTasks(): Flow<List<Task>> =
        dao.observeAlarmSetTasks().map { entities ->
            entities.map { it.toDomain() }
        }

    // ── One-shot reads ────────────────────────────────────────────────────────

    override suspend fun getTodayTasksOnce(userId: String, todayDate: String): List<Task> =
        dao.getTasksForUser(userId)
            .filter { it.dueDate == todayDate }
            .map { it.toDomain() }

    override suspend fun getTaskById(taskId: String): Task? =
        dao.getTaskById(taskId)?.toDomain()

    // ── Writes ────────────────────────────────────────────────────────────────

    override suspend fun addTask(task: Task): Result<String> = runCatching {
        val id = task.id.ifBlank { java.util.UUID.randomUUID().toString() }
        val entity = task.copy(id = id).toEntity(syncPending = true)
        dao.insertOrUpdate(entity)
        triggerSync()
        id
    }

    override suspend fun updateTask(task: Task): Result<Unit> = runCatching {
        dao.insertOrUpdate(task.toEntity(syncPending = true))
        triggerSync()
    }

    override suspend fun toggleTaskDone(taskId: String, isDone: Boolean): Result<Unit> =
        runCatching {
            dao.toggleTaskDone(taskId, isDone, System.currentTimeMillis())
            triggerSync()
        }

    override suspend fun setAlarmFlag(taskId: String, alarmSet: Boolean): Result<Unit> =
        runCatching {
            dao.setAlarmFlag(taskId, alarmSet, System.currentTimeMillis())
            triggerSync()
        }

    override suspend fun deleteTask(taskId: String): Result<Unit> = runCatching {
        dao.markDeleted(taskId)
        triggerSync()
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Enqueues a one-time WorkManager sync request (network-constrained).
     * Safe to call on every mutation — WorkManager deduplicates via unique work name.
     */
    private fun triggerSync() {
        TaskSyncWorker.triggerOneTimeSync(context)
    }

    // ── Giai Đoạn 2: Gamification & Logs Implementation ──────────────────────

    override fun observeUserStats(userId: String): Flow<UserStats?> {
        return dao.observeUserStats(userId).map { it?.toDomain() }
    }

    override suspend fun getUserStats(userId: String): UserStats? {
        return dao.getUserStats(userId)?.toDomain()
    }

    override suspend fun saveUserStats(stats: UserStats): Result<Unit> = runCatching {
        dao.insertOrUpdateUserStats(stats.toEntity())
    }

    override fun observeTaskLogsForPeriod(userId: String, startDate: String, endDate: String): Flow<List<TaskLog>> {
        return dao.observeTaskLogsForPeriod(userId, startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskLogsForPeriod(userId: String, startDate: String, endDate: String): List<TaskLog> =
        dao.getTaskLogsForPeriod(userId, startDate, endDate).map { it.toDomain() }

    override suspend fun addTaskLog(log: TaskLog): Result<Unit> = runCatching {
        dao.insertTaskLog(log.toEntity())
    }

    override suspend fun deleteTaskLogsForTask(taskId: String): Result<Unit> = runCatching {
        dao.deleteTaskLogsForTask(taskId)
    }

    override suspend fun deleteTaskLogForDate(taskId: String, date: String): Result<Unit> = runCatching {
        dao.deleteTaskLogForDate(taskId, date)
    }
}
