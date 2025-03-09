package com.taskermobile

import android.app.Application
import com.google.firebase.FirebaseApp
import com.taskermobile.data.local.TaskerDatabase

class TaskerApplication : Application() {
    val database: TaskerDatabase by lazy {
        TaskerDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 