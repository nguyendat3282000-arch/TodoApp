// File: di/ServiceLocator.kt
package com.example.todoapp.di

import android.content.Context
import com.example.todoapp.data.local.TodoDatabase
import com.example.todoapp.data.remote.FirestoreTaskSource
import com.example.todoapp.data.repository.AuthRepositoryImpl
import com.example.todoapp.data.repository.TaskRepositoryImpl
import com.example.todoapp.domain.repository.AuthRepository
import com.example.todoapp.domain.repository.TaskRepository

/**
 * Manual dependency injection graph.
 *
 * Because this project intentionally avoids Hilt/Dagger (per architecture
 * constraints), all singleton instances are created here using lazy delegation
 * for thread-safe, on-demand initialization.
 *
 * Call [init] exactly once from [com.example.todoapp.TodoApplication.onCreate]
 * before accessing any property.
 *
 * All exposed properties use their interface type (not the implementation) so
 * consumers depend on abstractions, not concretions.
 */
object ServiceLocator {

    @Volatile
    private var appContext: Context? = null

    /**
     * Must be called from Application.onCreate() before any other property is accessed.
     * Thread-safe — safe to call multiple times (subsequent calls are no-ops).
     */
    fun init(context: Context) {
        if (appContext == null) {
            synchronized(this) {
                if (appContext == null) {
                    appContext = context.applicationContext
                }
            }
        }
    }

    private fun requireContext(): Context =
        appContext ?: error("ServiceLocator.init(context) must be called before accessing dependencies.")

    // ── Shared infrastructure ─────────────────────────────────────────────────

    /**
     * Single Firestore data source instance shared between [taskRepository]
     * and [com.example.todoapp.sync.TaskSyncWorker] to avoid duplicate SDK instances.
     */
    val firestoreTaskSource: FirestoreTaskSource by lazy {
        FirestoreTaskSource()
    }

    // ── Repositories (exposed as interfaces) ──────────────────────────────────

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }

    val taskRepository: TaskRepository by lazy {
        val ctx = requireContext()
        TaskRepositoryImpl(
            dao          = TodoDatabase.getDatabase(ctx).taskDao,
            remoteSource = firestoreTaskSource,
            context      = ctx,
        )
    }
}
