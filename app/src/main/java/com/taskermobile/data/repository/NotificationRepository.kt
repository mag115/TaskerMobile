package com.taskermobile.data.repository

import com.taskermobile.data.api.NotificationApiService
import com.taskermobile.data.local.dao.NotificationDao
import com.taskermobile.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import android.util.Log
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
                    Log.d("NotificationRepository", "Received ${newNotifications.size} notifications from API")
                    
                    // Get existing notifications to preserve read status and timestamps
                    val existingNotifications = notificationDao.getAllNotifications().first()
                    val existingNotificationsMap = existingNotifications.associateBy { it.id }
                    
                    // Update new notifications with read status and timestamps from existing ones
                    val updatedNotifications = newNotifications.map { newNotification ->
                        val existingNotification = existingNotificationsMap[newNotification.id]
                        if (existingNotification != null) {
                            // Use existing notification's timestamp and read status
                            Log.d("NotificationRepository", "Using existing timestamp for notification ${newNotification.id}: ${existingNotification.timestamp}")
                            newNotification.copy(
                                isRead = existingNotification.isRead,
                                timestamp = existingNotification.timestamp,
                                timestampMillis = existingNotification.timestampMillis
                            )
                        } else {
                            // For new notifications, ensure they have a valid timestamp
                            val validTimestamp = validateTimestamp(newNotification.timestamp)
                            Log.d("NotificationRepository", "New notification ${newNotification.id} with validated timestamp: $validTimestamp")
                            newNotification.copy(
                                timestamp = validTimestamp
                                // timestampMillis will be recalculated from the new timestamp
                            )
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
            Log.e("NotificationRepository", "Error fetching notifications", e)
            Result.failure(e)
        }
    }

    suspend fun fetchUnreadNotifications(userId: Long): Result<List<NotificationEntity>> {
        return try {
            val response = notificationApi.getUnreadNotifications(userId)
            if (response.isSuccessful) {
                response.body()?.let { unreadNotifications ->
                    Log.d("NotificationRepository", "Received ${unreadNotifications.size} unread notifications from API")
                    
                    // Get existing notifications to preserve timestamps
                    val existingNotifications = notificationDao.getAllNotifications().first()
                    val existingNotificationsMap = existingNotifications.associateBy { it.id }
                    
                    // Update unread notifications with timestamps from existing ones
                    val updatedNotifications = unreadNotifications.map { unreadNotification ->
                        val existingNotification = existingNotificationsMap[unreadNotification.id]
                        if (existingNotification != null) {
                            // Use existing notification's timestamp
                            Log.d("NotificationRepository", "Using existing timestamp for unread notification ${unreadNotification.id}: ${existingNotification.timestamp}")
                            unreadNotification.copy(
                                timestamp = existingNotification.timestamp,
                                timestampMillis = existingNotification.timestampMillis
                            )
                        } else {
                            // For new notifications, ensure they have a valid timestamp
                            val validTimestamp = validateTimestamp(unreadNotification.timestamp)
                            Log.d("NotificationRepository", "New unread notification ${unreadNotification.id} with validated timestamp: $validTimestamp")
                            unreadNotification.copy(
                                timestamp = validTimestamp
                                // timestampMillis will be recalculated from the new timestamp
                            )
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
            Log.e("NotificationRepository", "Error fetching unread notifications", e)
            Result.failure(e)
        }
    }

    private fun validateTimestamp(timestamp: String): String {
        // Log the original timestamp
        Log.d("NotificationRepository", "Validating timestamp: $timestamp")
        
        // If timestamp is empty, null, or doesn't match expected format
        if (timestamp.isBlank() || timestamp == "null" || 
            (!timestamp.contains('T') && !timestamp.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*")))) {
            // Generate a current timestamp in the expected format for Spring LocalDateTime
            val currentDateTime = LocalDateTime.now()
            // Use ISO format without the timezone part which matches Spring's LocalDateTime format
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val formattedTimestamp = currentDateTime.format(formatter)
            Log.d("NotificationRepository", "Generated new timestamp: $formattedTimestamp")
            return formattedTimestamp
        }
        
        // If timestamp is in expected format but is missing the 'T' separator
        if (timestamp.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*"))) {
            // Replace space with 'T'
            val correctedTimestamp = timestamp.replace(" ", "T")
            Log.d("NotificationRepository", "Corrected timestamp format: $correctedTimestamp")
            return correctedTimestamp
        }
        
        // Log and return the original timestamp for valid timestamps
        Log.d("NotificationRepository", "Original timestamp is valid: $timestamp")
        return timestamp
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
