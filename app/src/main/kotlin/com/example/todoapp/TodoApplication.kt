// File: TodoApplication.kt
package com.example.todoapp

import android.app.Application
import com.example.todoapp.alarm.AlarmNotificationHelper
import com.example.todoapp.di.ServiceLocator
import com.example.todoapp.notification.DailySummaryNotificationHelper
import com.example.todoapp.sync.TaskSyncWorker

/**
 * Custom Application class.
 *
 * Responsibilities (in [onCreate] order):
 *  1. Initialize [ServiceLocator] with the application context — must happen
 *     before any property on ServiceLocator is accessed.
 *  2. Create notification channels (idempotent — safe every launch).
 *  3. Schedule the periodic background sync worker via [TaskSyncWorker].
 *
 * No `taskRepository` property is exposed here; receivers and services that
 * need it use [ServiceLocator.taskRepository] directly.
 */
class TodoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. DI graph — must be first
        ServiceLocator.init(this)

        // 2. Notification channels (API 26+ requirement)
        AlarmNotificationHelper.createNotificationChannel(this)
        DailySummaryNotificationHelper.createChannel(this)

        // 3. Periodic sync (ExistingPeriodicWorkPolicy.KEEP — won't reset if already running)
        TaskSyncWorker.schedulePeriodicSync(this)
    }
}
