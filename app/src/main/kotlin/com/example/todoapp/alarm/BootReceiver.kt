// File: alarm/BootReceiver.kt
package com.example.todoapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.todoapp.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Re-schedules all pending alarms after device reboot.
 *
 * [AlarmManager] clears all alarms when the device powers off.
 * This receiver listens for [Intent.ACTION_BOOT_COMPLETED] and
 * `LOCKED_BOOT_COMPLETED`, then re-schedules every task whose
 * [com.example.todoapp.domain.model.Task.alarmSet] flag is true.
 *
 * ## Async handling
 * [goAsync] is called immediately in [onReceive] to extend the broadcast
 * window beyond the default 10 seconds. The [PendingResult.finish] call in
 * the `finally` block signals to the system that processing is complete.
 *
 * Registered in AndroidManifest.xml with RECEIVE_BOOT_COMPLETED permission.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) return

        Log.d(TAG, "Boot completed — re-scheduling alarms")

        val pendingResult  = goAsync()
        val taskRepo       = ServiceLocator.taskRepository
        val alarmScheduler = AlarmSchedulerImpl(context)

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                // .first() collects only the first emission from the snapshot Flow
                // and then cancels the collector, preventing a persistent listener.
                val tasks = taskRepo.observeAlarmSetTasks().first()
                tasks.forEach { task ->
                    val scheduled = alarmScheduler.scheduleExactAlarm(
                        taskId    = task.id,
                        taskTitle = task.title,
                        dueDate   = task.dueDate,
                        dueTime   = task.dueTime,
                    )
                    Log.d(TAG, "Re-scheduled task ${task.id}: $scheduled")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to re-schedule alarms on boot: $e")
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
