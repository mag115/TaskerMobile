package com.taskermobile.data.service

import com.taskermobile.data.model.Task
import com.taskermobile.data.model.TaskResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskService {
    @POST("/tasks")
    suspend fun createTask(
        @Body task: Task,
        @Query("projectId") projectId: Long,
        @Query("assignedUserId") assignedUserId: Long? = null // Optional parameter
    ): Response<TaskResponse<Task>>


    @GET("/tasks")
    suspend fun getAllTasks(@Query("project_id") projectId: Long): Response<List<Task>>

    @POST("/tasks/updateTime")
    suspend fun updateTime(@Body timeRequest: HashMap<String, Any>): Response<Task>

    @PUT("/tasks/{taskId}/updateTime")
    suspend fun updateTaskTime(
        @Path("taskId") taskId: Long,
        @Body timeSpent: Double
    ): Response<Task>
    //@GET("/assigned")
    //suspend fun getAssignedTasks(@Query("project_id") projectId: Long = 1L): Response<List<Task>>

    @GET("/tasks/assigned")
    suspend fun getAssignedTasks(
        @Query("username") username: String,
        @Query("project_id") projectId: Long
    ): Response<List<Task>>
}