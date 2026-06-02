// File: ui/screens/alarm/AlarmActivity.kt
package com.example.todoapp.ui.screens.alarm

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoapp.alarm.AlarmConstants
import com.example.todoapp.alarm.AlarmService
import com.example.todoapp.di.ServiceLocator
import com.example.todoapp.notification.DailySummaryNotificationHelper
import com.example.todoapp.ui.theme.TodoTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Full-screen alarm Activity that wakes up the device and presents a
 * Baemin-style ringing UI.
 *
 * Key window flags set via modern WindowCompat API:
 *  - [setShowWhenLocked] → shows over the lock screen (API 27+)
 *  - [setTurnScreenOn]   → wakes the display (API 27+)
 *
 * Audio + vibration are owned by [AlarmService] (a foreground service).
 * This Activity handles only the UI and user interaction.
 *
 * Dismissing calls [dismissAlarm], which:
 *  1. Stops AlarmService (audio + vibration)
 *  2. Cancels the alarm notification
 *  3. Optionally marks the task done in Room (syncs to Firestore)
 *  4. Refreshes the daily summary notification
 *  5. Finishes this Activity
 */
class AlarmActivity : ComponentActivity() {

    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        enableEdgeToEdge()

        val taskId    = intent.getStringExtra(AlarmConstants.EXTRA_TASK_ID)    ?: "unknown"
        val taskTitle = intent.getStringExtra(AlarmConstants.EXTRA_TASK_TITLE) ?: "Task Due!"
        val notifId   = intent.getIntExtra(AlarmConstants.EXTRA_NOTIF_ID, -1)

        setContent {
            TodoTheme {
                AlarmScreen(
                    taskTitle  = taskTitle,
                    onDismiss  = { dismissAlarm(taskId, notifId, markDone = false) },
                    onMarkDone = { dismissAlarm(taskId, notifId, markDone = true) },
                )
            }
        }
    }

    // ── Dismiss logic ─────────────────────────────────────────────────────────

    private fun dismissAlarm(taskId: String, notifId: Int, markDone: Boolean) {
        stopService(Intent(this, AlarmService::class.java))

        if (notifId != -1) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(notifId)
        }

        activityScope.launch {
            if (markDone) {
                ServiceLocator.taskRepository.toggleTaskDone(taskId, isDone = true)
                    .onSuccess {
                        Log.d(TAG, "Task $taskId marked as done from AlarmActivity.")
                        // Refresh the daily summary with updated data from Room
                        val repo   = ServiceLocator.taskRepository
                        val userId = ServiceLocator.authRepository.getCurrentUser()?.uid
                        if (userId != null) {
                            val today  = LocalDate.now().toString()
                            val tasks  = repo.getTodayTasksOnce(userId, today)
                            val pending = tasks.filter { !it.isDone }
                            withContext(Dispatchers.Main) {
                                DailySummaryNotificationHelper.updateDailySummary(
                                    this@AlarmActivity, pending,
                                )
                            }
                        }
                    }
                    .onFailure { e -> Log.e(TAG, "Failed to mark task done: $e") }
            }
            withContext(Dispatchers.Main) { finish() }
        }
    }

    companion object {
        private const val TAG = "AlarmActivity"
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  Composable UI — Baemin-style cute alarm screen
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AlarmScreen(
    taskTitle: String,
    onDismiss: () -> Unit,
    onMarkDone: () -> Unit,
) {
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        pulseScale.animateTo(
            targetValue  = 1.18f,
            animationSpec = infiniteRepeatable(
                animation  = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        )
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF96E4CE),   // Mint300
            Color(0xFFFFB39E),   // Coral300
        )
    )

    Box(
        modifier          = Modifier.fillMaxSize().background(backgroundBrush),
        contentAlignment  = Alignment.Center,
    ) {
        Column(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.Center,
        ) {
            // ── Pulsing bell ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale.value)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "⏰", fontSize = 72.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text       = "Alarm! 🌸",
                fontSize   = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White,
                textAlign  = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Task title card ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.45f))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = taskTitle,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF272D34),
                    textAlign  = TextAlign.Center,
                    lineHeight = 28.sp,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ── Mark as Done ──────────────────────────────────────────────────
            Button(
                onClick  = onMarkDone,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(28.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2BBFA0),
                    contentColor   = Color.White,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            ) {
                Text(
                    text       = "✅  Mark as Done",
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Dismiss ───────────────────────────────────────────────────────
            OutlinedButton(
                onClick  = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(28.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border   = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
            ) {
                Text(
                    text       = "Dismiss",
                    fontSize   = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
