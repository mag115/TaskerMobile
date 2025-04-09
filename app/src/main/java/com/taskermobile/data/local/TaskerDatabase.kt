package com.taskermobile.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.taskermobile.data.local.converter.Converters
import com.taskermobile.data.local.converter.TaskListConverter
import com.taskermobile.data.local.dao.*
import com.taskermobile.data.local.entity.*
import com.taskermobile.data.local.relations.ProjectMemberCrossRef
import androidx.room.migration.Migration
import android.util.Log

@Database(
    entities = [
        ProjectEntity::class,
        TaskEntity::class,
        UserEntity::class,
        NotificationEntity::class,
        ProjectReportEntity::class,
        ProjectMemberCrossRef::class
    ],
    version = 14,
    exportSchema = false
)
@TypeConverters(TaskListConverter::class, Converters::class)
abstract class TaskerDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao
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
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d("TaskerDatabase", "Database created")
                        // Verify tables exist
                        db.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
                            while (cursor.moveToNext()) {
                                Log.d("TaskerDatabase", "Table found: ${cursor.getString(0)}")
                            }
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys = ON;")
                        Log.d("TaskerDatabase", "Database opened")
                        // Verify tables exist
                        db.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
                            while (cursor.moveToNext()) {
                                Log.d("TaskerDatabase", "Table found: ${cursor.getString(0)}")
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}