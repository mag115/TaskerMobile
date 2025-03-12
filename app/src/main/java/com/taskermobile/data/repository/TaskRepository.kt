package com.taskermobile.data.repository

import com.taskermobile.data.local.dao.TaskDao
import com.taskermobile.data.local.dao.ProjectDao
import com.taskermobile.data.local.dao.UserDao
import com.taskermobile.data.local.entity.TaskEntity
import com.taskermobile.data.local.entity.ProjectEntity
import com.taskermobile.data.model.Task
import com.taskermobile.data.service.TaskService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import retrofit2.HttpException
import android.util.Log
import com.taskermobile.data.local.mapper.toEntity
import kotlinx.coroutines.flow.flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskService: TaskService,
    private val projectDao: ProjectDao,
    private val userDao: UserDao
) {

    private suspend fun refreshTasks() {
        try {
            // Fetch from remote
            val response = taskService.getAllTasks(0) // 0 means all tasks
            if (response.isSuccessful) {
                response.body()?.let { tasks ->
                    // Convert to entities and mark as synced
                    val entities = tasks.map { TaskEntity.fromTask(it).copy(isSynced = true) }
                    taskDao.insertTasks(entities)
                }
            }
        } catch (e: Exception) {
            // Ignore network errors - we'll just use local data
        }
    }

    suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)?.toTask()
    }

    suspend fun insertTask(task: Task) {
        val taskEntity = TaskEntity.fromTask(task)

        Log.d("TaskRepository", "Attempting to insert task: $taskEntity") // Debugging log

        taskDao.insertTask(taskEntity)

        val insertedTask = taskDao.getTaskById(taskEntity.id ?: -1)
        Log.d("TaskRepository", "Inserted Task from DB: $insertedTask") // Confirm insertion

        syncTaskWithBackend(taskEntity)
    }




    private suspend fun syncTaskWithBackend(task: TaskEntity) {
        try {
            //send task to the server using Retrofit API
            val projectId: Long = task.projectId
            val assignedUserId: Long? = task.assignedUserId
            val taskRequest = task.toTask()
            val response = taskService.createTask(taskRequest, projectId, assignedUserId)
            if (response.isSuccessful) {
                Log.d("TaskRepository", "Task synced with server: ${response.body()}")
            } else {
                Log.d("TaskRepository", "Failed to sync task with server: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error syncing task with server", e)
        }
    }


    suspend fun updateTask(task: Task) {
        val taskEntity = TaskEntity.fromTask(task)
        taskDao.updateTask(taskEntity)
    }

    suspend fun deleteTask(task: Task) {
        val taskEntity = TaskEntity.fromTask(task)
        taskDao.deleteTask(taskEntity)
    }

    fun getTasksByProject(projectId: Long): Flow<List<Task>> {
        Log.d("TaskRepository", "Setting up flow for project ID: $projectId")
        return taskDao.getTasksByProject(projectId)
            .map { entities ->
                Log.d("TaskRepository", "Mapping ${entities.size} entities from database")
                entities.map { it.toTask() }
            }
            .onStart {
                try {
                    Log.d("TaskRepository", "Starting flow, fetching tasks from API for project: $projectId")
                    val response = taskService.getAllTasks(projectId)
                    if (response.isSuccessful) {
                        response.body()?.let { tasks ->
                            // Filter tasks by project (if necessary)
                            val filteredTasks = tasks.filter { task ->
                                val taskProjectId = task.project?.id ?: task.projectId
                                taskProjectId == projectId
                            }
                            Log.d("TaskRepository", "API returned ${filteredTasks.size} tasks for project $projectId")
                            // Convert to entities and update local cache
                            val entities = filteredTasks.map { task ->
                                TaskEntity.fromTask(task.copy(projectId = projectId)).copy(isSynced = true)
                            }
                            if (entities.isNotEmpty()) {
                                taskDao.insertTasks(entities)
                                Log.d("TaskRepository", "Updated local database with ${entities.size} tasks")
                            }
                        }
                    } else {
                        Log.d("TaskRepository", "API call failed: ${response.code()} - using cached data")
                    }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    Log.d("TaskRepository", "Error fetching tasks from API, using cached data", e)
                }
            }
    }


    suspend fun refreshProjectTasks(projectId: Long) {
        try {
            Log.d("TaskRepository", "Fetching tasks from API for project: $projectId")
            val response = taskService.getAllTasks(projectId)
            if (response.isSuccessful) {
                Log.d("TaskRepository", "API call successful. Response code: ${response.code()}")
                response.body()?.let { tasks ->
                    Log.d("TaskRepository", "Received ${tasks.size} tasks from API")
                    val entities = tasks.map { TaskEntity.fromTask(it).copy(isSynced = true) }
                    taskDao.insertTasks(entities)
                    Log.d("TaskRepository", "Saved tasks to local database")
                } ?: Log.e("TaskRepository", "Response body was null")
            } else {
                Log.e("TaskRepository", "API call failed. Response code: ${response.code()}, Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error fetching tasks from API", e)
            // Ignore network errors - we'll just use local data
        }
    }

    //fun getTasksByUser(userId: Long, projectId: Long): Flow<List<Task>> {
      //  return taskDao.getTasksByUser(userId, projectId) // Ensure TaskDao supports this
        //    .map { entities -> entities.map { it.toTask() } }
    //}

    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
            .map { entities ->
                Log.d("TaskRepository", "Emitting ${entities.size} tasks from DB") // 🛠️ Debugging log
                entities.map { it.toTask() }
            }
    }


    fun getAllTasksForUser(username: String): Flow<List<Task>> {
        return flow {
            val userId = userDao.getUserIdByUsername(username)
            // Fetch tasks for the user with the given userId
            taskDao.getAllTasksForUser(userId)
                .map { entities -> entities.map { it.toTask() } }
                .onStart { refreshTasks() }
                .collect { tasks ->
                    emit(tasks) // Emit the tasks to the flow
                }
        }
    }


    fun getTasksByUsername(username: String): Flow<List<Task>> {
        return flow {
            // Get the user ID by username
            val userId = userDao.getUserIdByUsername(username)  // Fetch the userId using the username
            if (userId != null) {
                // If the user is found, fetch tasks by assignedUserId
                taskDao.getTasksByUser(userId).collect { tasks ->
                    emit(tasks.map { it.toTask() }) // Convert TaskEntity to Task
                }
            } else {
                emit(emptyList()) // Emit empty list if the user is not found
            }
        }
    }

    // Remote operations with local caching
    suspend fun createTaskWithSync(task: Task): Result<Task> {
        return try {
            val projectId: Long = task.projectId
            val assignedUserId: Long? = task.assignedUserId
            val taskRequest = task
            val response = taskService.createTask(taskRequest, projectId, assignedUserId)
            if (response.isSuccessful) {
                response.body()?.let { taskResponse ->
                    if (taskResponse.success && taskResponse.data != null) {
                        // Save to local database
                        val taskEntity = TaskEntity.fromTask(taskResponse.data).copy(isSynced = true)
                        taskDao.insertTask(taskEntity)
                        Result.success(taskResponse.data)
                    } else {
                        // Save to local database with isSynced = false
                        val taskEntity = TaskEntity.fromTask(task).copy(isSynced = false)
                        taskDao.insertTask(taskEntity)
                        Result.failure(Exception(taskResponse.message ?: "Unknown error"))
                    }
                } ?: run {
                    // Save to local database with isSynced = false
                    val taskEntity = TaskEntity.fromTask(task).copy(isSynced = false)
                    taskDao.insertTask(taskEntity)
                    Result.failure(Exception("Empty response body"))
                }
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
                val projectId: Long = taskEntity.projectId
                val assignedUserId: Long? = taskEntity.assignedUserId
                val taskRequest = taskEntity.toTask()
                val response = taskService.createTask(taskRequest, projectId, assignedUserId)
                if (response.isSuccessful) {
                    response.body()?.let { taskResponse ->
                        if (taskResponse.success && taskResponse.data != null) {
                            taskEntity.id?.let { id ->
                                taskDao.markTaskAsSynced(id)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle exception (retry later, notify user, etc.)
            }
        }
    }
} 