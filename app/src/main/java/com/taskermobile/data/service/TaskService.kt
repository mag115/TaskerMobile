package com.taskermobile.data.service

import com.taskermobile.data.model.Task
import com.taskermobile.data.model.TaskResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskService {
    @POST("/tasks")
    suspend fun createTask(
        @Body task: Task,
    ): Response<TaskResponse<Task>>

    @GET("/tasks")
    suspend fun getAllTasks(@Query("project_id") projectId: Long): Response<List<Task>>

    @GET("/assigned")
    suspend fun getAssignedTasks(@Query("project_id") projectId: Long): Response<List<Task>>

    @PATCH("/tasks/{taskId}/updateTime")
    @FormUrlEncoded
    suspend fun updateTimeSpent(@Path("taskId") taskId: Long, @Field("timeSpent") timeSpent: Double): Task

}