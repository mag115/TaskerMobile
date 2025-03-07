package com.taskermobile.data.repository

import com.taskermobile.data.local.dao.ProjectDao
import com.taskermobile.data.local.mapper.toDomain
import com.taskermobile.data.local.mapper.toEntity
import com.taskermobile.data.model.Project
import com.taskermobile.data.service.ProjectService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

class ProjectRepository(
    private val projectService: ProjectService,
    private val projectDao: ProjectDao
) {
    fun getLocalProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun refreshProjects(): Result<List<Project>> {
        return try {
            val response = projectService.getAllProjects()
            if (response.isSuccessful) {
                response.body()?.let { projects ->
                    projectDao.insertProjects(projects.map { it.toEntity() })
                    Result.success(projects)
                } ?: Result.failure(Exception("Response body is null"))
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
                response.body()?.let { createdProject ->
                    projectDao.insertProject(createdProject.toEntity())
                    Result.success(createdProject)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 