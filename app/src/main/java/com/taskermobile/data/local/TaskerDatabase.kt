package com.taskermobile.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.taskermobile.data.local.converter.Converters
import com.taskermobile.data.local.converter.TaskListConverter
import com.taskermobile.data.local.dao.*
import com.taskermobile.data.local.entity.*
import androidx.room.migration.Migration

@Database(
    entities = [
        ProjectEntity::class,
        TaskEntity::class,
        UserEntity::class,
        NotificationEntity::class,
        ProjectReportEntity::class
    ],
    version = 7,
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

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Already handled in next migration safely
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val cursor = database.query("PRAGMA table_info(tasks)")
                var hasImageUri = false
                while (cursor.moveToNext()) {
                    val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    if (columnName == "imageUri") {
                        hasImageUri = true
                        break
                    }
                }
                cursor.close()

                if (!hasImageUri) {
                    database.execSQL("ALTER TABLE tasks ADD COLUMN imageUri TEXT")
                }
            }
        }

        fun getDatabase(context: Context): TaskerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskerDatabase::class.java,
                    "tasker_database"
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("PRAGMA foreign_keys = ON;")
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}