package com.taskermobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.taskermobile.data.model.User
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Long?,
    val username: String,
    val email: String,
    val role: String,
    val createdAt: String?,
    val updatedAt: String?,
    val isSynced: Boolean = false
) {
    fun toUser(): User = User(
        id = id,
        username = username,
        email = email,
        role = role,
        createdAt = null, // We'll need to parse the string to Date if needed
        updatedAt = null,
        ownedProjects = emptyList(), // These will be populated from API responses
        projects = emptyList()
    )

    companion object {
        fun fromUser(user: User): UserEntity = UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            role = user.role,
            createdAt = user.createdAt?.toString(),
            updatedAt = user.updatedAt?.toString(),
            isSynced = false
        )
    }
} 