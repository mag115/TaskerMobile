package com.taskermobile.ui.main.controllers

import android.content.Context
import android.util.Log
import com.taskermobile.data.api.RetroFitClient
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.data.model.Task
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.service.TaskService
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.app.Application

class TaskController(context: Context, sessionManager: SessionManager, application: Application) {
    private val taskService = RetroFitClient.createService<TaskService>(application, sessionManager)
    private val database = TaskerDatabase.getDatabase(context)
    private val taskDao = database.taskDao()
    private val projectDao = database.projectDao()
    private val userDao = database.userDao()
    private val notificationDao = database.notificationDao()
    private val taskRepository = TaskRepository(taskDao, taskService, userDao, notificationDao, projectDao)

    suspend fun createTask(task: Task): Result<Task> {
        val project = projectDao.getProjectById(task.projectId)
        if (project == null) {
            Log.e("TaskController", "Project ID ${task.projectId} does not exist.")
            return Result.failure(Exception("Project ID ${task.projectId} does not exist"))
        }

        return taskRepository.createTaskWithSync(task)
    }


    fun getAllTasks(): Flow<List<Task>> {
        return taskRepository.getAllTasks()
            .map { tasks ->
                Log.d("TaskController", "Sending ${tasks.size} tasks to UI")
                tasks
            }
    }

    suspend fun getTaskById(taskId: Long): Task? = taskRepository.getTaskById(taskId)

    suspend fun updateTask(task: Task) = taskRepository.updateTask(task)

    suspend fun deleteTask(task: Task) = taskRepository.deleteTask(task)

    suspend fun syncTasks() = taskRepository.syncUnsyncedTasks()

    fun getTasksByProject(projectId: Long): Flow<List<Task>> = taskRepository.getTasksByProject(projectId)

    suspend fun refreshProjectTasks(projectId: Long) {
        Log.d("TaskController", "Manually refreshing tasks for project ID: $projectId")
        taskRepository.refreshProjectTasks(projectId)
    }

    suspend fun addCommentToTask(taskId: Long, comment: String) {
        try {
            val task = taskDao.getTaskById(taskId)?.toTask()
            if (task != null) {
                task.comments.add(comment)
                taskRepository.updateTask(task)
            }
        } catch (e: Exception) {
            Log.e("TaskController", "Error adding comment", e)
        }
    }

    suspend fun fetchTasksFromApi(projectId: Long?): List<Task>? {
        Log.d("TaskController", "Fetching tasks from API for projectId: $projectId")
        try {
            val response = taskService.getAllTasks(projectId ?: 0)
            if (response.isSuccessful) {
                val tasks = response.body()
                Log.d("TaskController", "Successfully fetched ${tasks?.size ?: 0} tasks from API")
                return tasks
            } else {
                Log.e("TaskController", "API call failed: ${response.code()}")
                return null
            }
        } catch (e: Exception) {
            Log.e("TaskController", "Error fetching tasks from API", e)
            return null
        }
    }
}
