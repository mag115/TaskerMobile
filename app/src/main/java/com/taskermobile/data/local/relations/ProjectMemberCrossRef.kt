package com.taskermobile.data.local.relations

import androidx.room.Entity
import androidx.room.ForeignKey
import com.taskermobile.data.local.entity.UserEntity
import com.taskermobile.data.local.entity.ProjectEntity

@Entity(
    tableName = "project_member_cross_ref",
    primaryKeys = ["userId", "projectId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProjectMemberCrossRef(
    val userId: Long,
    val projectId: Long
) 