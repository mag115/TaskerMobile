package com.taskermobile.data.api

import com.taskermobile.data.local.entity.NotificationEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface NotificationApiService {

    @GET("/notifications/{userId}")
    suspend fun getAllNotifications(@Path("userId") userId: Long): Response<List<NotificationEntity>>

    @GET("/notifications/{userId}/unread")
    suspend fun getUnreadNotifications(@Path("userId") userId: Long): Response<List<NotificationEntity>>

    @PATCH("/notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(@Path("notificationId") notificationId: Long): Response<Unit>
}
