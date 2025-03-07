package com.taskermobile.data.service

import androidx.room.Query
import com.taskermobile.data.model.Task
import com.taskermobile.data.model.TaskResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface TaskService {
    @POST("tasks")
    suspend fun createTask(
        @Body task: Task,
    ): Response<TaskResponse<Task>>

}