// File: presentation/task/TaskViewModel.kt
package com.example.todoapp.presentation.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.alarm.AlarmScheduler
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.repository.AuthRepository
import com.example.todoapp.domain.repository.TaskRepository
import com.example.todoapp.notification.DailySummaryNotificationHelper
import com.example.todoapp.widget.TodoWidgetReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel for the Home screen (task list) and Add/Edit Task screen.
 *
 * Responsibilities:
 *  - Exposes the reactive task list to the Home screen via [tasks].
 *  - Manages the Add/Edit form state via [taskUiState].
 *  - Orchestrates alarm scheduling/cancellation alongside task writes.
 *  - Triggers widget and daily-summary notification refreshes after list changes.
 *
 * Uses [AndroidViewModel] to access [Application] context for widget and
 * notification side effects. Business logic is delegated to [TaskRepository].
 *
 * @param application    Android Application — for widget/notification context.
 * @param taskRepo       Domain task repository (offline-first Room SSOT).
 * @param authRepo       Domain auth repository — for current user UID.
 * @param alarmScheduler Platform alarm scheduler for exact alarms.
 */
class TaskViewModel(
    application: Application,
    private val taskRepo: TaskRepository,
    private val authRepo: AuthRepository,
    private val alarmScheduler: AlarmScheduler,
) : AndroidViewModel(application) {

    // ── State ─────────────────────────────────────────────────────────────────

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _taskUiState = MutableStateFlow<TaskUiState>(TaskUiState.Idle)
    val taskUiState: StateFlow<TaskUiState> = _taskUiState.asStateFlow()

    // ── Load task list ────────────────────────────────────────────────────────

    /**
     * Starts collecting the reactive task list for [userId] from Room.
     * Emits immediately from local cache and re-emits on every change
     * (including changes pushed by the background sync worker).
     * Also syncs the widget and daily notification on each emission.
     */
    fun loadTasksForUser(userId: String) {
        viewModelScope.launch {
            taskRepo.observeTasksForUser(userId).collect { list ->
                _tasks.value = list
                syncWidget(list)
                syncDailySummary(list)
            }
        }
    }

    // ── Load single task for editing ──────────────────────────────────────────

    /**
     * Loads a task by [taskId] and transitions [taskUiState] to [TaskUiState.Editing].
     */
    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _taskUiState.value = TaskUiState.Loading
            val task = taskRepo.getTaskById(taskId)
            _taskUiState.value = if (task != null) {
                TaskUiState.Editing(task)
            } else {
                TaskUiState.Error("Task not found")
            }
        }
    }

    fun clearTaskForm() {
        _taskUiState.value = TaskUiState.Idle
    }

    // ── Add ───────────────────────────────────────────────────────────────────

    /**
     * Creates a new task, persists it to Room, and schedules an alarm if requested.
     * The alarm is scheduled after [taskRepo.addTask] so we have the final task ID.
     */
    fun addTask(
        title: String,
        description: String,
        dueDate: String,
        dueTime: String,
        setAlarm: Boolean = false,
    ) {
        val uid = authRepo.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            _taskUiState.value = TaskUiState.Loading
            val shouldSetAlarm = setAlarm && dueDate.isNotBlank() && dueTime.isNotBlank()
            val task = Task(
                title       = title.trim(),
                description = description.trim(),
                dueDate     = dueDate,
                dueTime     = dueTime,
                userId      = uid,
                alarmSet    = shouldSetAlarm,
            )
            taskRepo.addTask(task)
                .onSuccess { newId ->
                    if (shouldSetAlarm) {
                        alarmScheduler.scheduleExactAlarm(
                            taskId    = newId,
                            taskTitle = title,
                            dueDate   = dueDate,
                            dueTime   = dueTime,
                        )
                    }
                    _taskUiState.value = TaskUiState.Saved
                }
                .onFailure {
                    _taskUiState.value = TaskUiState.Error(it.message ?: "Failed to add task")
                }
        }
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Updates an existing task, cancels any previous alarm, and re-schedules
     * if the alarm flag is still active.
     */
    fun updateTask(task: Task) {
        viewModelScope.launch {
            _taskUiState.value = TaskUiState.Loading
            // Always cancel the previous alarm — safe even if none was scheduled.
            alarmScheduler.cancelAlarm(task.id)
            if (task.alarmSet && task.dueDate.isNotBlank() && task.dueTime.isNotBlank()) {
                alarmScheduler.scheduleExactAlarm(
                    taskId    = task.id,
                    taskTitle = task.title,
                    dueDate   = task.dueDate,
                    dueTime   = task.dueTime,
                )
            }
            taskRepo.updateTask(task)
                .onSuccess { _taskUiState.value = TaskUiState.Saved }
                .onFailure { _taskUiState.value = TaskUiState.Error(it.message ?: "Failed to update task") }
        }
    }

    // ── Toggle done ───────────────────────────────────────────────────────────

    /**
     * Optimistically toggles [Task.isDone] in [_tasks] for instant UI feedback,
     * then writes the change to Room (which will sync in the background).
     */
    fun toggleDone(task: Task) {
        viewModelScope.launch {
            val newIsDone = !task.isDone
            taskRepo.toggleTaskDone(task.id, newIsDone)
                .onSuccess {
                    // Optimistic update — Room's Flow will confirm it shortly
                    val updated = _tasks.value.map {
                        if (it.id == task.id) it.copy(isDone = newIsDone) else it
                    }
                    _tasks.value = updated
                    syncWidget(updated)
                    syncDailySummary(updated)
                }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /**
     * Cancels any scheduled alarm for [taskId] and soft-deletes it from Room.
     * The sync worker will delete the remote document on the next sync.
     */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            alarmScheduler.cancelAlarm(taskId)
            taskRepo.deleteTask(taskId)
        }
    }

    fun resetState() {
        _taskUiState.value = TaskUiState.Idle
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Persists today's tasks to the Glance widget DataStore and triggers a redraw. */
    private fun syncWidget(tasks: List<Task>) {
        TodoWidgetReceiver.syncAndUpdate(getApplication(), tasks)
    }

    /**
     * Updates the daily summary notification with today's pending tasks.
     * Called on every task list emission so the notification stays accurate.
     */
    private fun syncDailySummary(tasks: List<Task>) {
        val today       = LocalDate.now().toString()   // "YYYY-MM-DD"
        val todayPending = tasks.filter { it.dueDate == today && !it.isDone }
        DailySummaryNotificationHelper.updateDailySummary(getApplication(), todayPending)
    }
}
