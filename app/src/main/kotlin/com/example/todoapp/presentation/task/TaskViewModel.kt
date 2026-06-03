// File: presentation/task/TaskViewModel.kt
package com.example.todoapp.presentation.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.alarm.AlarmScheduler
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.UserStats
import com.example.todoapp.domain.model.TaskLog
import com.example.todoapp.domain.model.TaskType
import com.example.todoapp.domain.model.FrequencyType
import com.example.todoapp.domain.model.FlexibleInterval
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

    private val _userStats = MutableStateFlow<UserStats?>(null)
    val userStats: StateFlow<UserStats?> = _userStats.asStateFlow()

    private val _weeklyTaskLogs = MutableStateFlow<List<TaskLog>>(emptyList())
    val weeklyTaskLogs: StateFlow<List<TaskLog>> = _weeklyTaskLogs.asStateFlow()

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
                val todayStr = LocalDate.now().toString()
                val tasksToReset = list.filter { it.type == TaskType.HABIT && it.isDone && it.lastCompletedDate != todayStr }
                
                if (tasksToReset.isNotEmpty()) {
                    tasksToReset.forEach { task ->
                        taskRepo.updateTask(task.copy(isDone = false))
                    }
                } else {
                    _tasks.value = list
                    syncWidget(list)
                    syncDailySummary(list)
                }
            }
        }
        loadUserStatsAndLogs(userId)
    }

    private fun loadUserStatsAndLogs(userId: String) {
        viewModelScope.launch {
            val stats = taskRepo.getUserStats(userId)
            if (stats == null) {
                taskRepo.saveUserStats(UserStats(
                    userId = userId,
                    healthScore = 100,
                    totalStreak = 0,
                    lastResetDate = LocalDate.now().toString()
                ))
            }
            taskRepo.observeUserStats(userId).collect {
                _userStats.value = it
            }
        }
        viewModelScope.launch {
            val today = LocalDate.now()
            val start = today.minusDays(6).toString()
            val end = today.toString()
            taskRepo.observeTaskLogsForPeriod(userId, start, end).collect {
                _weeklyTaskLogs.value = it
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
        type: TaskType = TaskType.DAILY,
        frequencyType: FrequencyType? = null,
        fixedDays: List<Int> = emptyList(),
        flexibleCount: Int = 0,
        flexibleInterval: FlexibleInterval? = null,
    ) {
        val uid = authRepo.getCurrentUser()?.uid ?: return

        viewModelScope.launch {
            _taskUiState.value = TaskUiState.Loading
            val shouldSetAlarm = setAlarm && ((type == TaskType.HABIT && dueTime.isNotBlank()) || (dueDate.isNotBlank() && dueTime.isNotBlank()))
            val task = Task(
                title       = title.trim(),
                description = description.trim(),
                dueDate     = if (type == TaskType.HABIT) "" else dueDate,
                dueTime     = dueTime,
                userId      = uid,
                alarmSet    = shouldSetAlarm,
                type        = type,
                frequencyType = frequencyType,
                fixedDays   = fixedDays,
                flexibleCount = flexibleCount,
                flexibleInterval = flexibleInterval,
            )
            taskRepo.addTask(task)
                .onSuccess { newId ->
                    if (shouldSetAlarm) {
                        val finalDueDate = if (type == TaskType.HABIT) {
                            val today = LocalDate.now()
                            val time = java.time.LocalTime.parse(dueTime)
                            if (time.isBefore(java.time.LocalTime.now())) today.plusDays(1).toString() else today.toString()
                        } else dueDate

                        alarmScheduler.scheduleExactAlarm(
                            taskId    = newId,
                            taskTitle = title,
                            dueDate   = finalDueDate,
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

            val shouldSetAlarm = task.alarmSet && ((task.type == TaskType.HABIT && task.dueTime.isNotBlank()) || (task.dueDate.isNotBlank() && task.dueTime.isNotBlank()))
            if (shouldSetAlarm) {
                val finalDueDate = if (task.type == TaskType.HABIT) {
                    val today = LocalDate.now()
                    val time = java.time.LocalTime.parse(task.dueTime)
                    if (time.isBefore(java.time.LocalTime.now())) today.plusDays(1).toString() else today.toString()
                } else task.dueDate

                alarmScheduler.scheduleExactAlarm(
                    taskId    = task.id,
                    taskTitle = task.title,
                    dueDate   = finalDueDate,
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
            val userId = task.userId
            val todayStr = LocalDate.now().toString()

            if (newIsDone) {
                val pointsGained = when (task.type) {
                    TaskType.DAILY -> 5
                    TaskType.HABIT -> {
                        val yesterdayStr = LocalDate.now().minusDays(1).toString()
                        val newStreak = when (task.lastCompletedDate) {
                            yesterdayStr -> task.streak + 1
                            todayStr -> task.streak
                            else -> 1
                        }
                        val bonus = if (newStreak > 0 && newStreak % 5 == 0) 10 else 0
                        val totalHabitPoints = 10 + bonus

                        taskRepo.updateTask(task.copy(
                            lastCompletedDate = todayStr,
                            streak = newStreak,
                            isDone = true
                        ))
                        totalHabitPoints
                    }
                }

                val logId = java.util.UUID.randomUUID().toString()
                taskRepo.addTaskLog(TaskLog(
                    id = logId,
                    taskId = task.id,
                    userId = userId,
                    completedDate = todayStr,
                    pointsEarned = pointsGained
                ))

                val currentStats = taskRepo.getUserStats(userId) ?: UserStats(userId = userId)
                val newScore = minOf(100, currentStats.healthScore + pointsGained)
                taskRepo.saveUserStats(currentStats.copy(healthScore = newScore))
            } else {
                val taskLogs = taskRepo.getTaskLogsForPeriod(userId, todayStr, todayStr)
                    .filter { it.taskId == task.id }
                
                var pointsLost = 0
                for (log in taskLogs) {
                    pointsLost += log.pointsEarned
                }
                taskRepo.deleteTaskLogForDate(task.id, todayStr)

                if (task.type == TaskType.HABIT) {
                    val yesterdayStr = LocalDate.now().minusDays(1).toString()
                    taskRepo.updateTask(task.copy(
                        lastCompletedDate = yesterdayStr,
                        streak = maxOf(0, task.streak - 1),
                        isDone = false
                    ))
                }

                val currentStats = taskRepo.getUserStats(userId) ?: UserStats(userId = userId)
                val newScore = maxOf(0, currentStats.healthScore - pointsLost)
                taskRepo.saveUserStats(currentStats.copy(healthScore = newScore))
            }

            taskRepo.toggleTaskDone(task.id, newIsDone)
                .onSuccess {
                    val updated = _tasks.value.map {
                        if (it.id == task.id) it.copy(isDone = newIsDone) else it
                    }
                    _tasks.value = updated
                    syncWidget(updated)
                    syncDailySummary(updated)
                }
        }
    }

    // ── Suggestions ──────────────────────────────────────────────────────────
    val taskSuggestions = listOf(
        "Đọc sách 📚",
        "Tập Gym 🏋️",
        "Uống nước 💧",
        "Học tiếng Anh 🇬🇧",
        "Thiền định 🧘",
        "Check email 📧",
        "Lập kế hoạch tuần 📅"
    )

    val durationSuggestions = listOf(
        "15 phút",
        "30 phút",
        "1 giờ",
        "2 giờ"
    )

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

    fun resetUserStats() {
        val uid = authRepo.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            taskRepo.saveUserStats(UserStats(
                userId = uid,
                healthScore = 100,
                totalStreak = 0,
                lastResetDate = LocalDate.now().toString()
            ))
        }
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
