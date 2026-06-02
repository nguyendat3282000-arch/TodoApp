// File: widget/WidgetTask.kt
package com.example.todoapp.widget

/**
 * Lightweight projection of a [com.example.todoapp.domain.model.Task] used
 * exclusively by the Glance widget layer.
 *
 * Using a separate data class (instead of the full domain Task) keeps the
 * widget layer thin and limits the JSON payload stored in [WidgetDataStore].
 */
@kotlinx.serialization.Serializable
data class WidgetTask(
    val id: String = "",
    val title: String = "",
    val dueTime: String = "",
    val isDone: Boolean = false,
)
