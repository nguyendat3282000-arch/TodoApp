// File: data/remote/FirestoreTaskSource.kt
package com.example.todoapp.data.remote

import android.util.Log
import com.example.todoapp.data.mapper.toFirestoreMap
import com.example.todoapp.data.mapper.toTask
import com.example.todoapp.domain.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

/**
 * Remote data source that isolates ALL Firestore SDK calls in one place.
 *
 * This class is the ONLY component in the codebase allowed to hold a reference
 * to [FirebaseFirestore] or call its API directly. Every other component that
 * needs remote data interacts with this class instead.
 *
 * Responsibilities:
 *  - Upload (upsert) a task document to Firestore.
 *  - Delete a task document from Firestore.
 *  - Fetch all task documents for a given user from Firestore (server-side).
 *
 * All methods are suspend functions that bridge the Firestore Task API
 * via [kotlinx.coroutines.tasks.await] and surface errors as exceptions
 * (callers use [runCatching]).
 */
class FirestoreTaskSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val tasksCollection get() = firestore.collection(COLLECTION_TASKS)

    // ── Write operations ──────────────────────────────────────────────────────

    /**
     * Upserts [task] to Firestore using [Task.id] as the document ID.
     * Uses [Task.toFirestoreMap] to produce the write payload. Timestamps are
     * managed server-side by Firestore.
     *
     * @throws Exception if the network call fails (caller should use runCatching).
     */
    suspend fun upsertTask(task: Task) {
        tasksCollection
            .document(task.id)
            .set(task.toFirestoreMap())
            .await()
        Log.d(TAG, "Upserted task ${task.id} to Firestore.")
    }

    /**
     * Permanently deletes the document for [taskId] from Firestore.
     *
     * @throws Exception if the network call fails.
     */
    suspend fun deleteTask(taskId: String) {
        tasksCollection
            .document(taskId)
            .delete()
            .await()
        Log.d(TAG, "Deleted task $taskId from Firestore.")
    }

    /**
     * Fetches all task documents belonging to [userId] directly from the
     * Firestore server (bypasses local SDK cache).
     *
     * @param userId The authenticated user's UID.
     * @return List of domain [Task] objects deserialized from the snapshot.
     * @throws Exception if the network call fails.
     */
    suspend fun fetchTasksForUser(userId: String): List<Task> {
        val snapshot = tasksCollection
            .whereEqualTo(FIELD_USER_ID, userId)
            .get(Source.SERVER)
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            data.toTask(documentId = doc.id)
        }.also {
            Log.d(TAG, "Fetched ${it.size} tasks for user $userId from Firestore.")
        }
    }

    companion object {
        private const val TAG              = "FirestoreTaskSource"
        private const val COLLECTION_TASKS = "tasks"
        private const val FIELD_USER_ID    = "userId"
    }
}
