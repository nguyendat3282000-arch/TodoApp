// File: presentation/ViewModelFactory.kt
package com.example.todoapp.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todoapp.alarm.AlarmScheduler
import com.example.todoapp.di.ServiceLocator
import com.example.todoapp.presentation.auth.AuthViewModel
import com.example.todoapp.presentation.task.TaskViewModel

/**
 * Manual ViewModelProvider.Factory that wires domain repositories from
 * [ServiceLocator] into the ViewModel constructors.
 *
 * This is the **only** location in the app where ViewModel constructors
 * are called with concrete dependencies. All ViewModels depend on interfaces
 * from the domain layer, not on concrete implementations.
 *
 * @param application    Android Application — needed by [TaskViewModel] for
 *                       widget and notification side effects.
 * @param alarmScheduler Platform alarm scheduler — needed by [TaskViewModel].
 */
class ViewModelFactory(
    private val application: Application,
    private val alarmScheduler: AlarmScheduler,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
            AuthViewModel(
                authRepo = ServiceLocator.authRepository,
            ) as T

        modelClass.isAssignableFrom(TaskViewModel::class.java) ->
            TaskViewModel(
                application    = application,
                taskRepo       = ServiceLocator.taskRepository,
                authRepo       = ServiceLocator.authRepository,
                alarmScheduler = alarmScheduler,
            ) as T

        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
