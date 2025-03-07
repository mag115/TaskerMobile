package com.taskermobile.data.local.mapper

import com.taskermobile.data.local.entity.ProjectEntity
import com.taskermobile.data.model.Project

fun Project.toEntity() = ProjectEntity(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ProjectEntity.toDomain() = Project(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
) 