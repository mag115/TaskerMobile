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
import com.taskermobile.data.local.dao.NotificationDao
import com.taskermobile.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.firstOrNull

class TaskRepository(
    private val taskDao: TaskDao,
    private val taskService: TaskService,
    private val userDao: UserDao,
    private val notificationDao: NotificationDao,
    private val projectDao: ProjectDao? = null  
) {

    suspend fun refreshTasks() {
        try {
            Log.d("TaskRepository", "Starting refreshTasks")
            val response = taskService.getAllTasks(0)
            if (response.isSuccessful) {
                val apiTasks = response.body() ?: emptyList()
                Log.d("TaskRepository", "Received ${apiTasks.size} tasks from API")
                
                // Get current tasks from database
                val localTasks = taskDao.getAllTasksSync()
                Log.d("TaskRepository", "Found ${localTasks.size} tasks in local database")
                
                // Create a map of local tasks for quick lookup
                val localMap = localTasks.associateBy { it.id }
                Log.d("TaskRepository", "Local task IDs: ${localMap.keys}")

                // First, ensure all projects exist in the database
                val projects = apiTasks.mapNotNull { it.project }.distinctBy { it.id }
                Log.d("TaskRepository", "Found ${projects.size} unique projects in API response")
                
                if (projects.isNotEmpty()) {
                    try {
                        // Convert to project entities and insert
                        val projectEntities = projects.map { com.taskermobile.data.local.entity.ProjectEntity.fromProject(it) }
                        Log.d("TaskRepository", "Saving ${projectEntities.size} projects to database")
                        
                        if (projectDao != null) {
                            projectDao.insertProjects(projectEntities)
                            Log.d("TaskRepository", "Successfully saved projects to database")
                            
                            // Verify projects were saved
                            val savedProjects = projectDao.getAllProjects().firstOrNull() ?: emptyList()
                            Log.d("TaskRepository", "After save, database contains ${savedProjects.size} projects")
                            Log.d("TaskRepository", "Saved project IDs: ${savedProjects.map { it.id }}")
                        } else {
                            // Fallback to inserting via userDao if projectDao not available
                            projectEntities.forEach { entity ->
                                try {
                                    userDao.insertProjectIfNotExists(entity.id ?: 0, entity.name)
                                    Log.d("TaskRepository", "Successfully saved project via userDao: ${entity.name} (ID: ${entity.id})")
                                } catch (e: Exception) {
                                    Log.w("TaskRepository", "Could not insert project ${entity.id}: ${e.message}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("TaskRepository", "Error saving projects: ${e.message}")
                    }
                }

                // Ensure all users exist in the database
                val users = apiTasks.mapNotNull { it.assignedUser }.distinctBy { it.id }
                Log.d("TaskRepository", "Found ${users.size} unique users in API response")
                
                if (users.isNotEmpty()) {
                    try {
                        // Insert users
                        users.forEach { user ->
                            try {
                                userDao.insertUserIfNotExists(user.id ?: 0, user.username)
                                Log.d("TaskRepository", "Successfully saved user: ${user.username} (ID: ${user.id})")
                            } catch (e: Exception) {
                                Log.w("TaskRepository", "Could not insert user ${user.id}: ${e.message}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("TaskRepository", "Error saving users: ${e.message}")
                    }
                }

                // Convert API tasks to entities and merge with local data
                val mergedEntities = apiTasks.map { apiTask ->
                    val local = localMap[apiTask.id]
                    if (local != null) {
                        // If task exists locally, merge the data
                        TaskEntity.fromTask(apiTask).copy(
                            imageUri = local.imageUri,
                            isSynced = true,
                            comments = local.comments,
                            timeSpent = local.timeSpent,
                            elapsedTime = local.elapsedTime
                        )
                    } else {
                        // If task doesn't exist locally, create new entity
                        TaskEntity.fromTask(apiTask).copy(isSynced = true)
                    }
                }
                Log.d("TaskRepository", "Created ${mergedEntities.size} merged entities")

                // Add any local tasks that don't exist in the API
                val localOnlyTasks = localTasks.filter { localTask ->
                    apiTasks.none { it.id == localTask.id }
                }
                Log.d("TaskRepository", "Found ${localOnlyTasks.size} local-only tasks")

                // Combine merged API tasks and local-only tasks
                val allTasks = mergedEntities + localOnlyTasks
                Log.d("TaskRepository", "Total tasks to save: ${allTasks.size}")

                // Save all tasks to database
                try {
                    taskDao.insertTasks(allTasks)
                    Log.d("TaskRepository", "Successfully saved tasks to database")
                } catch (e: Exception) {
                    Log.e("TaskRepository", "Failed to save tasks to database", e)
                    // Try to save tasks one by one to identify which one fails
                    allTasks.forEach { task ->
                        try {
                            taskDao.insertTask(task)
                            Log.d("TaskRepository", "Successfully saved task: ${task.title} (ID: ${task.id})")
                        } catch (e: Exception) {
                            Log.e("TaskRepository", "Failed to save task: ${task.title} (ID: ${task.id})", e)
                            Log.e("TaskRepository", "Task details - Project ID: ${task.projectId}, Assigned User ID: ${task.assignedUserId}")
                        }
                    }
                }
                
                // Verify the save
                val savedTasks = taskDao.getAllTasksSync()
                Log.d("TaskRepository", "After save, database contains ${savedTasks.size} tasks")
                Log.d("TaskRepository", "Saved task IDs: ${savedTasks.map { it.id }}")
                
                Log.d("TaskRepository", "Successfully merged ${mergedEntities.size} API tasks with ${localOnlyTasks.size} local-only tasks")
            } else {
                Log.e("TaskRepository", "API call failed: ${response.code()} - ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "refreshTasks failed", e)
            Log.e("TaskRepository", "Stack trace: ${e.stackTraceToString()}")
        }
    }

    suspend fun syncAndGetAllTasksWithLocalMerge(): Flow<List<Task>> {
        try {
            val apiResponse = taskService.getAllTasks(0)
            if (apiResponse.isSuccessful) {
                val apiTasks = apiResponse.body() ?: emptyList()
                val localTasks = taskDao.getAllTasks().firstOrNull() ?: emptyList()
                val localTasksMap = localTasks.associateBy { it.id }

                // Merge local and API tasks, preserving local data
                val mergedTasks = apiTasks.map { apiTask ->
                    val local = localTasksMap[apiTask.id]
                    if (local != null) {
                        // If task exists locally, merge the data
                        TaskEntity.fromTask(apiTask).copy(
                            imageUri = local.imageUri,
                            isSynced = true,
                            comments = local.comments,
                            timeSpent = local.timeSpent,
                            elapsedTime = local.elapsedTime
                        )
                    } else {
                        // If task doesn't exist locally, create new entity
                        TaskEntity.fromTask(apiTask).copy(isSynced = true)
                    }
                }

                // Add any local tasks that don't exist in the API
                val localOnlyTasks = localTasks.filter { localTask ->
                    apiTasks.none { it.id == localTask.id }
                }

                // Combine merged API tasks and local-only tasks
                val allTasks = mergedTasks + localOnlyTasks
                Log.d("MergedTasks", "Saving: ${allTasks.size} tasks (${mergedTasks.size} from API, ${localOnlyTasks.size} local-only)")

                taskDao.insertTasks(allTasks)
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "syncAndGetAllTasksWithLocalMerge failed", e)
        }

        return taskDao.getAllTasks().map { it.map { entity -> entity.toTask() } }
    }

    suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)?.toTask()
    }

    suspend fun insertTask(task: Task) {
        Log.d("TaskRepository", "Inserting task locally: ${task.title}")
        createTaskWithSync(task)
    }

    private suspend fun syncTaskWithBackend(task: TaskEntity) {
        try {
            val projectId: Long = task.projectId ?: 0L
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

    suspend fun addCommentToTask(taskId: Long, comment: String) {
        val task = taskDao.getTaskById(taskId)
        if (task != null) {
            task.comments.add(comment)
            taskDao.updateTask(task)
        }
    }

    suspend fun addNotification(notification: NotificationEntity) {
        notificationDao.insertNotification(notification)
    }

    suspend fun updateTask(task: Task) {
        val taskEntity = TaskEntity.fromTask(task)
        taskDao.updateTask(taskEntity)
        Log.d("TaskRepo", "Saving task ${task.id} with imageUri = ${task.imageUri}")

        // Sync status update with backend
        try {
            val statusMap = mapOf("status" to task.status)
            val response = taskService.updateTaskStatus(task.id ?: 0L, statusMap)
            if (response.isSuccessful) {
                Log.d("TaskRepository", "Task status synced with server: ${response.body()}")
            } else {
                Log.e("TaskRepository", "Failed to sync task status with server: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Error syncing task status with server", e)
        }
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
                            val filteredTasks = tasks.filter { task ->
                                val taskProjectId = task.project?.id ?: task.projectId
                                taskProjectId == projectId
                            }
                            Log.d("TaskRepository", "API returned ${filteredTasks.size} tasks for project $projectId")
                            val localTasks = taskDao.getTasksByProject(projectId).firstOrNull() ?: emptyList()
                            val localMap = localTasks.associateBy { it.id }

                            val entities = tasks.map { task ->
                                val local = localMap[task.id]
                                TaskEntity.fromTask(task.copy(projectId = projectId)).copy(
                                    imageUri = local?.imageUri,
                                    isSynced = true
                                )
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
                Log.d("TaskRepository", "Retrieving ${entities.size} tasks from DB")
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
        Log.d("TaskRepository", "Creating task: ${task.title}, projectId: ${task.projectId}, assignedUserId: ${task.assignedUserId}")
        return try {
            // First check if project exists
            if (projectDao != null) {
                val project = projectDao.getProjectById(task.projectId)
                if (project == null) {
                    Log.e("TaskRepository", "Project ${task.projectId} does not exist in local database")
                    return Result.failure(Exception("Project ${task.projectId} does not exist"))
                }
                Log.d("TaskRepository", "Found project: ${project.name} (ID: ${project.id})")
            }

            val projectId: Long = task.projectId
            val assignedUserId: Long? = task.assignedUserId
            val taskRequest = task
            
            Log.d("TaskRepository", "Sending task creation request to API")
            val response = taskService.createTask(taskRequest, projectId, assignedUserId)
            
            if (response.isSuccessful) {
                Log.d("TaskRepository", "API response successful: ${response.code()}")
                response.body()?.let { taskResponse ->
                    if (taskResponse.success && taskResponse.data != null) {
                        Log.d("TaskRepository", "Task created successfully on backend with id: ${taskResponse.data.id}")
                        // Save to local database with isSynced = true
                        val taskEntity = TaskEntity.fromTask(taskResponse.data).copy(isSynced = true)
                        try {
                            Log.d("TaskRepository", "Attempting to save task to local database: ${taskEntity.title} (ID: ${taskEntity.id})")
                            taskDao.insertTask(taskEntity)
                            
                            // Verify the task was saved
                            val savedTask = taskDao.getTaskById(taskEntity.id ?: 0)
                            if (savedTask != null) {
                                Log.d("TaskRepository", "Successfully verified task in database: ${savedTask.title}")
                                // Double check by getting all tasks
                                val allTasks = taskDao.getAllTasksSync()
                                Log.d("TaskRepository", "Current total tasks in database: ${allTasks.size}")
                                Log.d("TaskRepository", "Task IDs in database: ${allTasks.map { it.id }}")
                                return Result.success(taskResponse.data)
                            } else {
                                Log.e("TaskRepository", "Failed to verify task in database after insertion")
                                return Result.failure(Exception("Failed to verify task in database"))
                            }
                        } catch (e: Exception) {
                            Log.e("TaskRepository", "Failed to save task to local database", e)
                            return Result.failure(e)
                        }
                    } else {
                        Log.w("TaskRepository", "API returned success=false: ${taskResponse.message}")
                        // Save to local database with isSynced = false
                        val taskEntity = TaskEntity.fromTask(task).copy(isSynced = false)
                        try {
                            Log.d("TaskRepository", "Attempting to save unsynced task to local database: ${taskEntity.title}")
                            taskDao.insertTask(taskEntity)
                            
                            // Verify the task was saved
                            val savedTask = taskDao.getTaskById(taskEntity.id ?: 0)
                            if (savedTask != null) {
                                Log.d("TaskRepository", "Successfully verified unsynced task in database: ${savedTask.title}")
                                // Double check by getting all tasks
                                val allTasks = taskDao.getAllTasksSync()
                                Log.d("TaskRepository", "Current total tasks in database: ${allTasks.size}")
                                Log.d("TaskRepository", "Task IDs in database: ${allTasks.map { it.id }}")
                                return Result.failure(Exception(taskResponse.message ?: "Unknown error"))
                            } else {
                                Log.e("TaskRepository", "Failed to verify unsynced task in database after insertion")
                                return Result.failure(Exception("Failed to verify task in database"))
                            }
                        } catch (e: Exception) {
                            Log.e("TaskRepository", "Failed to save unsynced task to local database", e)
                            return Result.failure(e)
                        }
                    }
                } ?: run {
                    Log.w("TaskRepository", "API response body was null")
                    // Save to local database with isSynced = false
                    val taskEntity = TaskEntity.fromTask(task).copy(isSynced = false)
                    try {
                        Log.d("TaskRepository", "Attempting to save task with null response to local database: ${taskEntity.title}")
                        taskDao.insertTask(taskEntity)
                        
                        // Verify the task was saved
                        val savedTask = taskDao.getTaskById(taskEntity.id ?: 0)
                        if (savedTask != null) {
                            Log.d("TaskRepository", "Successfully verified task with null response in database: ${savedTask.title}")
                            // Double check by getting all tasks
                            val allTasks = taskDao.getAllTasksSync()
                            Log.d("TaskRepository", "Current total tasks in database: ${allTasks.size}")
                            Log.d("TaskRepository", "Task IDs in database: ${allTasks.map { it.id }}")
                            return Result.failure(Exception("Empty response body"))
                        } else {
                            Log.e("TaskRepository", "Failed to verify task with null response in database after insertion")
                            return Result.failure(Exception("Failed to verify task in database"))
                        }
                    } catch (e: Exception) {
                        Log.e("TaskRepository", "Failed to save task with null response to local database", e)
                        return Result.failure(e)
                    }
                }
            } else {
                Log.e("TaskRepository", "API call failed: ${response.code()} - ${response.errorBody()?.string()}")
                // Save to local database with isSynced = false
                val taskEntity = TaskEntity.fromTask(task).copy(isSynced = false)
                try {
                    Log.d("TaskRepository", "Attempting to save task after API failure to local database: ${taskEntity.title}")
                    taskDao.insertTask(taskEntity)
                    
                    // Verify the task was saved
                    val savedTask = taskDao.getTaskById(taskEntity.id ?: 0)
                    if (savedTask != null) {
                        Log.d("TaskRepository", "Successfully verified task after API failure in database: ${savedTask.title}")
                        // Double check by getting all tasks
                        val allTasks = taskDao.getAllTasksSync()
                        Log.d("TaskRepository", "Current total tasks in database: ${allTasks.size}")
                        Log.d("TaskRepository", "Task IDs in database: ${allTasks.map { it.id }}")
                        return Result.failure(HttpException(response))
                    } else {
                        Log.e("TaskRepository", "Failed to verify task after API failure in database after insertion")
                        return Result.failure(Exception("Failed to verify task in database"))
                    }
                } catch (e: Exception) {
                    Log.e("TaskRepository", "Failed to save task after API failure to local database", e)
                    return Result.failure(e)
                }
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Exception creating task: ${e.message}")
            Log.e("TaskRepository", "Stack trace: ${e.stackTraceToString()}")
            // Save to local database with isSynced = false
            val taskEntity = TaskEntity.fromTask(task).copy(isSynced = false)
            try {
                Log.d("TaskRepository", "Attempting to save task after exception to local database: ${taskEntity.title}")
                taskDao.insertTask(taskEntity)
                
                // Verify the task was saved
                val savedTask = taskDao.getTaskById(taskEntity.id ?: 0)
                if (savedTask != null) {
                    Log.d("TaskRepository", "Successfully verified task after exception in database: ${savedTask.title}")
                    // Double check by getting all tasks
                    val allTasks = taskDao.getAllTasksSync()
                    Log.d("TaskRepository", "Current total tasks in database: ${allTasks.size}")
                    Log.d("TaskRepository", "Task IDs in database: ${allTasks.map { it.id }}")
                    return Result.failure(e)
                } else {
                    Log.e("TaskRepository", "Failed to verify task after exception in database after insertion")
                    return Result.failure(Exception("Failed to verify task in database"))
                }
            } catch (dbError: Exception) {
                Log.e("TaskRepository", "Failed to save task after exception to local database", dbError)
                return Result.failure(dbError)
            }
        }
    }

    // Sync operations
    suspend fun syncUnsyncedTasks() {
        Log.d("TaskRepository", "Starting sync of unsynced tasks")
        val unsyncedTasks = taskDao.getUnsyncedTasks()
        Log.d("TaskRepository", "Found ${unsyncedTasks.size} unsynced tasks")
        
        for (taskEntity in unsyncedTasks) {
            try {
                val projectId: Long = taskEntity.projectId ?: 0L
                val assignedUserId: Long? = taskEntity.assignedUserId
                val taskRequest = taskEntity.toTask()
                
                Log.d("TaskRepository", "Syncing task '${taskEntity.title}' with ID ${taskEntity.id}")
                val response = taskService.createTask(taskRequest, projectId, assignedUserId)
                
                if (response.isSuccessful) {
                    response.body()?.let { taskResponse ->
                        if (taskResponse.success && taskResponse.data != null) {
                            taskEntity.id?.let { id ->
                                Log.d("TaskRepository", "Task synced successfully, marking as synced: $id")
                                taskDao.markTaskAsSynced(id)
                                
                                if (taskResponse.data.id != null && taskResponse.data.id != id) {
                                    Log.d("TaskRepository", "Updating local task ID from $id to ${taskResponse.data.id}")
                                    val updatedEntity = TaskEntity.fromTask(taskResponse.data).copy(isSynced = true)
                                    taskDao.updateTask(updatedEntity)
                                }
                            }
                        } else {
                            Log.w("TaskRepository", "Backend rejected task: ${taskResponse.message}")
                        }
                    }
                } else {
                    Log.e("TaskRepository", "Failed to sync task: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("TaskRepository", "Error syncing task ${taskEntity.id}", e)
            }
        }
        Log.d("TaskRepository", "Completed sync operation")
    }
    suspend fun updateTaskTime(taskId: Long, timeSpent: Double): retrofit2.Response<Task> {
        val timeRequest = hashMapOf<String, Any>(
            "taskId" to taskId,
            "timeSpent" to timeSpent as Any
        )
        return taskService.updateTime(timeRequest)
    }

    fun getAssignedTasks(projectId: Long? = null): Flow<List<Task>> {
        Log.d("TaskRepository", "Getting assigned tasks. Project ID: $projectId")
        return flow {
            try {
                // First emit from cache
                val cachedTasks = if (projectId != null) {
                    taskDao.getTasksByProject(projectId).firstOrNull() ?: emptyList()
                } else {
                    taskDao.getAllTasks().firstOrNull() ?: emptyList()
                }
                
                emit(cachedTasks.map { it.toTask() })
                Log.d("TaskRepository", "Emitted ${cachedTasks.size} cached tasks")
                
                // Then fetch from API
                Log.d("TaskRepository", "Fetching assigned tasks from API")
                val response = taskService.getAssignedTasks(projectId)
                
                if (response.isSuccessful) {
                    val apiTasks = response.body() ?: emptyList()
                    Log.d("TaskRepository", "API returned ${apiTasks.size} assigned tasks")
                    
                    // Extract and save projects first
                    val projects = apiTasks.mapNotNull { it.project }.distinctBy { it.id }
                    if (projects.isNotEmpty()) {
                        Log.d("TaskRepository", "Saving ${projects.size} projects from task response")
                        try {
                            // Convert to project entities and insert
                            val projectEntities = projects.map { com.taskermobile.data.local.entity.ProjectEntity.fromProject(it) }
                            
                            if (projectDao != null) {
                                projectDao.insertProjects(projectEntities)
                            } else {
                                // Fallback to inserting via userDao if projectDao not available
                                projectEntities.forEach { entity ->
                                    try {
                                        userDao.insertProjectIfNotExists(entity.id ?: 0, entity.name)
                                    } catch (e: Exception) {
                                        Log.w("TaskRepository", "Could not insert project ${entity.id}: ${e.message}")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("TaskRepository", "Error saving projects: ${e.message}")
                        }
                    }
                    
                    // Extract and save users next
                    val users = apiTasks.mapNotNull { it.assignedUser }.distinctBy { it.id }
                    if (users.isNotEmpty()) {
                        Log.d("TaskRepository", "Saving ${users.size} users from task response")
                        try {
                            // Insert users
                            users.forEach { user ->
                                try {
                                    userDao.insertUserIfNotExists(user.id ?: 0, user.username)
                                } catch (e: Exception) {
                                    Log.w("TaskRepository", "Could not insert user ${user.id}: ${e.message}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("TaskRepository", "Error saving users: ${e.message}")
                        }
                    }
                    
                    // Merge with local data
                    val localTasksMap = cachedTasks.associateBy { it.id }
                    
                    val mergedTasks = apiTasks.map { apiTask ->
                        val local = localTasksMap[apiTask.id]
                        TaskEntity.fromTask(apiTask).copy(
                            imageUri = local?.imageUri,
                            isSynced = true
                        )
                    }
                    
                    if (mergedTasks.isNotEmpty()) {
                        Log.d("TaskRepository", "Saving ${mergedTasks.size} merged tasks to database")
                        try {
                            taskDao.insertTasks(mergedTasks)
                            emit(mergedTasks.map { it.toTask() })
                            Log.d("TaskRepository", "Successfully saved and emitted tasks")
                        } catch (e: Exception) {
                            Log.e("TaskRepository", "Error inserting tasks: ${e.message}")
                            // If we fail to insert merged tasks, at least return the API tasks
                            emit(apiTasks)
                        }
                    }
                } else {
                    Log.e("TaskRepository", "API call failed: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("TaskRepository", "Error fetching assigned tasks", e)
                Log.e("TaskRepository", "Stack trace: ${e.stackTraceToString()}")
            }
        }
    }

} 