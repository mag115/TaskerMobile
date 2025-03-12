package com.taskermobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: Long,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: String
)
