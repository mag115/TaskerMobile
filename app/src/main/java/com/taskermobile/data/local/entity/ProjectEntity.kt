package com.taskermobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.taskermobile.data.model.Project

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: Long?,
    val name: String,
    val description: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val isSynced: Boolean = false
) {
    /** ✅ Convert ProjectEntity to Project */
    fun toProject(): Project = Project(
        id = id ?: 0L, // Default to 0 if null
        name = name,
        description = description ?: "",
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: ""
    )

    companion object {
        /** ✅ Convert Project to ProjectEntity */
        fun fromProject(project: Project): ProjectEntity = ProjectEntity(
            id = project.id,
            name = project.name,
            description = project.description,
            createdAt = project.createdAt,
            updatedAt = project.updatedAt
        )
    }
}
