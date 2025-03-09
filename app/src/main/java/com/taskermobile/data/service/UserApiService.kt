package com.taskermobile.data.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApiService {
    @POST("/users/{userId}/register-token")
    suspend fun registerToken(
        @Path("userId") userId: Long,
        @Body token: Map<String, String> // Payload example: {"token": "fcm_token"}
    ): Response<Unit>
}