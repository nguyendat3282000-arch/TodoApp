// File: data/local/dao/TaskDao.kt
package com.example.todoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.todoapp.data.local.entity.TaskEntity
import com.example.todoapp.data.local.entity.TaskLogEntity
import com.example.todoapp.data.local.entity.UserStatsEntity
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

    // ── Giai Đoạn 2: User Stats Queries ───────────────────────────────────────

    @Query("SELECT * FROM user_stats WHERE userId = :userId")
    fun observeUserStats(userId: String): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE userId = :userId")
    suspend fun getUserStats(userId: String): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserStats(stats: UserStatsEntity)

    // ── Giai Đoạn 2: Task Logs Queries ────────────────────────────────────────

    @Query("""
        SELECT * FROM task_logs 
        WHERE userId = :userId AND completedDate BETWEEN :startDate AND :endDate
        ORDER BY completedDate ASC
    """)
    fun observeTaskLogsForPeriod(userId: String, startDate: String, endDate: String): Flow<List<TaskLogEntity>>

    @Query("""
        SELECT * FROM task_logs 
        WHERE userId = :userId AND completedDate BETWEEN :startDate AND :endDate
        ORDER BY completedDate ASC
    """)
    suspend fun getTaskLogsForPeriod(userId: String, startDate: String, endDate: String): List<TaskLogEntity>

    @Query("SELECT * FROM task_logs WHERE taskId = :taskId")
    suspend fun getTaskLogsForTask(taskId: String): List<TaskLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskLog(log: TaskLogEntity)

    @Query("DELETE FROM task_logs WHERE taskId = :taskId")
    suspend fun deleteTaskLogsForTask(taskId: String)

    @Query("DELETE FROM task_logs WHERE taskId = :taskId AND completedDate = :date")
    suspend fun deleteTaskLogForDate(taskId: String, date: String)
}

