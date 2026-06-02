package com.example.todoapp.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.todoapp.MainActivity
import com.example.todoapp.R
import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════════
//  TodoWidget — Jetpack Glance App Widget
//
//  Data flow:
//   MainActivity / TaskViewModel
//     → calls WidgetDataStore.saveTasks() after every task list update
//     → calls TodoWidgetReceiver.requestWidgetUpdate()
//   TodoWidget.provideContent()
//     → reads WidgetDataStore.loadTasksOnce()  (instant, offline-safe)
//     → renders the Glance UI
//
//  IMPORTANT — Glance API surface differs from Compose:
//   • GlanceModifier  (not Modifier)
//   • androidx.glance.layout.* (not androidx.compose.foundation.layout.*)
//   • androidx.glance.text.Text (not androidx.compose.material3.Text)
//   • No Brush gradients; use solid ColorProvider for backgrounds.
//   • No rememberable state — Glance re-runs provideContent() on each update.
// ══════════════════════════════════════════════════════════════════════════════

class TodoWidget : GlanceAppWidget() {

    // Maximum tasks shown in the widget to avoid over-filling the home screen.
    private val MAX_TASKS = 5

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Read the DataStore cache — always fast, works offline.
        val allTasks   = WidgetDataStore.loadTasksOnce(context)
        val pending    = allTasks.filter { !it.isDone }.take(MAX_TASKS)
        val totalCount = allTasks.count { !it.isDone }

        provideContent {
            GlanceTheme {
                WidgetContent(
                    context    = context,
                    tasks      = pending,
                    totalCount = totalCount,
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
    context: Context,
    tasks: List<WidgetTask>,
    totalCount: Int,
) {
    // Soft mint background — Baemin pastel palette
    val bgColor       = ColorProvider(Color(0xFFE8FAF6))   // Mint100
    val headerBg      = ColorProvider(Color(0xFF2BBFA0))   // Mint500
    val cardBg        = ColorProvider(Color(0xFFFFFFFF))
    val textOnHeader  = ColorProvider(Color.White)
    val textPrimary   = ColorProvider(Color(0xFF272D34))   // Neutral800
    val textSecondary = ColorProvider(Color(0xFF636D78))   // Neutral600
    val accentColor   = ColorProvider(Color(0xFFFF6B4E))   // Coral500
    val doneBg        = ColorProvider(Color(0xFFC5F0E5))   // Mint200

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .padding(0.dp),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {

            // ── Header bar ────────────────────────────────────────────────────
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(headerBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier          = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Bell icon
                    Image(
                        provider    = ImageProvider(R.drawable.ic_alarm_notification),
                        contentDescription = "Tasks",
                        modifier    = GlanceModifier.size(18.dp),
                    )
                    Spacer(GlanceModifier.width(6.dp))
                    // Title
                    Text(
                        text  = "Today's Tasks",
                        style = TextStyle(
                            color      = textOnHeader,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    // Pending badge
                    if (totalCount > 0) {
                        Box(
                            modifier = GlanceModifier
                                .background(ColorProvider(Color(0xFFFF6B4E)))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text  = "$totalCount",
                                style = TextStyle(
                                    color      = textOnHeader,
                                    fontSize   = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign  = TextAlign.Center,
                                ),
                            )
                        }
                    }
                }
            }

            // ── Task list ─────────────────────────────────────────────────────
            if (tasks.isEmpty()) {
                EmptyState(
                    modifier   = GlanceModifier.fillMaxSize(),
                    bgColor    = bgColor,
                    textColor  = textSecondary,
                    context    = context,
                )
            } else {
                LazyColumn(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    items(tasks) { task ->
                        TaskRow(
                            task          = task,
                            cardBg        = cardBg,
                            doneBg        = doneBg,
                            textPrimary   = textPrimary,
                            textSecondary = textSecondary,
                            accentColor   = accentColor,
                        )
                        Spacer(GlanceModifier.height(5.dp))
                    }
                }
            }

            // ── "Open App" footer button ──────────────────────────────────────
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(headerBg)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = "＋  Open App",
                    style = TextStyle(
                        color      = textOnHeader,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                    ),
                )
            }
        }
    }
}

// ── Single task row ───────────────────────────────────────────────────────────

@Composable
private fun TaskRow(
    task: WidgetTask,
    cardBg: ColorProvider,
    doneBg: ColorProvider,
    textPrimary: ColorProvider,
    textSecondary: ColorProvider,
    accentColor: ColorProvider,
) {
    val rowBg = if (task.isDone) doneBg else cardBg

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(rowBg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier          = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Colored dot indicator
            Box(
                modifier = GlanceModifier
                    .size(8.dp)
                    .background(
                        if (task.isDone)
                            ColorProvider(Color(0xFF2BBFA0))   // Mint500
                        else
                            accentColor
                    ),
            ) {}

            Spacer(GlanceModifier.width(8.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text     = task.title,
                    maxLines = 1,
                    style    = TextStyle(
                        color      = textPrimary,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                if (task.dueTime.isNotBlank()) {
                    Spacer(GlanceModifier.height(1.dp))
                    Text(
                        text  = "⏰ ${task.dueTime}",
                        style = TextStyle(
                            color    = textSecondary,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }

            // Done checkmark
            if (task.isDone) {
                Text(
                    text  = "✓",
                    style = TextStyle(
                        color      = ColorProvider(Color(0xFF2BBFA0)),
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(
    modifier: GlanceModifier,
    bgColor: ColorProvider,
    textColor: ColorProvider,
    context: Context,
) {
    Box(
        modifier          = modifier.background(bgColor).padding(16.dp),
        contentAlignment  = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text  = "🌟",
                style = TextStyle(fontSize = 32.sp),
            )
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text  = "No tasks today!",
                style = TextStyle(
                    color      = textColor,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign  = TextAlign.Center,
                ),
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text  = "Tap below to add one ✨",
                style = TextStyle(
                    color     = textColor,
                    fontSize  = 11.sp,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}
