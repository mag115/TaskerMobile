package com.taskermobile.data.service

import com.taskermobile.data.model.User
import retrofit2.Response
import retrofit2.http.*

interface UserService {

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") userId: Long): Response<User>

    @GET("users/id/{id}")
    suspend fun getUserById(@Path("id") userId: Long): Response<User>

    @GET("users")
    suspend fun getAllUsers(): Response<List<User>>

}