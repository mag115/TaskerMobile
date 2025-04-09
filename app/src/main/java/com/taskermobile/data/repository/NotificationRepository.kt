package com.taskermobile.data.repository

import com.taskermobile.data.api.NotificationApiService
import com.taskermobile.data.local.dao.NotificationDao
import com.taskermobile.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import android.util.Log

class NotificationRepository(
    private val notificationApi: NotificationApiService,
    private val notificationDao: NotificationDao
) {

    fun getLocalNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    fun getUnreadNotifications(): Flow<List<NotificationEntity>> = notificationDao.getUnreadNotifications()

    suspend fun fetchNotifications(userId: Long): Result<List<NotificationEntity>> {
        return try {
            val response = notificationApi.getAllNotifications(userId)
            if (response.isSuccessful) {
                response.body()?.let { newNotifications ->
                    // Get existing notifications to preserve read status and timestamps
                    val existingNotifications = notificationDao.getAllNotifications().first()
                    val existingNotificationsMap = existingNotifications.associateBy { it.id }
                    
                    // Update new notifications with read status and timestamps from existing ones
                    val updatedNotifications = newNotifications.map { newNotification ->
                        val existingNotification = existingNotificationsMap[newNotification.id]
                        if (existingNotification != null) {
                            newNotification.copy(
                                isRead = existingNotification.isRead,
                                timestamp = existingNotification.timestamp,
                                timestampMillis = existingNotification.timestampMillis
                            )
                        } else {
                            newNotification
                        }
                    }
                    
                    // Update notifications in database
                    notificationDao.insertNotifications(updatedNotifications)
                    Result.success(updatedNotifications)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUnreadNotifications(userId: Long): Result<List<NotificationEntity>> {
        return try {
            val response = notificationApi.getUnreadNotifications(userId)
            if (response.isSuccessful) {
                response.body()?.let { unreadNotifications ->
                    // Get existing notifications to preserve timestamps
                    val existingNotifications = notificationDao.getAllNotifications().first()
                    val existingNotificationsMap = existingNotifications.associateBy { it.id }
                    
                    // Update unread notifications with timestamps from existing ones
                    val updatedNotifications = unreadNotifications.map { unreadNotification ->
                        val existingNotification = existingNotificationsMap[unreadNotification.id]
                        if (existingNotification != null) {
                            unreadNotification.copy(
                                timestamp = existingNotification.timestamp,
                                timestampMillis = existingNotification.timestampMillis
                            )
                        } else {
                            unreadNotification
                        }
                    }
                    
                    // Insert new unread notifications
                    notificationDao.insertNotifications(updatedNotifications)
                    Result.success(updatedNotifications)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markNotificationAsRead(notificationId: Long) {
        try {
            notificationApi.markNotificationAsRead(notificationId)
            notificationDao.markAsRead(notificationId)
        } catch (e: Exception) {
            // If network call fails, still mark as read locally
            notificationDao.markAsRead(notificationId)
        }
    }
}
