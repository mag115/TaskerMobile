package `is`.hbv501g.taskermobile.data.repository

import `is`.hbv501g.taskermobile.data.local.dao.TaskDao
import `is`.hbv501g.taskermobile.data.local.entity.TaskEntity
import `is`.hbv501g.taskermobile.data.model.Task
import `is`.hbv501g.taskermobile.data.service.TaskService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskService: TaskService
) {
    // Local operations
    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toTask() }
        }
    }

    suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)?.toTask()
    }

    suspend fun insertTask(task: Task) {
        val taskEntity = TaskEntity.fromTask(task)
        taskDao.insertTask(taskEntity)
    }

    suspend fun updateTask(task: Task) {
        val taskEntity = TaskEntity.fromTask(task)
        taskDao.updateTask(taskEntity)
    }

    suspend fun deleteTask(task: Task) {
        val taskEntity = TaskEntity.fromTask(task)
        taskDao.deleteTask(taskEntity)
    }

    // Remote operations with local caching
    suspend fun createTaskWithSync(task: Task): Result<Task> {
        return try {
            val response = taskService.createTask(task)
            if (response.isSuccessful) {
                response.body()?.data?.let { remoteTask ->
                    // Save to local database
                    val taskEntity = TaskEntity.fromTask(remoteTask).copy(isSynced = true)
                    taskDao.insertTask(taskEntity)
                    Result.success(remoteTask)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                // Save to local database with isSynced = false
                val taskEntity = TaskEntity.fromTask(task).copy(isSynced = false)
                taskDao.insertTask(taskEntity)
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            // Save to local database with isSynced = false
            val taskEntity = TaskEntity.fromTask(task).copy(isSynced = false)
            taskDao.insertTask(taskEntity)
            Result.failure(e)
        }
    }

    // Sync operations
    suspend fun syncUnsyncedTasks() {
        val unsyncedTasks = taskDao.getUnsyncedTasks()
        for (taskEntity in unsyncedTasks) {
            try {
                val response = taskService.createTask(taskEntity.toTask())
                if (response.isSuccessful) {
                    taskDao.markTaskAsSynced(taskEntity.id)
                }
            } catch (e: Exception) {
                // Handle exception (retry later, notify user, etc.)
            }
        }
    }

    fun getTasksByProject(projectId: Long): Flow<List<Task>> {
        return taskDao.getTasksByProject(projectId).map { entities ->
            entities.map { it.toTask() }
        }
    }

    fun getTasksByUser(userId: Long): Flow<List<Task>> {
        return taskDao.getTasksByUser(userId).map { entities ->
            entities.map { it.toTask() }
        }
    }
} 