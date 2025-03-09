package com.taskermobile.data.service


import com.taskermobile.data.model.NotificationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface NotificationApiService {
    @GET("/notifications/{userId}")
    suspend fun getNotifications(@Path("userId") userId: Long): List<NotificationResponse>

    @GET("/notifications/{userId}/unread")
    suspend fun getUnreadNotifications(@Path("userId") userId: Long): List<NotificationResponse>

    @PATCH("/notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(@Path("notificationId") notificationId: Long): Response<Unit>
}