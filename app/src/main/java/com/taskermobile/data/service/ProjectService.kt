package com.taskermobile.data.service

import com.taskermobile.data.model.Project
import com.taskermobile.data.model.Task
import retrofit2.Response
import retrofit2.http.*

interface ProjectService {
    @GET("/projects")
    suspend fun getAllProjects(): Response<List<Project>>

    @POST("projects")
    suspend fun createProject(@Body project: Project): Response<Project>

    @GET("users/{userId}/projects")
    suspend fun getUserProjects(@Path("userId") userId: Long): Response<List<Project>>

    @GET("users/{userId}/owned-projects")
    suspend fun getUserOwnedProjects(@Path("userId") userId: Long): Response<List<Project>>

    @GET("projects/{projectId}")
    suspend fun getProjectById(@Path("projectId") projectId: Long): Response<Project>

    @GET("projects/current")
    suspend fun getCurrentProject(): Response<Project>

    @POST("projects/current")
    suspend fun setCurrentProject(@Body body: Map<String, Long>): Response<Unit>

    @POST("projects/{projectId}/tasks")
    suspend fun createTask(@Path("projectId") projectId: Long, @Body task: Task): Response<Task>
} 