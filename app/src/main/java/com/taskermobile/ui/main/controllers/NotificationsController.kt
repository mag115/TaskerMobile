package com.taskermobile.ui.main.controllers

import android.util.Log
import com.taskermobile.data.local.entity.NotificationEntity
import com.taskermobile.data.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsController(private val notificationRepository: NotificationRepository) {

    fun fetchNotifications(onResult: (List<NotificationEntity>, Int) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificationRepository.getLocalNotifications().collectLatest { notifications ->
                    val unreadCount = notifications.count { !it.isRead }
                    withContext(Dispatchers.Main) {
                        onResult(notifications, unreadCount) // Callback for UI
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationsController", "Error fetching notifications", e)
                withContext(Dispatchers.Main) { onResult(emptyList(), 0) }
            }
        }
    }

    fun markAsRead(notification: NotificationEntity, onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificationRepository.markNotificationAsRead(notification.id)
                withContext(Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                Log.e("NotificationsController", "Error marking notification as read", e)
            }
        }
    }
}
