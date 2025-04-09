package com.taskermobile.data.local.dao

import androidx.room.*
import com.taskermobile.data.local.entity.UserEntity
import com.taskermobile.data.local.entity.ProjectEntity
import com.taskermobile.data.local.relations.ProjectMemberCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT id FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserIdByUsername(username: String): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM users WHERE isSynced = 0")
    suspend fun getUnsyncedUsers(): List<UserEntity>

    @Query("UPDATE users SET isSynced = 1 WHERE id = :userId")
    suspend fun markUserAsSynced(userId: Long?)
    
    @Query("INSERT OR IGNORE INTO users (id, username, isSynced) VALUES (:userId, :username, 1)")
    suspend fun insertUserIfNotExists(userId: Long, username: String)
    
    @Query("INSERT OR IGNORE INTO projects (id, name, description, isSynced) VALUES (:projectId, :projectName, '', 1)")
    suspend fun insertProjectIfNotExists(projectId: Long, projectName: String)

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithProjects(userId: Long): UserWithProjects?
    
    @Query("UPDATE users SET imageUri = :imageUri WHERE id = :userId")
    suspend fun updateUserProfileImage(userId: Long, imageUri: String)
}

data class UserWithProjects(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "ownerId"
    )
    val ownedProjects: List<ProjectEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ProjectMemberCrossRef::class,
            parentColumn = "userId",
            entityColumn = "projectId"
        )
    )
    val projects: List<ProjectEntity>
) 