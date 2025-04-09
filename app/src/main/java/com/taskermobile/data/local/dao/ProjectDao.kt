package com.taskermobile.data.local.dao

import androidx.room.*
import com.taskermobile.data.local.entity.ProjectEntity
import com.taskermobile.data.local.entity.UserEntity
import com.taskermobile.data.local.relations.ProjectMemberCrossRef
import com.taskermobile.data.local.relations.ProjectWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Transaction
    @Query("SELECT * FROM projects")
    fun getProjectsWithTasks(): Flow<List<ProjectWithTasks>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Query("DELETE FROM projects")
    suspend fun deleteAllProjects()

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("SELECT * FROM projects WHERE isSynced = 0")
    fun getUnsyncedProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE isSynced = 0")
    suspend fun getUnsyncedProjectsSync(): List<ProjectEntity>

    @Query("UPDATE projects SET isSynced = 1 WHERE id = :projectId")
    suspend fun markProjectAsSynced(projectId: Long?)

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: Long): ProjectEntity?

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectMember(crossRef: ProjectMemberCrossRef)

    @Delete
    suspend fun deleteProjectMember(crossRef: ProjectMemberCrossRef)

    @Query("DELETE FROM project_member_cross_ref WHERE projectId = :projectId")
    suspend fun deleteAllProjectMembers(projectId: Long)

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectWithMembers(projectId: Long): ProjectWithMembers?
}

data class ProjectWithMembers(
    @Embedded val project: ProjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ProjectMemberCrossRef::class,
            parentColumn = "projectId",
            entityColumn = "userId"
        )
    )
    val members: List<UserEntity>
) 