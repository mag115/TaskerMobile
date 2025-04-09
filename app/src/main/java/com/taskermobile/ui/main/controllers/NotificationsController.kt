package com.taskermobile.ui.main.controllers

import android.util.Log
import com.taskermobile.data.local.entity.NotificationEntity
import com.taskermobile.data.repository.NotificationRepository
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsController(
    private val notificationRepository: NotificationRepository,
    private val sessionManager: SessionManager
) {

    fun fetchNotifications(onResult: (List<NotificationEntity>, Int) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Show all local notifications
                notificationRepository.getLocalNotifications().collectLatest { notifications ->
                    val unreadCount = notifications.count { !it.isRead }
                    withContext(Dispatchers.Main) {
                        onResult(notifications, unreadCount)
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationsController", "Error fetching local notifications", e)
                withContext(Dispatchers.Main) { onResult(emptyList(), 0) }
            }
        }
    }

    fun refreshNotifications(onResult: (List<NotificationEntity>, Int) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = sessionManager.userId.first() ?: return@launch
                // Fetch only unread notifications from server
                val result = notificationRepository.fetchUnreadNotifications(userId)
                
                if (result.isSuccess) {
                    // After fetching unread, get all notifications to display
                    notificationRepository.getLocalNotifications().first().let { allNotifications ->
                        val unreadCount = allNotifications.count { !it.isRead }
                        withContext(Dispatchers.Main) {
                            onResult(allNotifications, unreadCount)
                        }
                    }
                } else {
                    Log.e("NotificationsController", "Error refreshing unread notifications", result.exceptionOrNull())
                    // Still try to show local notifications on error
                    notificationRepository.getLocalNotifications().first().let { allNotifications ->
                        val unreadCount = allNotifications.count { !it.isRead }
                        withContext(Dispatchers.Main) {
                            onResult(allNotifications, unreadCount)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationsController", "Error refreshing notifications", e)
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
                withContext(Dispatchers.Main) { onComplete() }
            }
        }
    }
}
