package `is`.hbv501g.taskermobile.data.repository

import `is`.hbv501g.taskermobile.data.model.Project
import `is`.hbv501g.taskermobile.data.service.`ProjectService.kt`
import retrofit2.HttpException

class ProjectRepository(
    private val projectService: `ProjectService.kt`
) {
    suspend fun getAllProjects(): Result<List<Project>> {
        return try {
            val response = projectService.getAllProjects()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProjects(userId: Long): Result<List<Project>> {
        return try {
            val response = projectService.getUserProjects(userId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserOwnedProjects(userId: Long): Result<List<Project>> {
        return try {
            val response = projectService.getUserOwnedProjects(userId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
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
                response.body()?.let {
                    Result.success(it)
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
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentProject(): Result<Project> {
        return try {
            val response = projectService.getCurrentProject()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setCurrentProject(projectId: Long): Result<Unit> {
        return try {
            val response = projectService.setCurrentProject(mapOf("projectId" to projectId))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMemberToProject(projectId: Long, userId: Long): Result<Project> {
        return try {
            val response = projectService.addMemberToProject(projectId, mapOf("userId" to userId))
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 