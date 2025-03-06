package `is`.hbv501g.taskermobile.data.service

import androidx.room.Query
import `is`.hbv501g.taskermobile.data.model.Task
import `is`.hbv501g.taskermobile.data.model.TaskResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.*

interface TaskService {
    // Create a new task (API returns TaskResponse<Task>)
    @POST("tasks")
    suspend fun createTask(@Body task: Task): Response<TaskResponse<Task>>

    // Fetch all tasks (API should return TaskResponse<List<Task>>)
    @GET("tasks")
    suspend fun getAllTasks(): Response<TaskResponse<List<Task>>>

    // Fetch a specific task by ID (Ensure consistent TaskResponse<Task>)
    @GET("tasks/{taskId}")
    suspend fun getTaskById(@Path("taskId") taskId: Long): Response<TaskResponse<Task>>

    // Update an existing task (Ensure consistent TaskResponse<Task>)
    @PUT("tasks/{taskId}")
    suspend fun updateTask(@Path("taskId") taskId: Long, @Body updatedTask: Task): Response<TaskResponse<Task>>

    // Delete a task (Ensure API response consistency)
    @DELETE("tasks/{taskId}")
    suspend fun deleteTask(@Path("taskId") taskId: Long): Response<TaskResponse<Unit>>
}
