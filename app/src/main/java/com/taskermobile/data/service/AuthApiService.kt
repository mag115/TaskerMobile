
package com.taskermobile.data.api

import com.taskermobile.data.model.LoginRequest
import com.taskermobile.data.model.LoginResponse
import com.taskermobile.data.model.SignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface AuthApiService {

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse> 

    @POST("/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<LoginResponse>
}
