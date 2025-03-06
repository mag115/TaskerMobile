package `is`.hbv501g.taskermobile.data.api

import `is`.hbv501g.taskermobile.data.model.LoginRequest
import `is`.hbv501g.taskermobile.data.model.LoginResponse
import `is`.hbv501g.taskermobile.data.model.SignupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface AuthApiService {

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse> // Ensure this return type

    @POST("/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<LoginResponse> // Ensure this return type
}
