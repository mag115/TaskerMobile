package `is`.hbv501g.taskermobile.data.service

import `is`.hbv501g.taskermobile.data.local.dao.TaskDao
import `is`.hbv501g.taskermobile.data.local.entity.TaskEntity
import `is`.hbv501g.taskermobile.data.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException


class TaskServiceImpl(
    private val taskDao: TaskDao,
    private val taskApi: TaskService
) {
    // Local operations – exposing a Flow of tasks
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
            val response = taskApi.createTask(task)
            if (response.isSuccessful) {
                response.body()?.data?.let { remoteTask ->
                    // Save as synced in the local database
                    val taskEntity = TaskEntity.fromTask(remoteTask).copy(isSynced = true)
                    taskDao.insertTask(taskEntity)
                    Result.success(remoteTask)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                // Save locally as unsynced if remote call fails
                val taskEntity = TaskEntity.fromTask(task).copy(isSynced = false)
                taskDao.insertTask(taskEntity)
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            // On exception, save locally as unsynced
            val taskEntity = TaskEntity.fromTask(task).copy(isSynced = false)
            taskDao.insertTask(taskEntity)
            Result.failure(e)
        }
    }

    // Synchronize tasks that are not yet synced with the server
    suspend fun syncUnsyncedTasks() {
        val unsyncedTasks = taskDao.getUnsyncedTasks()
        for (taskEntity in unsyncedTasks) {
            try {
                val response = taskApi.createTask(taskEntity.toTask())
                if (response.isSuccessful) {
                    taskDao.markTaskAsSynced(taskEntity.id)
                }
            } catch (e: Exception) {
                // Log or handle the error for this task – retry later
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