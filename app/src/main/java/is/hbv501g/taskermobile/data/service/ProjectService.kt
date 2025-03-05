package `is`.hbv501g.taskermobile.data.service

import `is`.hbv501g.taskermobile.data.model.Project
import retrofit2.Response
import retrofit2.http.*

interface ProjectService {
    @GET("/projects")
    suspend fun getAllProjects(): Response<List<Project>>

    // @GET("users/{userId}/projects")
    // suspend fun getUserProjects(@Path("userId") userId: Long): Response<List<Project>>

    // @GET("users/{userId}/owned-projects")
    // suspend fun getUserOwnedProjects(@Path("userId") userId: Long): Response<List<Project>>

    // @GET("projects/{projectId}")
    // suspend fun getProjectById(@Path("projectId") projectId: Long): Response<Project>

    // @POST("projects")
    // suspend fun createProject(@Body project: Project): Response<Project>

    // @GET("projects/current")
    // suspend fun getCurrentProject(): Response<Project>

    // @POST("projects/current")
    // suspend fun setCurrentProject(@Body body: Map<String, Long>): Response<Unit>

    // @POST("projects/{projectId}/members")
    // suspend fun addMemberToProject(@Path("projectId") projectId: Long, @Body body: Map<String, Long>): Response<Project>
} 