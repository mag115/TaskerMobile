package com.taskermobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.taskermobile.data.local.converter.TaskListConverter
import com.taskermobile.data.local.dao.ProjectDao
import com.taskermobile.data.local.dao.ProjectReportDao
import com.taskermobile.data.local.dao.TaskDao
import com.taskermobile.data.local.dao.UserDao
import com.taskermobile.data.local.entity.ProjectEntity
import com.taskermobile.data.local.entity.ProjectReportEntity
import com.taskermobile.data.local.entity.TaskEntity
import com.taskermobile.data.local.entity.UserEntity

@Database(
    entities = [
        ProjectEntity::class,
        TaskEntity::class,
        UserEntity::class,
        ProjectReportEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(TaskListConverter::class)
abstract class TaskerDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao
    abstract fun projectReportDao(): ProjectReportDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
