package com.taskermobile

import android.app.Application
import com.taskermobile.data.api.NotificationApiService
import com.taskermobile.data.api.RetroFitClient
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.data.repository.NotificationRepository
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TaskerApplication : Application() {
    lateinit var sessionManager: SessionManager // Declare SessionManager
    var currentDecryptedAuthToken: String? = null // To hold token after biometric auth
    
    // Application scope for coroutines that should live as long as the application
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this) // Initialize SessionManager
    }

    val database: TaskerDatabase by lazy {
        TaskerDatabase.getDatabase(this)
    }

    val notificationRepository: NotificationRepository by lazy {
        val notificationDao = database.notificationDao()
        val notificationApiService = RetroFitClient.createService<NotificationApiService>(this, sessionManager)
        NotificationRepository(notificationApiService, notificationDao)
    }

    fun clearDecryptedToken() {
        currentDecryptedAuthToken = null
    }
}
