package com.taskermobile.data.local.dao

import androidx.room.*
import com.taskermobile.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

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
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<TaskEntity>

    @Query("UPDATE tasks SET isSynced = 1 WHERE id = :taskId")
    suspend fun markTaskAsSynced(taskId: Long?)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countTasks(): Int

} 