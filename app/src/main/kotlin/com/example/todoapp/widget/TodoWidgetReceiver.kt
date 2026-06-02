// File: widget/TodoWidgetReceiver.kt
package com.example.todoapp.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import com.example.todoapp.domain.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * [GlanceAppWidgetReceiver] for [TodoWidget].
 *
 * Responsibilities:
 *  1. Tells Glance which widget class to use via [glanceAppWidget].
 *  2. Handles [AppWidgetManager.ACTION_APPWIDGET_UPDATE] so the widget refreshes
 *     on the periodic update interval defined in todo_widget_info.xml.
 *  3. Exposes [syncAndUpdate] — called by [com.example.todoapp.presentation.task.TaskViewModel]
 *     after writing new task data to [WidgetDataStore] to trigger an immediate redraw.
 */
class TodoWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = TodoWidget()

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        refreshWidget(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            refreshWidget(context)
        }
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun refreshWidget(context: Context) {
        receiverScope.launch {
            try {
                TodoWidget().updateAll(context)
                Log.d(TAG, "Widget refreshed successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Widget refresh failed: $e")
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Companion — static helpers callable from TaskViewModel
    // ══════════════════════════════════════════════════════════════════════════

    companion object {
        private const val TAG = "TodoWidgetReceiver"

        /**
         * Custom broadcast action sent by TaskViewModel after writing new task
         * data to [WidgetDataStore] to trigger an immediate widget redraw.
         */
        const val ACTION_REFRESH_WIDGET = "com.example.todoapp.ACTION_REFRESH_WIDGET"

        /**
         * Filters [tasks] to today's tasks, persists them to [WidgetDataStore],
         * and triggers a widget redraw. Safe to call from any thread.
         *
         * @param context Application context.
         * @param tasks   Full task list for the current user (all dates).
         */
        fun syncAndUpdate(context: Context, tasks: List<Task>) {
            val appContext = context.applicationContext
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                try {
                    val today = LocalDate.now().toString()   // "YYYY-MM-DD"
                    val widgetTasks = tasks
                        .filter { it.dueDate == today }
                        .map { WidgetTask(id = it.id, title = it.title, dueTime = it.dueTime, isDone = it.isDone) }
                    WidgetDataStore.saveTasks(appContext, widgetTasks)
                    requestWidgetUpdate(appContext)
                } catch (e: Exception) {
                    Log.e(TAG, "syncAndUpdate failed: $e")
                }
            }
        }

        /**
         * Sends the [ACTION_REFRESH_WIDGET] broadcast, causing [onReceive] to
         * call [TodoWidget.updateAll]. Safe to call from any thread.
         */
        fun requestWidgetUpdate(context: Context) {
            val intent = Intent(context, TodoWidgetReceiver::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            context.sendBroadcast(intent)
            Log.d(TAG, "Widget update broadcast sent.")
        }
    }
}
