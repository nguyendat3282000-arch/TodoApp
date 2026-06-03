// File: Navigation.kt
package com.example.todoapp

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.todoapp.alarm.AlarmSchedulerImpl
import com.example.todoapp.presentation.ViewModelFactory
import com.example.todoapp.presentation.auth.AuthViewModel
import com.example.todoapp.presentation.task.TaskViewModel
import com.example.todoapp.ui.screens.auth.AuthScreen
import com.example.todoapp.ui.screens.home.HomeScreen
import com.example.todoapp.ui.screens.task.AddEditTaskScreen
import kotlinx.serialization.Serializable

// ══════════════════════════════════════════════════════════════════════════════
//  Type-safe navigation destinations — must implement NavKey
// ══════════════════════════════════════════════════════════════════════════════

@Serializable
data object AuthDest : NavKey

@Serializable
data object HomeDest : NavKey

@Serializable
data class AddEditTaskDest(val taskId: String? = null) : NavKey

@Serializable
data object SettingsDest : NavKey

// ══════════════════════════════════════════════════════════════════════════════
//  NavGraph
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun TodoNavGraph(
    authViewModel: AuthViewModel,
    onGoogleSignIn: () -> Unit,
) {
    val initialUser = remember {
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.let {
            com.example.todoapp.domain.model.AuthUser(
                uid = it.uid,
                email = it.email,
                displayName = it.displayName
            )
        }
    }
    val currentUser by authViewModel.currentUser.collectAsState(initial = initialUser)

    val startDest: NavKey = if (currentUser != null) HomeDest else AuthDest
    val backStack = rememberNavBackStack(startDest)

    // Reactively re-route on auth state changes
    LaunchedEffect(currentUser) {
        val last = backStack.lastOrNull()
        if (currentUser != null && last is AuthDest) {
            backStack.clear()
            backStack.add(HomeDest)
        } else if (currentUser == null && last !is AuthDest) {
            backStack.clear()
            backStack.add(AuthDest)
        }
    }

    val application = LocalContext.current.applicationContext as Application
    val factory = ViewModelFactory(
        application    = application,
        alarmScheduler = AlarmSchedulerImpl(application),
    )
    val taskViewModel: TaskViewModel = viewModel(factory = factory)

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid -> taskViewModel.loadTasksForUser(uid) }
    }

    NavDisplay(
        backStack = backStack,
        onBack    = { if (backStack.size > 1) backStack.removeLast() },
        entryProvider = entryProvider {

            entry<AuthDest> {
                AuthScreen(
                    viewModel      = authViewModel,
                    onAuthSuccess  = { backStack.add(HomeDest) },
                    onGoogleSignIn = onGoogleSignIn,
                )
            }

            entry<HomeDest> {
                HomeScreen(
                    viewModel  = taskViewModel,
                    onAddTask  = { backStack.add(AddEditTaskDest()) },
                    onEditTask = { taskId -> backStack.add(AddEditTaskDest(taskId)) },
                    onNavigateToSettings = { backStack.add(SettingsDest) },
                )
            }

            entry<AddEditTaskDest> { dest ->
                AddEditTaskScreen(
                    viewModel      = taskViewModel,
                    taskId         = dest.taskId,
                    onNavigateBack = { if (backStack.size > 1) backStack.removeLast() },
                )
            }

            entry<SettingsDest> {
                com.example.todoapp.ui.screens.settings.SettingsScreen(
                    viewModel      = taskViewModel,
                    onNavigateBack = { if (backStack.size > 1) backStack.removeLast() },
                    onSignOut      = {
                        authViewModel.signOut()
                        backStack.clear()
                        backStack.add(AuthDest)
                    }
                )
            }
        },
    )
}
