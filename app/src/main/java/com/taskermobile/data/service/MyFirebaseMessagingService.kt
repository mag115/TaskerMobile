package com.taskermobile.data.service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.taskermobile.R
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.service.UserApiService
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val sessionManager: SessionManager by lazy { SessionManager(applicationContext) }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val userId = runBlocking { sessionManager.userId.first() } ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userApi = RetrofitClient.createService<UserApiService>(sessionManager)
                val response = userApi.registerToken(userId, mapOf("token" to token))
                if (response.isSuccessful) {
                    Log.d("FCM", "Token registered successfully")
                } else {
                    Log.e("FCM", "Failed to register token: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error sending token", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "tasker_notifications"
        val notificationId = System.currentTimeMillis().toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Tasker Notifications", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications for task assignments and updates"
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title ?: "Tasker Notification")
            .setContentText(message ?: "You have a new notification")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(this).notify(notificationId, builder.build())
            } else {
                Log.e("MyFirebaseMessagingService", "POST_NOTIFICATIONS permission not granted")
                // Optionally, request the permission here or inform the user.
            }
        } else {
            NotificationManagerCompat.from(this).notify(notificationId, builder.build())
        }
    }
}
