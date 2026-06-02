// File: notification/DailySummaryNotificationHelper.kt
package com.example.todoapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.todoapp.MainActivity
import com.example.todoapp.R
import com.example.todoapp.domain.model.Task

// ══════════════════════════════════════════════════════════════════════════════
//  DailySummaryNotificationHelper
//
//  Posts / updates a single persistent notification that summarises today's
//  pending tasks. It lives in the status bar all day and is automatically
//  cancelled when there are no pending tasks left.
//
//  Design:
//   - Channel:  todo_daily_summary  (IMPORTANCE_DEFAULT — not intrusive)
//   - Style:    BigTextStyle listing task titles, one per line
//   - Actions:  "Open App"  +  "✓ Mark All Done"
//   - ID:       Fixed NOTIFICATION_ID so updates replace the previous one
//
//  NOTE: This helper only DISPLAYS data it is given — it does NOT fetch from
//  the repository. The caller (TaskViewModel, DailySummaryReceiver) is
//  responsible for providing fresh task data.
// ══════════════════════════════════════════════════════════════════════════════

object DailySummaryNotificationHelper {

    const val CHANNEL_ID      = "todo_daily_summary"
    const val CHANNEL_NAME    = "Daily Task Summary"
    const val NOTIFICATION_ID = 8000   // fixed — always the same notification

    // ── Channel ───────────────────────────────────────────────────────────────

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Shows today's pending tasks in the status bar"
            setShowBadge(true)
        }
        nm.createNotificationChannel(channel)
    }

    // ── Post / update ─────────────────────────────────────────────────────────

    /**
     * Posts or updates the daily summary notification.
     *
     * The caller is responsible for filtering tasks to today's pending ones.
     * This method does NOT query the repository — it only displays what it receives.
     *
     * @param context      Application context.
     * @param pendingTasks Today's tasks that are NOT yet done.
     */
    fun updateDailySummary(context: Context, pendingTasks: List<Task>) {
        val appContext = context.applicationContext
        createChannel(appContext)

        val nm = NotificationManagerCompat.from(appContext)

        if (pendingTasks.isEmpty()) {
            nm.cancel(NOTIFICATION_ID)
            return
        }

        val count        = pendingTasks.size
        val contentTitle = if (count == 1) "1 task due today" else "$count tasks due today"
        val bigText      = pendingTasks.joinToString(separator = "\n") { task ->
            val time = if (task.dueTime.isNotBlank()) "  [${task.dueTime}]" else ""
            "• ${task.title}$time"
        }

        val openAppPi = PendingIntent.getActivity(
            appContext,
            NOTIFICATION_ID,
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val taskIds = ArrayList(pendingTasks.map { it.id })
        val markAllDonePi = PendingIntent.getBroadcast(
            appContext,
            NOTIFICATION_ID + 1,
            Intent(appContext, DailySummaryReceiver::class.java).apply {
                action = DailySummaryReceiver.ACTION_MARK_ALL_DONE
                putStringArrayListExtra(DailySummaryReceiver.EXTRA_TASK_IDS, taskIds)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_check)
            .setContentTitle(contentTitle)
            .setContentText(bigText.lines().first())
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
                    .setBigContentTitle(contentTitle)
                    .setSummaryText("Today's agenda"),
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(false)
            .setAutoCancel(false)
            .setContentIntent(openAppPi)
            .addAction(
                NotificationCompat.Action.Builder(R.drawable.ic_check, "Open App", openAppPi).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(R.drawable.ic_check, "✓ Mark All Done", markAllDonePi).build()
            )
            .build()

        try {
            nm.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            android.util.Log.w(TAG, "POST_NOTIFICATIONS not granted: $e")
        }
    }

    private const val TAG = "DailySummaryHelper"
}
