package com.taskermobile.ui.main.controllers

import android.content.Context
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.data.model.Task
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.service.TaskService
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import com.taskermobile.data.local.dao.TaskDao

class TaskController(context: Context, sessionManager: SessionManager) {
    private val taskService = RetrofitClient.createService<TaskService>(sessionManager)
    private val taskDao = TaskerDatabase.getInstance(context).taskDao()
    private val taskRepository = TaskRepository(taskDao, taskService)

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
        return taskRepository.getTasksByProject(projectId)
    }

    fun getTasksByUser(userId: Long): Flow<List<Task>> {
        return taskRepository.getTasksByUser(userId)
    }
}