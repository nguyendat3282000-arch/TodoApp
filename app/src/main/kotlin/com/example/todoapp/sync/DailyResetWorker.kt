// File: sync/DailyResetWorker.kt
package com.example.todoapp.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.todoapp.data.local.TodoDatabase
import com.example.todoapp.domain.model.TaskType
import com.example.todoapp.domain.model.FrequencyType
import com.example.todoapp.domain.model.UserStats
import com.example.todoapp.data.mapper.toDomain
import com.example.todoapp.data.mapper.toEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.FirebaseAuth

/**
 * Background worker that runs once a day at midnight (00:00) to:
 * 1. Penalize uncompleted DAILY tasks from yesterday (-2 points).
 * 2. Penalize missed FIXED habits from yesterday (-2 points, reset streak to 0).
 * 3. Reset isDone = false for all HABIT tasks for the new day.
 */
class DailyResetWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: run {
            Log.d(TAG, "No authenticated user — skipping daily reset.")
            return Result.success()
        }

        val db = TodoDatabase.getDatabase(applicationContext)
        val dao = db.taskDao

        val todayStr = LocalDate.now().toString()
        val yesterdayStr = LocalDate.now().minusDays(1).toString()
        val yesterdayDayOfWeek = LocalDate.now().minusDays(1).dayOfWeek.value // 1 = Monday, ..., 7 = Sunday

        Log.d(TAG, "Running daily reset for user: $userId")

        return try {
            val statsEntity = dao.getUserStats(userId)
            var stats = if (statsEntity != null) {
                statsEntity.toDomain()
            } else {
                UserStats(userId = userId, healthScore = 100, totalStreak = 0, lastResetDate = "")
            }

            if (stats.lastResetDate == todayStr) {
                Log.d(TAG, "Daily reset already ran today. Skipping.")
                return Result.success()
            }

            val tasks = dao.getTasksForUser(userId).map { it.toDomain() }
            var pointsDeducted = 0
            val updatedTasks = mutableListOf<com.example.todoapp.domain.model.Task>()

            for (task in tasks) {
                if (task.type == TaskType.DAILY) {
                    if (task.dueDate == yesterdayStr && !task.isDone) {
                        pointsDeducted += 2
                    }
                } else if (task.type == TaskType.HABIT) {
                    var updatedHabit = task.copy(isDone = false)
                    if (task.frequencyType == FrequencyType.FIXED) {
                        if (task.fixedDays.contains(yesterdayDayOfWeek) && task.lastCompletedDate != yesterdayStr) {
                            pointsDeducted += 2
                            updatedHabit = updatedHabit.copy(streak = 0)
                        }
                    }
                    updatedTasks.add(updatedHabit)
                }
            }

            if (updatedTasks.isNotEmpty()) {
                dao.insertOrUpdateAll(updatedTasks.map { it.toEntity() })
            }

            val newScore = maxOf(0, stats.healthScore - pointsDeducted)
            stats = stats.copy(
                healthScore = newScore,
                lastResetDate = todayStr
            )
            dao.insertOrUpdateUserStats(stats.toEntity())

            Log.d(TAG, "Daily reset complete. Points deducted: $pointsDeducted. New health score: $newScore")
            TaskSyncWorker.triggerOneTimeSync(applicationContext)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in DailyResetWorker: $e")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DailyResetWorker"
        private const val WORK_NAME = "DailyResetWork"

        fun scheduleDailyReset(context: Context) {
            val now = LocalDateTime.now()
            val midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)
            val initialDelay = Duration.between(now, midnight).toMinutes()

            val request = PeriodicWorkRequestBuilder<DailyResetWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            Log.d(TAG, "Daily reset scheduled to run in $initialDelay minutes, then every 24 hours.")
        }
    }
}
