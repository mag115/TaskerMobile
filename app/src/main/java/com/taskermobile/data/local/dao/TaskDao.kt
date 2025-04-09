package com.taskermobile.data.local.dao

import androidx.room.*
import com.taskermobile.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import android.util.Log

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE (projectId = :projectId OR :projectId = 0) AND (isDeleted IS NULL OR isDeleted = 0)")
    fun getTasksByProject(projectId: Long): Flow<List<TaskEntity>>

    @Query("DELETE FROM tasks WHERE projectId = :projectId")
    suspend fun deleteTasksByProject(projectId: Long)

    @Query("SELECT * FROM tasks WHERE (projectId = :projectId)")
    fun getAssignedTasks(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE (assignedUserId = :userId)")
    fun getAllTasksForUser(userId: Long): Flow<List<TaskEntity>>

    // @Query("SELECT * FROM tasks WHERE assignedUserId = :userId AND projectId = :projectId")

    //fun getTasksByUser(userId: Long, projectId: Long): Flow<List<TaskEntity>>
    @Query("SELECT * FROM tasks WHERE assignedUserId = :userId")
    fun getTasksByUser(userId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity) {
        Log.d("TaskDao", "Inserting task: ${task.title} (ID: ${task.id})")
        try {
            _insertTask(task)
            Log.d("TaskDao", "Successfully inserted task: ${task.title}")
        } catch (e: Exception) {
            Log.e("TaskDao", "Failed to insert task: ${task.title}", e)
            throw e
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>) {
        Log.d("TaskDao", "Inserting ${tasks.size} tasks")
        try {
            _insertTasks(tasks)
            Log.d("TaskDao", "Successfully inserted ${tasks.size} tasks")
        } catch (e: Exception) {
            Log.e("TaskDao", "Failed to insert tasks", e)
            throw e
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity) {
        Log.d("TaskDao", "Updating task: ${task.title} (ID: ${task.id})")
        try {
            _updateTask(task)
            Log.d("TaskDao", "Successfully updated task: ${task.title}")
        } catch (e: Exception) {
            Log.e("TaskDao", "Failed to update task: ${task.title}", e)
            throw e
        }
    }

    @Update
    suspend fun _updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity) {
        Log.d("TaskDao", "Deleting task: ${task.title} (ID: ${task.id})")
        try {
            _deleteTask(task)
            Log.d("TaskDao", "Successfully deleted task: ${task.title}")
        } catch (e: Exception) {
            Log.e("TaskDao", "Failed to delete task: ${task.title}", e)
            throw e
        }
    }

    @Delete
    suspend fun _deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks() {
        Log.d("TaskDao", "Deleting all tasks")
        try {
            _deleteAllTasks()
            Log.d("TaskDao", "Successfully deleted all tasks")
        } catch (e: Exception) {
            Log.e("TaskDao", "Failed to delete all tasks", e)
            throw e
        }
    }

    @Query("DELETE FROM tasks")
    suspend fun _deleteAllTasks()

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<TaskEntity>

    @Query("UPDATE tasks SET isSynced = 1 WHERE id = :taskId")
    suspend fun markTaskAsSynced(taskId: Long?)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countTasks(): Int {
        val count = _countTasks()
        Log.d("TaskDao", "Current task count: $count")
        return count
    }

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun _countTasks(): Int

    @Query("UPDATE tasks SET imageUri = :imageUri WHERE id = :taskId")
    suspend fun updateImageUri(taskId: Long?, imageUri: String)

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksSync(): List<TaskEntity> {
        val tasks = _getAllTasksSync()
        Log.d("TaskDao", "Retrieved ${tasks.size} tasks synchronously")
        return tasks
    }

    @Query("SELECT * FROM tasks")
    suspend fun _getAllTasksSync(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskByIdSync(taskId: Long): TaskEntity? {
        val task = _getTaskByIdSync(taskId)
        Log.d("TaskDao", "Retrieved task by ID $taskId: ${task?.title ?: "null"}")
        return task
    }

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun _getTaskByIdSync(taskId: Long): TaskEntity?
} 