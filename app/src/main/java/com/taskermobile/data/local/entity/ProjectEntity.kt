package com.taskermobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: Long?,
    val name: String,
    val description: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val isSynced: Boolean = false
) 