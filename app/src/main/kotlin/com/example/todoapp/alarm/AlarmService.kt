// File: alarm/AlarmService.kt
package com.example.todoapp.alarm

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.todoapp.alarm.AlarmNotificationHelper.NOTIFICATION_BASE_ID

/**
 * Foreground Service that owns the alarm audio and vibration lifecycle.
 *
 * Posts a [FullScreenIntent] notification immediately on start, begins the
 * ringtone and vibration, and cleans up everything in [onDestroy].
 *
 * Stopped by [DismissAlarmReceiver] (or by [com.example.todoapp.ui.screens.alarm.AlarmActivity])
 * via an explicit [Context.stopService] call.
 */
class AlarmService : Service() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var notifId: Int = NOTIFICATION_BASE_ID

    override fun onBind(intent: Intent?): IBinder? = null  // not a bound service

    override fun onCreate() {
        super.onCreate()
        AlarmNotificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val taskId    = intent?.getStringExtra(AlarmConstants.EXTRA_TASK_ID)    ?: return START_NOT_STICKY
        val taskTitle = intent.getStringExtra(AlarmConstants.EXTRA_TASK_TITLE)  ?: "Task Due!"

        notifId = NOTIFICATION_BASE_ID + taskId.hashCode()

        val notification = AlarmNotificationHelper.buildAlarmNotification(
            context   = this,
            taskId    = taskId,
            taskTitle = taskTitle,
            notifId   = notifId,
        )
        startForeground(notifId, notification)

        startRingtone()
        startVibration()

        Log.d(TAG, "AlarmService started for task=$taskId, notifId=$notifId")
        return START_NOT_STICKY  // if killed, do NOT restart — the alarm has been missed
    }

    override fun onDestroy() {
        stopRingtone()
        stopVibration()
        Log.d(TAG, "AlarmService destroyed")
        super.onDestroy()
    }

    // ── Audio ─────────────────────────────────────────────────────────────────

    private fun startRingtone() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)?.also { rt ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    rt.isLooping    = true
                    rt.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    rt.streamType = AudioManager.STREAM_ALARM
                }
                rt.play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start ringtone: $e")
        }
    }

    private fun stopRingtone() {
        try {
            ringtone?.stop()
            ringtone = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop ringtone: $e")
        }
    }

    // ── Vibration ─────────────────────────────────────────────────────────────

    @Suppress("DEPRECATION")
    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0L, 800L, 400L)  // pause 0ms, vibrate 800ms, pause 400ms

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start vibration: $e")
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop vibration: $e")
        }
    }

    companion object {
        private const val TAG = "AlarmService"
    }
}
