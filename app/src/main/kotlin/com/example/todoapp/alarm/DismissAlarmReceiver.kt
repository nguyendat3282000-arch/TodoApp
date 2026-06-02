// File: alarm/DismissAlarmReceiver.kt
package com.example.todoapp.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.todoapp.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for the "Dismiss" and "Mark as Done" notification actions,
 * as well as the dismiss flow inside [com.example.todoapp.ui.screens.alarm.AlarmActivity].
 *
 * ## Actions handled
 *  - [AlarmConstants.ACTION_DISMISS_ALARM] — stop the alarm service + cancel the notification.
 *  - [ACTION_MARK_DONE]  — same as dismiss, then toggle the task done in Room (syncs to Firestore).
 *
 * ## Async handling
 * [goAsync] extends the broadcast execution window; the [PendingResult] is
 * finished in the `finally` block after all async work completes.
 */
class DismissAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId  = intent.getStringExtra(AlarmConstants.EXTRA_TASK_ID) ?: return
        val notifId = intent.getIntExtra(AlarmConstants.EXTRA_NOTIF_ID, -1)

        Log.d(TAG, "action=${intent.action}, task=$taskId, notifId=$notifId")

        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    AlarmConstants.ACTION_DISMISS_ALARM -> dismissAlarm(context, notifId)
                    ACTION_MARK_DONE -> {
                        dismissAlarm(context, notifId)
                        ServiceLocator.taskRepository
                            .toggleTaskDone(taskId = taskId, isDone = true)
                            .onSuccess { Log.d(TAG, "Task $taskId marked as done.") }
                            .onFailure { e -> Log.e(TAG, "Failed to mark task $taskId as done: $e") }
                    }
                    else -> Log.w(TAG, "Unknown action: ${intent.action}")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun dismissAlarm(context: Context, notifId: Int) {
        context.stopService(Intent(context, AlarmService::class.java))
        if (notifId != -1) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(notifId)
        }
    }

    companion object {
        /** Intent action for the "Mark as Done" notification action. */
        const val ACTION_MARK_DONE = "com.example.todoapp.ACTION_MARK_DONE"

        private const val TAG = "DismissAlarmReceiver"
    }
}
