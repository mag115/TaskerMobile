package com.taskermobile.data.repository

import com.taskermobile.data.service.NotificationApiService
import com.taskermobile.data.model.NotificationResponse

class NotificationRepository(private val api: NotificationApiService) {

    suspend fun fetchNotifications(userId: Long): List<NotificationResponse> {
        return api.getNotifications(userId)
    }

    suspend fun fetchUnreadNotifications(userId: Long): List<NotificationResponse> {
        return api.getUnreadNotifications(userId)
    }

    suspend fun markAsRead(notificationId: Long) {
        api.markNotificationAsRead(notificationId)
    }
}