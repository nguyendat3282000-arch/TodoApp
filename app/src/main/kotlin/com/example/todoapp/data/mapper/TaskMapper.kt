// File: data/mapper/TaskMapper.kt
package com.example.todoapp.data.mapper

import com.example.todoapp.data.local.entity.TaskEntity
import com.example.todoapp.data.local.entity.TaskLogEntity
import com.example.todoapp.data.local.entity.UserStatsEntity
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskType
import com.example.todoapp.domain.model.FrequencyType
import com.example.todoapp.domain.model.FlexibleInterval
import com.example.todoapp.domain.model.UserStats
import com.example.todoapp.domain.model.TaskLog

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
    type          = runCatching { TaskType.valueOf(type) }.getOrDefault(TaskType.DAILY),
    frequencyType = frequencyType?.let { runCatching { FrequencyType.valueOf(it) }.getOrNull() },
    fixedDays     = fixedDays.split(",").filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() },
    flexibleCount = flexibleCount,
    flexibleInterval = flexibleInterval?.let { runCatching { FlexibleInterval.valueOf(it) }.getOrNull() },
    streak        = streak,
    lastCompletedDate = lastCompletedDate
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
    type          = type.name,
    frequencyType = frequencyType?.name,
    fixedDays     = fixedDays.joinToString(","),
    flexibleCount = flexibleCount,
    flexibleInterval = flexibleInterval?.name,
    streak        = streak,
    lastCompletedDate = lastCompletedDate
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

    val typeStr = this["type"] as? String ?: "DAILY"
    val freqStr = this["frequencyType"] as? String
    val flexIntervalStr = this["flexibleInterval"] as? String

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
        type          = runCatching { TaskType.valueOf(typeStr) }.getOrDefault(TaskType.DAILY),
        frequencyType = freqStr?.let { runCatching { FrequencyType.valueOf(it) }.getOrNull() },
        fixedDays     = (this["fixedDays"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList(),
        flexibleCount = (this["flexibleCount"] as? Number)?.toInt() ?: 0,
        flexibleInterval = flexIntervalStr?.let { runCatching { FlexibleInterval.valueOf(it) }.getOrNull() },
        streak        = (this["streak"] as? Number)?.toInt() ?: 0,
        lastCompletedDate = this["lastCompletedDate"] as? String
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
    "type"        to type.name,
    "frequencyType" to frequencyType?.name,
    "fixedDays"   to fixedDays,
    "flexibleCount" to flexibleCount,
    "flexibleInterval" to flexibleInterval?.name,
    "streak"      to streak,
    "lastCompletedDate" to lastCompletedDate,
)

fun UserStatsEntity.toDomain(): UserStats = UserStats(
    userId = userId,
    healthScore = healthScore,
    totalStreak = totalStreak,
    lastResetDate = lastResetDate
)

fun UserStats.toEntity(): UserStatsEntity = UserStatsEntity(
    userId = userId,
    healthScore = healthScore,
    totalStreak = totalStreak,
    lastResetDate = lastResetDate
)

fun TaskLogEntity.toDomain(): TaskLog = TaskLog(
    id = id,
    taskId = taskId,
    userId = userId,
    completedDate = completedDate,
    pointsEarned = pointsEarned
)

fun TaskLog.toEntity(): TaskLogEntity = TaskLogEntity(
    id = id,
    taskId = taskId,
    userId = userId,
    completedDate = completedDate,
    pointsEarned = pointsEarned
)
