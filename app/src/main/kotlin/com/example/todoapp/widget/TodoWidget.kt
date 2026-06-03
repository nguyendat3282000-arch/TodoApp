package com.example.todoapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.example.todoapp.MainActivity
import com.example.todoapp.data.mapper.toDomain

import androidx.glance.appwidget.SizeMode
import androidx.glance.LocalSize

class TodoWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    private val maxTasks = 5

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = com.example.todoapp.data.local.TodoDatabase.getDatabase(context)
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        val allTasks = if (userId != null) {
            db.taskDao.getTasksForUser(userId).map { it.toDomain() }
        } else {
            emptyList()
        }

        val stats = if (userId != null) {
            db.taskDao.getUserStats(userId)
        } else null

        val today = java.time.LocalDate.now().toString()
        val dayOfWeekVal = java.time.LocalDate.now().dayOfWeek.value
        val todayTasks = allTasks.filter { 
            if (it.type == com.example.todoapp.domain.model.TaskType.DAILY) {
                it.dueDate == today
            } else {
                if (it.frequencyType == com.example.todoapp.domain.model.FrequencyType.FIXED) {
                    it.fixedDays.contains(dayOfWeekVal)
                } else {
                    it.lastCompletedDate != today || it.isDone
                }
            }
        }

        // Show pending tasks first, then done tasks, up to maxTasks
        val pending = todayTasks
            .filter { !it.isDone }
            .take(maxTasks)
            .map { WidgetTask(id = it.id, title = it.title, dueTime = it.dueTime, isDone = it.isDone) }
        val doneTasks = todayTasks
            .filter { it.isDone }
            .take(maxOf(0, maxTasks - pending.size))
            .map { WidgetTask(id = it.id, title = it.title, dueTime = it.dueTime, isDone = it.isDone) }
            
        val displayTasks = pending + doneTasks
        val totalCount = todayTasks.count { !it.isDone }

        val healthScore = stats?.healthScore ?: 100
        val streak = stats?.totalStreak ?: 0

        provideContent {
            GlanceTheme {
                WidgetContent(
                    tasks       = displayTasks,
                    totalCount  = totalCount,
                    healthScore = healthScore,
                    streak      = streak
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  Glance Composable Tree
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun WidgetContent(
    tasks: List<WidgetTask>,
    totalCount: Int,
    healthScore: Int,
    streak: Int,
) {
    // Premium Health App Palette (Clean White/Gray with vibrant accents)
    val bgColor       = ColorProvider(Color(0xFFF7F9FA), Color(0xFF1C1C1E)) // Light/Dark root bg
    val surfaceColor  = ColorProvider(Color(0xFFFFFFFF), Color(0xFF2C2C2E)) // Card bg
    val textPrimary   = ColorProvider(Color(0xFF111111), Color(0xFFFFFFFF))
    val textSecondary = ColorProvider(Color(0xFF8E8E93), Color(0xFF98989E))
    
    val scoreColor    = ColorProvider(Color(0xFFFF3B30), Color(0xFFFF453A)) // Apple Red
    val streakColor   = ColorProvider(Color(0xFFFF9500), Color(0xFFFF9F0A)) // Orange
    val doneBg        = ColorProvider(Color(0xFFF2F2F7), Color(0xFF3A3A3C))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        val size = LocalSize.current
        val isCompactHeight = size.height < 150.dp
        
        Column(modifier = GlanceModifier.fillMaxSize()) {
            
            // ── Top Stats Header (Health & Streak) ────────────────────────────
            if (!isCompactHeight) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                // Health Card
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .background(surfaceColor)
                        .cornerRadius(16.dp)
                        .padding(10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("❤️", style = TextStyle(fontSize = 18.sp))
                        Spacer(GlanceModifier.width(6.dp))
                        Column {
                            Text("Sức khỏe", style = TextStyle(color = textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium))
                            Text("$healthScore", style = TextStyle(color = scoreColor, fontSize = 16.sp, fontWeight = FontWeight.Bold))
                        }
                    }
                }
                
                Spacer(GlanceModifier.width(8.dp))
                
                // Streak Card
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .background(surfaceColor)
                        .cornerRadius(16.dp)
                        .padding(10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", style = TextStyle(fontSize = 18.sp))
                        Spacer(GlanceModifier.width(6.dp))
                        Column {
                            Text("Chuỗi", style = TextStyle(color = textSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium))
                            Text("$streak ngày", style = TextStyle(color = streakColor, fontSize = 16.sp, fontWeight = FontWeight.Bold))
                        }
                    }
                }
                } // Close Row
            } // Close if (!isCompactHeight)

            // ── Section Title ──────────────────────────────────────────────────
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 8.dp),

                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nhiệm vụ hôm nay",
                    style = TextStyle(color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    modifier = GlanceModifier.defaultWeight()
                )
                if (totalCount > 0) {
                    Box(
                        modifier = GlanceModifier.background(scoreColor).cornerRadius(10.dp).padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$totalCount",
                            style = TextStyle(color = ColorProvider(Color.White, Color.White), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // ── Task list ─────────────────────────────────────────────────────
            if (tasks.isEmpty()) {
                EmptyState(
                    modifier   = GlanceModifier.fillMaxSize(),
                    surfaceColor = surfaceColor,
                    textColor  = textSecondary,
                )
            } else {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                ) {
                    items(tasks) { task ->
                        TaskRow(
                            task          = task,
                            cardBg        = surfaceColor,
                            doneBg        = doneBg,
                            textPrimary   = textPrimary,
                            textSecondary = textSecondary,
                        )
                        Spacer(GlanceModifier.height(6.dp))
                    }
                }
            }
        }
    }
}

// ── Single task row ───────────────────────────────────────────────────────────

