package `is`.hbv501g.taskermobile.data.service


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import `is`.hbv501g.taskermobile.MainActivity
import `is`.hbv501g.taskermobile.R

class NotificationService : FirebaseMessagingService() {

    companion object {
        const val TAG = "MyFirebaseMsgService"
        const val CHANNEL_ID = "task_updates_channel"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        // TODO: Send this token to your backend server if needed
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // If the message contains data payload, handle it:
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            val title = remoteMessage.data["title"] ?: "Task Update"
            val body = remoteMessage.data["body"] ?: "You have a new task assignment/update."
            sendNotification(title, body)
        }

        // If the message contains a notification payload, use it:
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "Task Update", it.body ?: "")
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            // Optionally, add extras to direct the user to a specific fragment or task detail
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Setja eh icon?
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for task assignments and updates"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}