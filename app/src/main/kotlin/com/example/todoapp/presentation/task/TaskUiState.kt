// File: presentation/task/TaskUiState.kt
package com.example.todoapp.presentation.task

import com.example.todoapp.domain.model.Task

/**
 * Represents all possible UI states for the Add/Edit Task screen.
 *
 * Used by [TaskViewModel] to drive the Compose UI reactively.
 */
sealed class TaskUiState {
    /** Initial state — form is blank/ready. */
    object Idle : TaskUiState()

    /** A save/load operation is in progress. */
    object Loading : TaskUiState()

    /** A task was saved successfully. The screen should navigate back. */
    object Saved : TaskUiState()

    /**
     * The form is populated with an existing task ready to be edited.
     * @property task The current state of the task being edited.
     */
    data class Editing(val task: Task) : TaskUiState()

    /**
     * An error occurred.
     * @property message Human-readable error message.
     */
    data class Error(val message: String) : TaskUiState()
}