@Composable
private fun TaskRow(
    task: WidgetTask,
    cardBg: androidx.glance.unit.ColorProvider,
    doneBg: androidx.glance.unit.ColorProvider,
    textPrimary: androidx.glance.unit.ColorProvider,
    textSecondary: androidx.glance.unit.ColorProvider,
) {
    val rowBg = if (task.isDone) doneBg else cardBg

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(rowBg)
            .cornerRadius(16.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier          = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Checkbox
            Box(
                modifier = GlanceModifier
                    .size(24.dp)
                    .background(if (task.isDone) ColorProvider(Color(0xFF34C759), Color(0xFF34C759)) else ColorProvider(Color.Transparent, Color.Transparent))
                    .cornerRadius(12.dp)
                    .clickable(
                        actionRunCallback<ToggleTaskCallback>(
                            actionParametersOf(ToggleTaskCallback.TaskIdKey to task.id)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isDone) {
                    Text("✓", style = TextStyle(color = ColorProvider(Color.White, Color.White), fontSize = 14.sp, fontWeight = FontWeight.Bold))
                } else {
                    Text("○", style = TextStyle(color = textSecondary, fontSize = 20.sp))
                }
            }

            Spacer(GlanceModifier.width(12.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text     = task.title,
                    maxLines = 1,
                    style    = TextStyle(
                        color      = if (task.isDone) textSecondary else textPrimary,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                    ),
                )
                if (task.dueTime.isNotBlank()) {
                    Spacer(GlanceModifier.height(2.dp))
                    Text(
                        text  = task.dueTime,
                        style = TextStyle(
                            color    = textSecondary,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(
    modifier: GlanceModifier,
    surfaceColor: androidx.glance.unit.ColorProvider,
    textColor: androidx.glance.unit.ColorProvider,
) {
    Box(
        modifier          = modifier.background(surfaceColor).cornerRadius(16.dp).padding(16.dp),
        contentAlignment  = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text  = "✨",
                style = TextStyle(fontSize = 28.sp),
            )
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text  = "Không có nhiệm vụ nào",
                style = TextStyle(
                    color      = textColor,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign  = TextAlign.Center,
                ),
            )
        }
    }
}

// ── Glance Action Callback for Toggle ────────────────────────────────────────

class ToggleTaskCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[TaskIdKey] ?: return
        val db = com.example.todoapp.data.local.TodoDatabase.getDatabase(context)
        val taskEntity = db.taskDao.getTaskById(taskId) ?: return
        val newIsDone = !taskEntity.isDone
        val userId = taskEntity.userId
        val todayStr = java.time.LocalDate.now().toString()

        if (newIsDone) {
            val pointsGained = if (taskEntity.type == "DAILY") {
                5
            } else {
                // HABIT
                val yesterdayStr = java.time.LocalDate.now().minusDays(1).toString()
                val newStreak = when (taskEntity.lastCompletedDate) {
                    yesterdayStr -> taskEntity.streak + 1
                    todayStr -> taskEntity.streak
                    else -> 1
                }
                val bonus = if (newStreak > 0 && newStreak % 5 == 0) 10 else 0
                val totalHabitPoints = 10 + bonus

                db.taskDao.insertOrUpdate(taskEntity.copy(
                    lastCompletedDate = todayStr,
                    streak = newStreak,
                    isDone = true,
                    syncPending = true,
                    updatedAt = System.currentTimeMillis()
                ))
                totalHabitPoints
            }

            // Save task log
            val logId = java.util.UUID.randomUUID().toString()
            db.taskDao.insertTaskLog(com.example.todoapp.data.local.entity.TaskLogEntity(
                id = logId,
                taskId = taskId,
                userId = userId,
                completedDate = todayStr,
                pointsEarned = pointsGained
            ))

            // Save user stats
            val currentStats = db.taskDao.getUserStats(userId) ?: com.example.todoapp.data.local.entity.UserStatsEntity(userId = userId, healthScore = 100, totalStreak = 0, lastResetDate = todayStr)
            val newScore = minOf(100, currentStats.healthScore + pointsGained)
            db.taskDao.insertOrUpdateUserStats(currentStats.copy(healthScore = newScore))
        } else {
            // Uncheck
            val taskLogs = db.taskDao.getTaskLogsForPeriod(userId, todayStr, todayStr)
                .filter { it.taskId == taskId }
            
            var pointsLost = 0
            for (log in taskLogs) {
                pointsLost += log.pointsEarned
            }
            db.taskDao.deleteTaskLogsForTask(taskId)

            if (taskEntity.type == "HABIT") {
                val yesterdayStr = java.time.LocalDate.now().minusDays(1).toString()
                db.taskDao.insertOrUpdate(taskEntity.copy(
                    lastCompletedDate = yesterdayStr,
                    streak = maxOf(0, taskEntity.streak - 1),
                    isDone = false,
                    syncPending = true,
                    updatedAt = System.currentTimeMillis()
                ))
            }

            // Save user stats
            val currentStats = db.taskDao.getUserStats(userId) ?: com.example.todoapp.data.local.entity.UserStatsEntity(userId = userId, healthScore = 100, totalStreak = 0, lastResetDate = todayStr)
            val newScore = maxOf(0, currentStats.healthScore - pointsLost)
            db.taskDao.insertOrUpdateUserStats(currentStats.copy(healthScore = newScore))
        }

        // Toggle task status
        db.taskDao.toggleTaskDone(taskId, newIsDone, System.currentTimeMillis())

        // Refresh and update widget datastore cache
        val tasks = db.taskDao.getTasksForUser(userId).map { it.toDomain() }
        TodoWidgetReceiver.syncAndUpdate(context, tasks)
    }

    companion object {
        val TaskIdKey = ActionParameters.Key<String>("taskId")
    }
}


