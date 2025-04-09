package com.taskermobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.taskermobile.data.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long?,
    val username: String,
    val email: String? = null,
    val role: String,
    val isSynced: Boolean = false,
    val imageUri: String? = null
) {
    fun toUser(): User = User(
        id = id,
        username = username,
        email = email,
        role = role,
        isSynced = isSynced,
        imageUri = imageUri
    )

    companion object {
        fun fromUser(user: User): UserEntity = UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            role = user.role,
            isSynced = user.isSynced,
            imageUri = user.imageUri
        )
    }
} 