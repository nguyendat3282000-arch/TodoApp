// File: data/local/TodoDatabase.kt
package com.example.todoapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.data.local.entity.TaskEntity

/**
 * Room database singleton.
 *
 * Contains a single [TaskEntity] table. The singleton is created once via
 * [getDatabase] and held in a [Volatile] field protected by [synchronized]
 * double-checked locking.
 *
 * Schema version is pinned to 1. Use [RoomDatabase.Builder.addMigrations] for
 * production migration paths instead of [fallbackToDestructiveMigration].
 */
import com.example.todoapp.data.local.entity.TaskLogEntity
import com.example.todoapp.data.local.entity.UserStatsEntity

@Database(
    entities = [TaskEntity::class, TaskLogEntity::class, UserStatsEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {

    abstract val taskDao: TaskDao


    companion object {
        @Volatile
        private var INSTANCE: TodoDatabase? = null

        fun getDatabase(context: Context): TodoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
