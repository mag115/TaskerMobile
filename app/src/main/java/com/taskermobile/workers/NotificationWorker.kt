package com.taskermobile.workers
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.taskermobile.R
import com.taskermobile.TaskerApplication
import com.taskermobile.data.repository.NotificationRepository
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.first

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val notificationRepository: NotificationRepository =
        (context.applicationContext as TaskerApplication).notificationRepository
    private val sessionManager: SessionManager = SessionManager(context)

    override suspend fun doWork(): Result {
        val userId = sessionManager.userId.first() ?: return Result.failure()

        return try {
            val result = notificationRepository.fetchNotifications(userId)
            if (result.isSuccess) {
                val newNotifications = result.getOrNull()?.filter { !it.isRead } ?: emptyList()

                if (newNotifications.isNotEmpty()) {
                    showNotification(newNotifications.size) // Show local notification
                }

                Result.success()
            } else {
                Result.retry() // Retry if the fetch fails
            }
        } catch (e: Exception) {
            Result.retry() //Retry in case of an exception
        }
    }

    private fun showNotification(unreadCount: Int) {
        val channelId = "tasker_notifications"
        val notificationId = 1

        //Ensure the channel is created on Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tasker Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Task updates and reminders"
            }

            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Check for Notification Permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                return // Exit early if permission is NOT granted
            }
        }

        // Build Notification
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Tasker Notifications")
            .setContentText("You have $unreadCount unread notifications")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Close when tapped
            .build()

        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }

}
