// File: data/local/dao/TaskDao.kt
package com.example.todoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.todoapp.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for all task read/write operations against the local SQLite database.
 *
 * All reactive queries return [Flow] so Room automatically emits a new list
 * whenever the underlying table changes. One-shot reads are [suspend] functions.
 */
@Dao
interface TaskDao {

    // ── Reactive queries ──────────────────────────────────────────────────────

    @Query("""
        SELECT * FROM tasks
        WHERE userId = :userId AND deletePending = 0
        ORDER BY dueDate ASC, dueTime ASC
    """)
    fun observeTasksForUser(userId: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE alarmSet = 1 AND deletePending = 0
    """)
    fun observeAlarmSetTasks(): Flow<List<TaskEntity>>

    // ── One-shot reads ────────────────────────────────────────────────────────

    @Query("""
        SELECT * FROM tasks
        WHERE userId = :userId AND deletePending = 0
        ORDER BY dueDate ASC, dueTime ASC
    """)
    suspend fun getTasksForUser(userId: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE alarmSet = 1 AND deletePending = 0")
    suspend fun getAlarmSetTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE syncPending = 1")
    suspend fun getPendingSyncTasks(): List<TaskEntity>

    // ── Writes ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(tasks: List<TaskEntity>)

    @Query("""
        UPDATE tasks
        SET isDone = :isDone, syncPending = 1, updatedAt = :updatedAt
        WHERE id = :taskId
    """)
    suspend fun toggleTaskDone(taskId: String, isDone: Boolean, updatedAt: Long)

    @Query("""
        UPDATE tasks
        SET alarmSet = :alarmSet, syncPending = 1, updatedAt = :updatedAt
        WHERE id = :taskId
    """)
    suspend fun setAlarmFlag(taskId: String, alarmSet: Boolean, updatedAt: Long)

    @Query("UPDATE tasks SET deletePending = 1, syncPending = 1 WHERE id = :taskId")
    suspend fun markDeleted(taskId: String)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun hardDelete(taskId: String)
}
