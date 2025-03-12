package com.taskermobile.data.repository

import com.taskermobile.data.api.NotificationApiService
import com.taskermobile.data.local.dao.NotificationDao
import com.taskermobile.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class NotificationRepository(
    private val notificationApi: NotificationApiService,
    private val notificationDao: NotificationDao
) {

    fun getLocalNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()

    fun getUnreadNotifications(): Flow<List<NotificationEntity>> = notificationDao.getUnreadNotifications()

    suspend fun fetchNotifications(userId: Long): Result<List<NotificationEntity>> {
        return try {
            val response = notificationApi.getUnreadNotifications(userId)
            if (response.isSuccessful) {
                response.body()?.let { notifications ->
                    notificationDao.insertNotifications(notifications)
                    Result.success(notifications)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markNotificationAsRead(notificationId: Long) {
        notificationApi.markNotificationAsRead(notificationId)
        notificationDao.markAsRead(notificationId)
    }
}
