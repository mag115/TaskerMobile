package com.taskermobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import com.taskermobile.data.model.Project
import com.taskermobile.data.model.User

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ProjectEntity(
    @PrimaryKey val id: Long?,
    val name: String,
    val description: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val isSynced: Boolean = false,
    val ownerId: Long? = null
) {
    /** Convert ProjectEntity to Project */
    fun toProject(owner: User? = null): Project = Project(
        id = id ?: 0L, // Default to 0 if null
        name = name,
        description = description ?: "",
        createdAt = createdAt ?: "",
        updatedAt = updatedAt ?: "",
        isSynced = isSynced,
        owner = owner
    )

    companion object {
        /** Convert Project to ProjectEntity */
        fun fromProject(project: Project): ProjectEntity = ProjectEntity(
            id = project.id,
            name = project.name,
            description = project.description,
            createdAt = project.createdAt,
            updatedAt = project.updatedAt,
            isSynced = project.isSynced,
            ownerId = project.owner?.id
        )
    }
}
