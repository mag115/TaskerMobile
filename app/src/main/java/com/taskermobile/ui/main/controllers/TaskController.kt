package com.taskermobile.ui.main.controllers

import android.content.Context
import android.util.Log
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.data.model.Task
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.service.TaskService
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import com.taskermobile.data.local.dao.TaskDao

class TaskController(context: Context, sessionManager: SessionManager) {
    private val taskService = RetrofitClient.createService<TaskService>(sessionManager)
    private val database = TaskerDatabase.getDatabase(context)
    private val taskDao = database.taskDao()
    private val projectDao = database.projectDao()
    private val taskRepository = TaskRepository(taskDao, taskService, projectDao)

    suspend fun createTask(task: Task): Result<Task> {
        return try {
            taskRepository.createTaskWithSync(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllTasks(): Flow<List<Task>> {
        return taskRepository.getAllTasks()
    }

    suspend fun getTaskById(taskId: Long): Task? {
        return taskRepository.getTaskById(taskId)
    }

    suspend fun updateTask(task: Task) {
        taskRepository.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskRepository.deleteTask(task)
    }

    suspend fun syncTasks() {
        taskRepository.syncUnsyncedTasks()
    }

    fun getTasksByProject(projectId: Long): Flow<List<Task>> {
        Log.d("TaskController", "Getting tasks for project ID: $projectId")
        return taskRepository.getTasksByProject(projectId)
            .onEach { tasks ->
                Log.d("TaskController", "Received ${tasks.size} tasks from repository")
                tasks.forEach { task ->
                    Log.d("TaskController", "Task: ${task}")
                }
            }
    }

    suspend fun refreshProjectTasks(projectId: Long) {
        Log.d("TaskController", "Manually refreshing tasks for project ID: $projectId")
        taskRepository.refreshProjectTasks(projectId)
    }

    fun getTasksByUser(userId: Long): Flow<List<Task>> {
        return taskRepository.getTasksByUser(userId)
    }
}