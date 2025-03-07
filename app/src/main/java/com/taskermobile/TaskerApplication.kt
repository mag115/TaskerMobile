package com.taskermobile

import android.app.Application
import com.taskermobile.data.local.TaskerDatabase

class TaskerApplication : Application() {
    lateinit var database: TaskerDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = TaskerDatabase.getInstance(this)
    }
} 