package `is`.hbv501g.taskermobile.data.local.dao

import androidx.room.*
import `is`.hbv501g.taskermobile.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Fetch all tasks from local database
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    // Get a specific task by its ID
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    // Insert a new task into the local database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    // Insert multiple tasks at once
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    // Update an existing task
    @Update
    suspend fun updateTask(task: TaskEntity)

    // Delete a task by its ID
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    // Delete all tasks (for full data reset)
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    // Get tasks that **have not been synced** with the server
    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    fun getUnsyncedTasks(): Flow<List<TaskEntity>>

    // Mark multiple tasks as **synced** after they are uploaded
    @Query("UPDATE tasks SET isSynced = 1 WHERE id IN (:taskIds)")
    suspend fun markTasksAsSynced(taskIds: List<Long>)

    // Get tasks assigned to a **specific project**
    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    fun getTasksByProject(projectId: Long): Flow<List<TaskEntity>>

    // Get tasks assigned to a **specific user**
    @Query("SELECT * FROM tasks WHERE assignedUserId = :userId")
    fun getTasksByUser(userId: Long): Flow<List<TaskEntity>>
}
