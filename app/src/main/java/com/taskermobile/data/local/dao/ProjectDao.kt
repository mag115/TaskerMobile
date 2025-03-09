package com.taskermobile.data.local.dao

import androidx.room.*
import com.taskermobile.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Query("DELETE FROM projects")
    suspend fun deleteAllProjects()

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("SELECT * FROM projects WHERE isSynced = 0")
    suspend fun getUnsyncedProjects(): List<ProjectEntity>

    @Query("UPDATE projects SET isSynced = 1 WHERE id = :projectId")
    suspend fun markProjectAsSynced(projectId: Long?)
} 