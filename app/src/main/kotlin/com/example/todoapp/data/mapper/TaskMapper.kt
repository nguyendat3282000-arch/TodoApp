// File: data/mapper/TaskMapper.kt
package com.example.todoapp.data.mapper

import com.example.todoapp.data.local.entity.TaskEntity
import com.example.todoapp.domain.model.Task

// ══════════════════════════════════════════════════════════════════════════════
//  TaskMapper — Single source of truth for all Task mapping logic
//
//  Conversions covered:
//   TaskEntity  ↔  Task  (local DB ↔ domain)
//   Map<String,Any?> → Task  (Firestore document → domain)
//   Task → Map<String,Any?>  (domain → Firestore write payload)
// ══════════════════════════════════════════════════════════════════════════════

// ── TaskEntity → Task ─────────────────────────────────────────────────────────

/**
 * Maps a [TaskEntity] (Room) to a domain [Task].
 * All data is already in the correct format (Long millis, String dates).
 */
fun TaskEntity.toDomain(): Task = Task(
    id            = id,
    title         = title,
    description   = description,
    dueDate       = dueDate,
    dueTime       = dueTime,
    isDone        = isDone,
    userId        = userId,
    alarmSet      = alarmSet,
    createdAt     = createdAt,
    updatedAt     = updatedAt,
    syncPending   = syncPending,
    deletePending = deletePending,
)

// ── Task → TaskEntity ─────────────────────────────────────────────────────────

/**
 * Maps a domain [Task] to a [TaskEntity] for persistence in Room.
 *
 * @param syncPending   Whether this write should be pushed to Firestore on next sync.
 * @param deletePending Whether this record should be deleted from Firestore on next sync.
 */
fun Task.toEntity(
    syncPending: Boolean = false,
    deletePending: Boolean = false,
): TaskEntity = TaskEntity(
    id            = id,
    title         = title,
    description   = description,
    dueDate       = dueDate,
    dueTime       = dueTime,
    isDone        = isDone,
    userId        = userId,
    alarmSet      = alarmSet,
    createdAt     = if (createdAt != 0L) createdAt else System.currentTimeMillis(),
    updatedAt     = if (updatedAt != 0L) updatedAt else System.currentTimeMillis(),
    syncPending   = syncPending,
    deletePending = deletePending,
)

// ── Firestore document Map → Task ─────────────────────────────────────────────

/**
 * Deserializes a raw Firestore document [map] into a domain [Task].
 *
 * Firestore stores timestamps as [com.google.firebase.Timestamp]; we convert
 * them to epoch millis here so the domain [Task] remains Firebase-free.
 *
 * @param documentId The Firestore document ID (managed separately from the map).
 */
fun Map<String, Any?>.toTask(documentId: String): Task {
    fun timestampToMillis(value: Any?): Long {
        if (value == null) return System.currentTimeMillis()
        // Firestore returns com.google.firebase.Timestamp at runtime
        return try {
            val ts = value as com.google.firebase.Timestamp
            ts.toDate().time
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    return Task(
        id          = documentId,
        title       = this["title"] as? String ?: "",
        description = this["description"] as? String ?: "",
        dueDate     = this["dueDate"] as? String ?: "",
        dueTime     = this["dueTime"] as? String ?: "",
        isDone      = this["isDone"] as? Boolean ?: false,
        userId      = this["userId"] as? String ?: "",
        alarmSet    = this["alarmSet"] as? Boolean ?: false,
        createdAt   = timestampToMillis(this["createdAt"]),
        updatedAt   = timestampToMillis(this["updatedAt"]),
        syncPending = false,
        deletePending = false,
    )
}

// ── Task → Firestore document Map ─────────────────────────────────────────────

/**
 * Converts a domain [Task] to a plain [Map] for Firestore writes.
 *
 * The [Task.id] field is intentionally excluded — Firestore manages it as the
 * document ID, not as a field inside the document.
 *
 * `createdAt` / `updatedAt` are omitted here because they are set server-side
 * using Firestore's [com.google.firebase.firestore.FieldValue.serverTimestamp].
 */
fun Task.toFirestoreMap(): Map<String, Any?> = mapOf(
    "title"       to title,
    "description" to description,
    "dueDate"     to dueDate,
    "dueTime"     to dueTime,
    "isDone"      to isDone,
    "userId"      to userId,
    "alarmSet"    to alarmSet,
)
