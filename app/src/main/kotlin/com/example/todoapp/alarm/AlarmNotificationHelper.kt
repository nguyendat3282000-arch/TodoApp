// File: alarm/AlarmNotificationHelper.kt
package com.example.todoapp.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.todoapp.R
import com.example.todoapp.ui.screens.alarm.AlarmActivity

/**
 * Single source of truth for alarm notifications.
 *
 * Responsibilities:
 *  - Create the alarm [NotificationChannel] (call once from Application.onCreate).
 *  - Build the alarm [Notification] with a [FullScreenIntent] to [AlarmActivity].
 *  - Build action PendingIntents ("Dismiss" and "Mark as Done") → [DismissAlarmReceiver].
 *
 * All extra keys come from [AlarmConstants] — no local constant duplication.
 */
object AlarmNotificationHelper {

    const val CHANNEL_ID        = "todo_alarm_channel"
    const val CHANNEL_NAME      = "Todo Alarms"

    /**
     * Base value for notification IDs.
     * Actual ID = NOTIFICATION_BASE_ID + taskId.hashCode() (computed in [AlarmService]).
     */
    const val NOTIFICATION_BASE_ID = 7000

    // ── Channel ───────────────────────────────────────────────────────────────

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description          = "Notifications for scheduled task alarms"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(false)   // vibration is managed by AlarmService
            setBypassDnd(true)
            setShowBadge(true)
        }
        nm.createNotificationChannel(channel)
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    /**
     * Builds a high-priority alarm notification with a FullScreenIntent and
     * two notification actions.
     *
     * @param context    Application or Service context.
     * @param taskId     Unique task identifier.
     * @param taskTitle  Title shown in the notification.
     * @param notifId    Notification ID — same value used to post and later cancel.
     */
    fun buildAlarmNotification(
        context: Context,
        taskId: String,
        taskTitle: String,
        notifId: Int,
    ): Notification {

        // ── FullScreenIntent ──────────────────────────────────────────────────
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmConstants.EXTRA_TASK_ID,    taskId)
            putExtra(AlarmConstants.EXTRA_TASK_TITLE, taskTitle)
            putExtra(AlarmConstants.EXTRA_NOTIF_ID,   notifId)
        }
        val fullScreenPi = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // ── "Dismiss" action ──────────────────────────────────────────────────
        val dismissPi = buildActionIntent(
            context, taskId, notifId,
            action  = AlarmConstants.ACTION_DISMISS_ALARM,
            reqCode = taskId.hashCode() + 1,
        )

        // ── "Mark as Done" action ─────────────────────────────────────────────
        val markDonePi = buildActionIntent(
            context, taskId, notifId,
            action  = DismissAlarmReceiver.ACTION_MARK_DONE,
            reqCode = taskId.hashCode() + 2,
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_notification)
            .setContentTitle("⏰  $taskTitle")
            .setContentText("Your task is due! Tap to open.")
            .setSubText("TodoApp Alarm")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPi, true)
            .setContentIntent(fullScreenPi)
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_dismiss, "Dismiss", dismissPi,
                ).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_check, "Mark as Done", markDonePi,
                ).build()
            )
            .build()
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun buildActionIntent(
        context: Context,
        taskId: String,
        notifId: Int,
        action: String,
        reqCode: Int,
    ): PendingIntent {
        val intent = Intent(context, DismissAlarmReceiver::class.java).apply {
            this.action = action
            putExtra(AlarmConstants.EXTRA_TASK_ID,  taskId)
            putExtra(AlarmConstants.EXTRA_NOTIF_ID, notifId)
        }
        return PendingIntent.getBroadcast(
            context, reqCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
