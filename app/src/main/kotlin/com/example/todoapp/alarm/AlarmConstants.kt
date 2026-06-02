// File: alarm/AlarmConstants.kt
package com.example.todoapp.alarm

/**
 * Central repository for all alarm-related Intent action strings and extra keys.
 *
 * Centralizing these constants here prevents accidental duplication or mismatch
 * between the sender (AlarmScheduler / AlarmReceiver) and the receivers
 * (AlarmActivity, DismissAlarmReceiver, AlarmService).
 */
object AlarmConstants {

    // ── Intent actions ────────────────────────────────────────────────────────

    /** Action sent by [AlarmReceiver] to launch [com.example.todoapp.ui.screens.alarm.AlarmActivity]. */
    const val ACTION_ALARM_TRIGGERED = "com.example.todoapp.ACTION_ALARM_TRIGGERED"

    /** Action sent by [DismissAlarmReceiver] to stop [AlarmService]. */
    const val ACTION_DISMISS_ALARM   = "com.example.todoapp.ACTION_DISMISS_ALARM"

    // ── Intent extras ─────────────────────────────────────────────────────────

    /** String extra: the unique ID of the task that triggered the alarm. */
    const val EXTRA_TASK_ID   = "extra_task_id"

    /** String extra: the title of the task shown in the alarm UI. */
    const val EXTRA_TASK_TITLE = "extra_task_title"

    /** Int extra: the notification ID associated with this alarm. */
    const val EXTRA_NOTIF_ID  = "extra_notif_id"
}
