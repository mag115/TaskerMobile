package `is`.hbv501g.taskermobile.data.service

import `is`.hbv501g.taskermobile.data.model.Task
import `is`.hbv501g.taskermobile.data.model.TaskResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TaskService {
    @POST("tasks")
    suspend fun createTask(@Body task: Task): Response<TaskResponse<Task>>
}