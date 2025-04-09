package com.taskermobile.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import com.taskermobile.R
import com.taskermobile.TaskerApplication
import com.taskermobile.data.repository.NotificationRepository
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

class UnreadNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val notificationRepository: NotificationRepository =
        (context.applicationContext as TaskerApplication).notificationRepository
    private val sessionManager: SessionManager = SessionManager(context)

    companion object {
        private const val TAG = "UnreadNotificationWorker"
        private const val CHANNEL_ID = "tasker_unread_notifications"
        private const val NOTIFICATION_ID = 2
        
        // Simple function to start the notification worker
        fun startPolling(context: Context) {
            Log.d(TAG, "Starting unread notification polling")
            try {
                // Standard 15-minute WorkManager periodic request (minimum allowed)
                val workRequest = PeriodicWorkRequestBuilder<UnreadNotificationWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "UnreadNotificationWorker",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
                
                Log.d(TAG, "Notification worker started with 15-minute interval")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting notification worker", e)
            }
        }

        // Simple function to stop the notification worker
        fun stopPolling(context: Context) {
            Log.d(TAG, "Stopping unread notification polling")
            WorkManager.getInstance(context).cancelUniqueWork("UnreadNotificationWorker")
        }
        
        // Method to immediately check for unread notifications (useful for push notification scenarios)
        fun checkForUnreadNotifications(context: Context) {
            Log.d(TAG, "Immediate check for unread notifications requested")
            
            try {
                // Use a OneTimeWorkRequest with no delays or constraints
                val workRequest = androidx.work.OneTimeWorkRequestBuilder<UnreadNotificationWorker>()
                    .build()
                
                // Enqueue the request and the WorkManager will handle it immediately
                WorkManager.getInstance(context).enqueue(workRequest)
                Log.d(TAG, "Immediate notification check scheduled with ID: ${workRequest.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling immediate notification check", e)
            }
        }
    }
    
    // Separated logic for checking notifications so it can be called directly
    private suspend fun doChecks(): Boolean {
        try {
            // Get the user ID
            val userId = sessionManager.userId.first()
            if (userId == null) {
                Log.e(TAG, "No user ID available")
                return false
            }

            Log.d(TAG, "Checking for unread notifications for user: $userId")
            // Check for unread notifications
            val result = notificationRepository.fetchUnreadNotifications(userId)
            
            if (result.isSuccess) {
                val notifications = result.getOrNull() ?: emptyList()
                
                if (notifications.isNotEmpty()) {
                    Log.d(TAG, "Found ${notifications.size} unread notifications, showing notification")
                    showNotification(notifications.size)
                    return true
                } else {
                    Log.d(TAG, "No unread notifications found for user: $userId")
                }
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Failed to fetch notifications: ${error?.message}", error)
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking notifications", e)
            return false
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Checking for unread notifications")
        return if (doChecks()) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    private fun showNotification(unreadCount: Int) {
        try {
            // Create notification channel on Android 8.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Unread Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Shows when you have unread notifications"
                    enableVibration(true)
                    enableLights(true)
                    lightColor = android.graphics.Color.RED
                    setShowBadge(true)
                }
                
                val manager = applicationContext.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(channel)
            }
            
            // Check notification permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionGranted = applicationContext.checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                if (!permissionGranted) {
                    Log.e(TAG, "Notification permission not granted")
                    return
                }
            }
            
            // Build and show the notification
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Unread Notifications")
                .setContentText("You have $unreadCount unread notifications")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVibrate(longArrayOf(0, 250, 250, 250)) 
                .setLights(android.graphics.Color.RED, 1000, 300)
                .setAutoCancel(true)
                .build()
                
            NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notification displayed with ID: $NOTIFICATION_ID")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }
} 