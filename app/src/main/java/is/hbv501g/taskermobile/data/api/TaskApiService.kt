package `is`.hbv501g.taskermobile.data.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Call
import `is`.hbv501g.taskermobile.data.model.Task

interface TaskApiService {
    @GET("tasks") // Matches Spring Boot GET /tasks
    fun getAllTasks(): Call<List<Task>>

    @POST("tasks") // Matches Spring Boot POST /tasks
    fun createTask(@Body task: Task): Call<Task>
}
