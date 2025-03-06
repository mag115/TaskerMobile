package `is`.hbv501g.taskermobile.data.service

import `is`.hbv501g.taskermobile.data.model.Project
import retrofit2.Response
import retrofit2.http.*

interface `ProjectService` {
    // Fetch all projects
    @GET("/projects")
    suspend fun getAllProjects(): Response<List<Project>>

    // Fetch a single project by ID
    @GET("/projects/{projectId}")
    suspend fun getProjectById(@Path("projectId") projectId: Long): Response<Project>

    // Create a new project
    @POST("/projects")
    suspend fun createProject(@Body project: Project): Response<Project>

    // Update an existing project
    @PUT("/projects/{projectId}")
    suspend fun updateProject(@Path("projectId") projectId: Long, @Body updatedProject: Project): Response<Project>

    // Fetch the currently selected project
    @GET("/projects/current")
    suspend fun getCurrentProject(): Response<Project>

    // Set the current project
    @POST("/projects/current")
    suspend fun setCurrentProject(@Body body: Map<String, Long>): Response<Unit>

    // Add a member to a project
    @POST("/projects/{projectId}/members")
    suspend fun addMemberToProject(@Path("projectId") projectId: Long, @Body body: Map<String, Long>): Response<Project>
}
