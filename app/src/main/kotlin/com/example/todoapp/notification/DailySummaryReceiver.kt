// File: notification/DailySummaryReceiver.kt
package com.example.todoapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.todoapp.di.ServiceLocator
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for daily-summary notification actions:
 *
 *  - [ACTION_MARK_ALL_DONE]  — marks every listed task as done in Room (syncs to Firestore),
 *                              then refreshes the notification.
 *  - [ACTION_MARK_TASK_DONE] — marks a single task done (via [EXTRA_TASK_ID]).
 *
 * ## Async handling
 * [goAsync] extends the broadcast window; the [PendingResult] is always finished
 * in the `finally` block.
 *
 * ## ServiceLocator usage
 * This receiver runs outside any activity/fragment lifecycle, so constructor
 * injection is not possible. Using ServiceLocator here is acceptable because
 * this receiver is an Android entry point — it is the boundary between the
 * Android system and the app.
 */
class DailySummaryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received action: ${intent.action}")

        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_MARK_ALL_DONE  -> markAllDone(context, intent)
                    ACTION_MARK_TASK_DONE -> markSingleTaskDone(context, intent)
                    else -> Log.w(TAG, "Unknown action: ${intent.action}")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun markAllDone(context: Context, intent: Intent) {
        val taskIds = intent.getStringArrayListExtra(EXTRA_TASK_IDS) ?: return

        var failedCount = 0
        taskIds.forEach { taskId ->
            ServiceLocator.taskRepository.toggleTaskDone(taskId, isDone = true)
                .onFailure { e ->
                    Log.e(TAG, "Failed to mark task $taskId done: $e")
                    failedCount++
                }
        }
        Log.d(TAG, "Marked ${taskIds.size - failedCount} task(s) as done from notification.")

        // Refresh the daily summary notification with the new state
        refreshNotification(context)
    }

    private suspend fun markSingleTaskDone(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        ServiceLocator.taskRepository.toggleTaskDone(taskId, isDone = true)
            .onSuccess { Log.d(TAG, "Task $taskId marked done from notification.") }
            .onFailure { e -> Log.e(TAG, "Failed to mark task $taskId done: $e") }

        refreshNotification(context)
    }

    /**
     * Re-fetches today's pending tasks from Room and updates the daily summary
     * notification accordingly. Runs entirely from the local DB — no network needed.
     */
    private suspend fun refreshNotification(context: Context) {
        val repo   = ServiceLocator.taskRepository
        val userId = ServiceLocator.authRepository.getCurrentUser()?.uid ?: return
        val today  = LocalDate.now().toString()
        try {
            val todayTasks   = repo.getTodayTasksOnce(userId, today)
            val pendingTasks = todayTasks.filter { !it.isDone }
            DailySummaryNotificationHelper.updateDailySummary(context, pendingTasks)
        } catch (e: Exception) {
            Log.e(TAG, "refreshNotification failed: $e")
        }
    }

    companion object {
        const val ACTION_MARK_ALL_DONE  = "com.example.todoapp.ACTION_MARK_ALL_DONE"
        const val ACTION_MARK_TASK_DONE = "com.example.todoapp.ACTION_MARK_TASK_DONE"

        /** ArrayList<String> of task IDs — used with [ACTION_MARK_ALL_DONE]. */
        const val EXTRA_TASK_IDS = "extra_task_ids"

        /** Single task ID — used with [ACTION_MARK_TASK_DONE]. */
        const val EXTRA_TASK_ID  = "extra_task_id"

        private const val TAG = "DailySummaryReceiver"
    }
}
