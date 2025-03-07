package `is`.hbv501g.taskermobile.data.service

import `is`.hbv501g.taskermobile.data.model.Project
import retrofit2.HttpException

class ProjectServiceImpl(
    private val projectService: ProjectService
) {
    suspend fun getAllProjects(): Result<List<Project>> {
        return try {
            val response = projectService.getAllProjects()
            if (response.isSuccessful) {
                response.body()?.let { projects ->
                    Result.success(projects)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProjectById(projectId: Long): Result<Project> {
        return try {
            val response = projectService.getProjectById(projectId)
            if (response.isSuccessful) {
                response.body()?.let { project ->
                    Result.success(project)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProject(project: Project): Result<Project> {
        return try {
            val response = projectService.createProject(project)
            if (response.isSuccessful) {
                response.body()?.let { projectCreated ->
                    Result.success(projectCreated)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Similarly add methods for getCurrentProject(), setCurrentProject(), and addMemberToProject()
}