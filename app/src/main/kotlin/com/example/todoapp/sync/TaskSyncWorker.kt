// File: sync/TaskSyncWorker.kt
package com.example.todoapp.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.todoapp.data.local.TodoDatabase
import com.example.todoapp.data.mapper.toDomain
import com.example.todoapp.data.mapper.toEntity
import com.example.todoapp.data.remote.FirestoreTaskSource
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager worker that synchronizes the local Room database
 * with Firestore when the device has a network connection.
 *
 * Sync algorithm (three-phase):
 *  1. **Push** — Upload all locally-modified ([syncPending] = true) tasks to Firestore.
 *     - If [deletePending] = true, delete the remote document and hard-delete locally.
 *     - Otherwise, upsert the task to Firestore and clear [syncPending].
 *  2. **Pull** — Fetch the full task list for the current user from Firestore.
 *     Skip records that still have local pending changes to avoid clobbering them.
 *  3. **Clean** — Hard-delete any local records that no longer exist on Firestore
 *     and have no pending local changes (i.e., were deleted on another device).
 *
 * On failure, returns [Result.retry] so WorkManager re-tries with exponential backoff.
 */
class TaskSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: run {
            Log.d(TAG, "No authenticated user — skipping sync.")
            return Result.success()
        }

        val dao           = TodoDatabase.getDatabase(applicationContext).taskDao
        val remoteSource  = FirestoreTaskSource()   // Firestore is cheap to instantiate

        Log.d(TAG, "Starting sync for user $userId")

        return try {
            // ── Phase 1: Push local changes ───────────────────────────────────
            val pendingSyncTasks = dao.getPendingSyncTasks()
            for (entity in pendingSyncTasks) {
                if (entity.deletePending) {
                    try {
                        remoteSource.deleteTask(entity.id)
                        dao.hardDelete(entity.id)
                        Log.d(TAG, "Deleted task ${entity.id} from remote and local.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to delete task ${entity.id} remotely: $e")
                        // Continue with other tasks — don't abort the whole sync
                    }
                } else {
                    try {
                        remoteSource.upsertTask(entity.toDomain())
                        dao.insertOrUpdate(entity.copy(syncPending = false))
                        Log.d(TAG, "Pushed task ${entity.id} to remote.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to push task ${entity.id} remotely: $e")
                    }
                }
            }

            // ── Phase 2: Pull remote changes ──────────────────────────────────
            val remoteTasks = remoteSource.fetchTasksForUser(userId)
            val remoteEntities = remoteTasks.map { remoteTask ->
                val localEntity = dao.getTaskById(remoteTask.id)
                if (localEntity != null && localEntity.syncPending) {
                    // Local changes are in flight — preserve local state
                    localEntity
                } else {
                    remoteTask.toEntity(syncPending = false)
                }
            }
            dao.insertOrUpdateAll(remoteEntities)

            // ── Phase 3: Clean up orphaned local records ───────────────────────
            val remoteIds   = remoteTasks.map { it.id }.toSet()
            val localTasks  = dao.getTasksForUser(userId)
            for (local in localTasks) {
                if (local.id !in remoteIds && !local.syncPending) {
                    dao.hardDelete(local.id)
                    Log.d(TAG, "Hard-deleted orphaned local task ${local.id}.")
                }
            }

            Log.d(TAG, "Sync completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: $e")
            Result.retry()
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Static helpers — schedule sync from anywhere
    // ══════════════════════════════════════════════════════════════════════════

    companion object {
        private const val TAG                     = "TaskSyncWorker"
        private const val WORK_NAME_ONE_TIME      = "TaskOneTimeSync"
        private const val WORK_NAME_PERIODIC      = "TaskPeriodicSync"
        private const val PERIODIC_INTERVAL_HOURS = 1L

        private fun buildNetworkConstraints() = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        /**
         * Enqueues a one-time sync that runs as soon as the device has a
         * network connection. Uses [ExistingWorkPolicy.REPLACE] so repeated
         * calls after rapid mutations don't stack up pending jobs.
         */
        fun triggerOneTimeSync(context: Context) {
            val request = OneTimeWorkRequestBuilder<TaskSyncWorker>()
                .setConstraints(buildNetworkConstraints())
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(
                    WORK_NAME_ONE_TIME,
                    ExistingWorkPolicy.REPLACE,
                    request,
                )
            Log.d(TAG, "One-time sync enqueued.")
        }

        /**
         * Schedules a periodic sync (every hour) that persists across app restarts.
         * Uses [ExistingPeriodicWorkPolicy.KEEP] so an existing schedule is not reset
         * each time the app launches.
         */
        fun schedulePeriodicSync(context: Context) {
            val request = PeriodicWorkRequestBuilder<TaskSyncWorker>(
                PERIODIC_INTERVAL_HOURS, TimeUnit.HOURS,
            )
                .setConstraints(buildNetworkConstraints())
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(
                    WORK_NAME_PERIODIC,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request,
                )
            Log.d(TAG, "Periodic sync scheduled (every $PERIODIC_INTERVAL_HOURS hour(s)).")
        }
    }
}
