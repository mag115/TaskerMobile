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
            println("🔍 Fetching ALL notifications for user ID: $userId...")
            val response = notificationApi.getAllNotifications(userId) // Fetch all notifications

            if (response.isSuccessful) {
                response.body()?.let { notifications ->
                    println("✅ API returned ${notifications.size} notifications!") // Debug Log
                    notificationDao.insertNotifications(notifications)
                    return Result.success(notifications)
                } ?: run {
                    println("⚠️ API returned empty response!")
                    return Result.failure(Exception("Empty response"))
                }
            } else {
                println("❌ API request failed: ${response.errorBody()?.string()}")
                return Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            println("❌ Exception while fetching notifications: ${e.message}")
            return Result.failure(e)
        }
    }


    suspend fun markNotificationAsRead(notificationId: Long) {
        val response = notificationApi.markNotificationAsRead(notificationId)
        if (response.isSuccessful) {
            println("✅ Successfully marked notification $notificationId as read!")
            notificationDao.markAsRead(notificationId) // Only update if API succeeds
        } else {
            println("❌ Failed to mark notification as read: ${response.errorBody()?.string()}")
        }
    }

}
