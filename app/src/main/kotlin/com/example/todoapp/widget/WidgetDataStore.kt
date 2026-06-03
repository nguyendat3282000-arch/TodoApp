// File: widget/WidgetDataStore.kt
package com.example.todoapp.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// ══════════════════════════════════════════════════════════════════════════════
//  WidgetDataStore
//
//  Why DataStore instead of reading the Room DB directly in Glance?
//  ──────────────────────────────────────────────────────────────
//  Glance's provideGlance() runs on a background coroutine with a strict
//  time budget imposed by the HomeScreen launcher. Room uses its own
//  thread-pool for async queries; it is not safe to rely on a query completing
//  within Glance's execution window.
//
//  Solution — write-through cache:
//   1. TaskViewModel writes today's pending tasks here whenever the list changes.
//   2. TodoWidget reads from DataStore — instant, always offline-safe.
//   3. TodoWidgetReceiver.syncAndUpdate() triggers a widget redraw after each write.
//
//  Data format: a JSON-serialized List<WidgetTask> under a single Preferences key.
// ══════════════════════════════════════════════════════════════════════════════

// One DataStore instance per process — backed by "widget_task_cache.preferences_pb".
private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "widget_task_cache",
)

object WidgetDataStore {

    private val TASKS_JSON_KEY = stringPreferencesKey("today_tasks_json")
    private val HEALTH_SCORE_KEY = androidx.datastore.preferences.core.intPreferencesKey("health_score")
    private val STREAK_KEY = androidx.datastore.preferences.core.intPreferencesKey("streak")
    private val json = Json { ignoreUnknownKeys = true }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Persists [tasks] to DataStore as a JSON string.
     * Call from [com.example.todoapp.presentation.task.TaskViewModel] whenever
     * the task list for today changes.
     */
    suspend fun saveTasks(context: Context, tasks: List<WidgetTask>) {
        context.widgetDataStore.edit { prefs ->
            prefs[TASKS_JSON_KEY] = json.encodeToString(tasks)
        }
    }

    suspend fun saveStats(context: Context, score: Int, streak: Int) {
        context.widgetDataStore.edit { prefs ->
            prefs[HEALTH_SCORE_KEY] = score
            prefs[STREAK_KEY] = streak
        }
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns a [Flow] of the cached [WidgetTask] list.
     * Emits an empty list if no data has been written yet.
     */
    fun tasksFlow(context: Context): Flow<List<WidgetTask>> =
        context.widgetDataStore.data.map { prefs ->
            val rawJson = prefs[TASKS_JSON_KEY] ?: return@map emptyList()
            runCatching { json.decodeFromString<List<WidgetTask>>(rawJson) }
                .getOrDefault(emptyList())
        }

    /**
     * One-shot suspend read for use inside Glance's [provideGlance].
     * Takes only the first emission and returns immediately.
     */
    suspend fun loadTasksOnce(context: Context): List<WidgetTask> {
        var result = emptyList<WidgetTask>()
        context.widgetDataStore.data.collect { prefs ->
            val rawJson = prefs[TASKS_JSON_KEY] ?: return@collect
            result = runCatching { json.decodeFromString<List<WidgetTask>>(rawJson) }
                .getOrDefault(emptyList())
            return@collect   // take only the first emission
        }
        return result
    }

    suspend fun loadStatsOnce(context: Context): Pair<Int, Int> {
        var score = 100
        var streak = 0
        context.widgetDataStore.data.collect { prefs ->
            score = prefs[HEALTH_SCORE_KEY] ?: 100
            streak = prefs[STREAK_KEY] ?: 0
            return@collect
        }
        return Pair(score, streak)
    }
}
