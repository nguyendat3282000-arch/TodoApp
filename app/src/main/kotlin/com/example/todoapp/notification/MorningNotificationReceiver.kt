// File: notification/MorningNotificationReceiver.kt
package com.example.todoapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.todoapp.MainActivity
import com.example.todoapp.R

/**
 * BroadcastReceiver triggered early morning by AlarmManager to send a motivational quote.
 * After posting the notification, it automatically re-schedules itself for the next day.
 */
class MorningNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Morning motivation routine alarm fired!")

        val quotes = listOf(
            "Một ngày mới bắt đầu, hãy biến hôm nay trở nên tuyệt vời! ☀️",
            "Mỗi ngày là một cơ hội để tiến gần hơn tới mục tiêu của bạn. 🌟",
            "Đừng chờ đợi cơ hội, hãy tự tạo ra nó! 💪",
            "Kỷ luật là cầu nối giữa mục tiêu và thành tựu. 🎯",
            "Hãy làm những việc nhỏ bé với một tình yêu lớn lao. ❤️",
            "Mọi nỗ lực hôm nay sẽ là trái ngọt của ngày mai. 🍇",
            "Bắt đầu ngày mới với nụ cười và kế hoạch rõ ràng! 😊"
        )
        val quote = quotes.random()

        val appContext = context.applicationContext
        createChannel(appContext)

        val openAppPi = PendingIntent.getActivity(
            appContext,
            NOTIFICATION_ID,
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_notification) // using existing icon
            .setContentTitle("Chào buổi sáng! ☀️")
            .setContentText(quote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(quote))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPi)
            .build()

        val nm = NotificationManagerCompat.from(appContext)
        try {
            nm.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Missing POST_NOTIFICATIONS permission", e)
        }

        // Re-schedule for tomorrow at the same time
        val prefs = appContext.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val hour = prefs.getInt("morning_hour", 7)
        val minute = prefs.getInt("morning_minute", 0)
        scheduleNotification(appContext, hour, minute)
    }

    companion object {
        private const val TAG = "MorningNotifReceiver"
        const val CHANNEL_ID = "todo_morning_routine"
        const val CHANNEL_NAME = "Morning Motivation"
        const val NOTIFICATION_ID = 9000

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) != null) return

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Gửi châm ngôn động lực đầu ngày và nhắc nhở đặt mục tiêu"
                setShowBadge(true)
            }
            nm.createNotificationChannel(channel)
        }

        fun scheduleNotification(context: Context, hour: Int, minute: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, MorningNotificationReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pi)

            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)

                // If scheduled time already passed today, set to tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pi
                )
            } else {
                alarmManager.setExact(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pi
                )
            }
            Log.d(TAG, "Scheduled morning motivation for: ${calendar.time}")
        }

        fun cancelNotification(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, MorningNotificationReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pi)
            Log.d(TAG, "Cancelled morning motivation routine.")
        }
    }
}
