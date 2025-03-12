package com.taskermobile.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.taskermobile.data.local.entity.ProjectEntity
import com.taskermobile.data.local.entity.TaskEntity

data class ProjectWithTasks(
    @Embedded val project: ProjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val tasks: List<TaskEntity>
)
