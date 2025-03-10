package com.taskermobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.taskermobile.data.local.dao.NotificationDao
import com.taskermobile.data.local.dao.ProjectDao
import com.taskermobile.data.local.dao.TaskDao
import com.taskermobile.data.local.dao.UserDao
import com.taskermobile.data.local.entity.NotificationEntity
import com.taskermobile.data.local.entity.ProjectEntity
import com.taskermobile.data.local.entity.TaskEntity
import com.taskermobile.data.local.entity.UserEntity

@Database(
    entities = [ProjectEntity::class, TaskEntity::class, UserEntity::class, NotificationEntity::class],
    version = 4,
    exportSchema = false
)
abstract class TaskerDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: TaskerDatabase? = null

        fun getDatabase(context: Context): TaskerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskerDatabase::class.java,
                    "tasker_database"
                )
                .fallbackToDestructiveMigration()  // This will drop and recreate tables if schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 