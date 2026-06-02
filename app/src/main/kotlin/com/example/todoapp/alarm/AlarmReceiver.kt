// File: alarm/AlarmReceiver.kt
package com.example.todoapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * BroadcastReceiver that fires when an exact AlarmManager alarm goes off.
 *
 * Responsibilities:
 *  1. Extract task metadata from intent extras.
 *  2. Start [AlarmService] as a foreground service — it owns the ringtone,
 *     vibration, and the FullScreenIntent notification.
 *
 * Why start a Service instead of doing work here directly?
 *  - A BroadcastReceiver's onReceive() has a strict 10-second execution window.
 *  - Starting a foreground service lets us safely manage audio/vibration for
 *    an indefinite duration (until the user dismisses or marks as done).
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmConstants.ACTION_ALARM_TRIGGERED) {
            Log.w(TAG, "Unexpected action: ${intent.action}")
            return
        }

        val taskId    = intent.getStringExtra(AlarmConstants.EXTRA_TASK_ID)    ?: return
        val taskTitle = intent.getStringExtra(AlarmConstants.EXTRA_TASK_TITLE) ?: "Task due!"

        Log.d(TAG, "Alarm fired for task: $taskId ($taskTitle)")

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmConstants.EXTRA_TASK_ID, taskId)
            putExtra(AlarmConstants.EXTRA_TASK_TITLE, taskTitle)
        }

        // Must use startForegroundService on API 26+; the service calls
        // startForeground() within 5 seconds to avoid an ANR.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}
