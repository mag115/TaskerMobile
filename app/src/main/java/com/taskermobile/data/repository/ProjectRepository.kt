package com.taskermobile.data.repository

import com.taskermobile.data.api.RetroFitClient
import com.taskermobile.data.local.dao.ProjectDao
import com.taskermobile.data.local.dao.TaskDao
import com.taskermobile.data.local.dao.UserDao
import com.taskermobile.data.local.entity.ProjectEntity
import com.taskermobile.data.local.entity.TaskEntity
import com.taskermobile.data.local.entity.UserEntity
import com.taskermobile.data.model.Project
import com.taskermobile.data.model.Task
import com.taskermobile.data.model.User
import com.taskermobile.data.service.ProjectService
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.util.Log

class ProjectRepository(
    private val projectService: ProjectService,
    private val projectDao: ProjectDao,
    private val taskDao: TaskDao,
    private val userDao: UserDao
) {
    private val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)

    // Get all projects from local database
    fun getLocalProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { projectEntities ->
            projectEntities.map { it.toProject() }
        }
    }

    // Get unsynced projects from local database
    fun getUnsyncedProjects(): Flow<List<Project>> {
        return projectDao.getUnsyncedProjects().map { projectEntities ->
            projectEntities.map { it.toProject() }
        }
    }

    // Create project locally first
    suspend fun createProject(project: Project): Result<Project> {
        return try {
            // Create project locally with isSynced = false
            val projectEntity = ProjectEntity.fromProject(project.copy(isSynced = false))
            val projectId = projectDao.insertProject(projectEntity)
            
            // Insert tasks if any
            project.tasks?.forEach { task ->
                val taskEntity = TaskEntity.fromTask(task.copy(projectId = projectId, isSynced = false))
                taskDao.insertTask(taskEntity)
            }
            
            // Insert members if any
            project.members?.forEach { member ->
                val userEntity = UserEntity.fromUser(member.copy(isSynced = false))
                userDao.insertUser(userEntity)
            }
            
            Result.success(project.copy(id = projectId, isSynced = false))
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error creating project locally", e)
            Result.failure(e)
        }
    }

    // Sync unsynced projects with the server
    suspend fun syncUnsyncedProjects() {
        try {
            val unsyncedProjects = projectDao.getUnsyncedProjectsSync()
            
            unsyncedProjects.forEach { projectEntity ->
                val project = projectEntity.toProject()
                val response = projectService.createProject(project)
                
                if (response.isSuccessful) {
                    val createdProject = response.body()
                    if (createdProject != null) {
                        // Update local project with server data
                        projectDao.updateProject(
                            projectEntity.copy(
                                id = createdProject.id,
                                isSynced = true,
                                updatedAt = currentTime
                            )
                        )
                        
                        // Sync tasks
                        project.tasks?.forEach { task ->
                            createdProject.id?.let { projectId ->
                                val taskResponse = projectService.createTask(projectId, task)
                                if (taskResponse.isSuccessful) {
                                    val createdTask = taskResponse.body()
                                    if (createdTask != null) {
                                        taskDao.updateTask(
                                            TaskEntity.fromTask(createdTask.copy(isSynced = true))
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Sync members
                        project.members?.forEach { member ->
                            createdProject.id?.let { projectId ->
                                val memberResponse = projectService.addMemberToProject(
                                    projectId,
                                    mapOf("userId" to (member.id ?: 0L))
                                )
                                if (memberResponse.isSuccessful) {
                                    userDao.updateUser(
                                        UserEntity.fromUser(member.copy(isSynced = true))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error syncing projects", e)
        }
    }

    // Refresh projects from server
    suspend fun refreshProjects(): Result<Unit> {
        return try {
            Log.d("ProjectRepository", "Attempting to fetch all projects from API")
            val response = projectService.getAllProjects()
            Log.d("ProjectRepository", "API response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val projects = response.body()
                Log.d("ProjectRepository", "Received ${projects?.size ?: 0} projects from API")
                
                if (projects != null) {
                    // Clear existing data
                    Log.d("ProjectRepository", "Clearing existing data from local database")
                    projectDao.deleteAllProjects()
                    taskDao.deleteAllTasks()
                    userDao.deleteAllUsers()
                    
                    // Insert new data
                    Log.d("ProjectRepository", "Inserting ${projects.size} projects into local database")
                    projects.forEach { project ->
                        // Create a copy of the project with empty lists if tasks or members are null
                        val projectWithDefaults = project.copy(
                            tasks = project.tasks ?: emptyList(),
                            members = project.members ?: emptyList()
                        )
                        val projectEntity = ProjectEntity.fromProject(projectWithDefaults.copy(isSynced = true))
                        projectDao.insertProject(projectEntity)
                        
                        projectWithDefaults.tasks.forEach { task ->
                            val taskEntity = TaskEntity.fromTask(task.copy(isSynced = true))
                            taskDao.insertTask(taskEntity)
                        }
                        
                        projectWithDefaults.members.forEach { member ->
                            val userEntity = UserEntity.fromUser(member.copy(isSynced = true))
                            userDao.insertUser(userEntity)
                        }
                    }
                    Log.d("ProjectRepository", "Successfully refreshed projects")
                    Result.success(Unit)
                } else {
                    Log.e("ProjectRepository", "Empty response body from API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                Log.e("ProjectRepository", "API call failed with code ${response.code()}, error: ${response.errorBody()?.string()}")
                Result.failure(Exception("Failed to fetch projects: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error refreshing projects", e)
            Result.failure(e)
        }
    }
} 