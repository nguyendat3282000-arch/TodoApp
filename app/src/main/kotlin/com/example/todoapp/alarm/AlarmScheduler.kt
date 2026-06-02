// File: alarm/AlarmScheduler.kt
package com.example.todoapp.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

// ══════════════════════════════════════════════════════════════════════════════
//  Interface
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Contract for scheduling and cancelling exact alarms for tasks.
 *
 * Lives in the alarm package (infrastructure layer). The domain layer does NOT
 * depend on this interface — it is used only from [com.example.todoapp.presentation.task.TaskViewModel].
 */
interface AlarmScheduler {
    /**
     * Schedule an exact alarm for [taskId] at [dueDate] + [dueTime].
     *
     * @return `true` if the alarm was successfully scheduled, `false` otherwise
     *         (e.g. the permission is not granted on API 31-32, or the time is in the past).
     */
    fun scheduleExactAlarm(
        taskId: String,
        taskTitle: String,
        dueDate: String,   // "YYYY-MM-DD"
        dueTime: String,   // "HH:mm"
    ): Boolean

    /** Cancel a previously scheduled alarm for [taskId]. No-op if not scheduled. */
    fun cancelAlarm(taskId: String)
}

// ══════════════════════════════════════════════════════════════════════════════
//  Implementation
// ══════════════════════════════════════════════════════════════════════════════

/**
 * [AlarmScheduler] implementation backed by [AlarmManager].
 *
 * Uses [AlarmManager.setExactAndAllowWhileIdle] for reliable delivery even in
 * Doze mode. All intent actions and extra keys come from [AlarmConstants] to
 * prevent duplication and mismatches.
 */
class AlarmSchedulerImpl(private val context: Context) : AlarmScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleExactAlarm(
        taskId: String,
        taskTitle: String,
        dueDate: String,
        dueTime: String,
    ): Boolean {
        val triggerAtMs = parseTriggerMillis(dueDate, dueTime) ?: run {
            Log.w(TAG, "Invalid dueDate/dueTime: $dueDate $dueTime")
            return false
        }

        if (triggerAtMs <= System.currentTimeMillis()) {
            Log.w(TAG, "Alarm time is in the past for task $taskId — skipping.")
            return false
        }

        // API 31-32: SCHEDULE_EXACT_ALARM requires user toggle in Settings.
        // API 33+:   USE_EXACT_ALARM declared in manifest → always granted.
        if (Build.VERSION.SDK_INT in 31..32 && !alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "canScheduleExactAlarms() = false. Alarm NOT scheduled for $taskId.")
            return false
        }

        val pendingIntent = buildAlarmPendingIntent(taskId, taskTitle) ?: run {
            Log.e(TAG, "Failed to build PendingIntent for task $taskId.")
            return false
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMs,
            pendingIntent,
        )

        Log.d(TAG, "Alarm scheduled: task=$taskId at $dueDate $dueTime (epoch=$triggerAtMs)")
        return true
    }

    override fun cancelAlarm(taskId: String) {
        val pendingIntent = buildAlarmPendingIntent(
            taskId    = taskId,
            taskTitle = "",
            flags     = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: run {
            Log.d(TAG, "No existing alarm PendingIntent for task $taskId.")
            return
        }
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Alarm cancelled for task $taskId.")
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun buildAlarmPendingIntent(
        taskId: String,
        taskTitle: String,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    ): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_ALARM_TRIGGERED
            putExtra(AlarmConstants.EXTRA_TASK_ID, taskId)
            putExtra(AlarmConstants.EXTRA_TASK_TITLE, taskTitle)
        }
        // Use taskId.hashCode() as the request code — each task gets a unique PendingIntent.
        return PendingIntent.getBroadcast(context, taskId.hashCode(), intent, flags)
    }

    /**
     * Converts "YYYY-MM-DD" + "HH:mm" into epoch milliseconds in the
     * device's default time zone. Returns null if either string is malformed.
     */
    private fun parseTriggerMillis(dueDate: String, dueTime: String): Long? = try {
        LocalDate.parse(dueDate)
            .atTime(LocalTime.parse(dueTime))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (e: DateTimeParseException) {
        Log.e(TAG, "parseTriggerMillis failed: $e")
        null
    }

    companion object {
        private const val TAG = "AlarmScheduler"
    }
}
