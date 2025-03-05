package `is`.hbv501g.taskermobile.ui.controllers

import android.content.Context
import `is`.hbv501g.taskermobile.data.api.RetrofitClient
import `is`.hbv501g.taskermobile.data.local.TaskerDatabase
import `is`.hbv501g.taskermobile.data.model.Task
import `is`.hbv501g.taskermobile.data.repository.TaskRepository
import `is`.hbv501g.taskermobile.data.service.TaskService
import `is`.hbv501g.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.Flow

class TaskController(context: Context, sessionManager: SessionManager) {
    private val taskService = RetrofitClient.createService<TaskService>(sessionManager)
    private val taskDao = TaskerDatabase.getDatabase(context).taskDao()
    private val taskRepository = TaskRepository(taskDao, taskService)

    suspend fun createTask(task: Task): Result<Task> {
        return taskRepository.createTaskWithSync(task)
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