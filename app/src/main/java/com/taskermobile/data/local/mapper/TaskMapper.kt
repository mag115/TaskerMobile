package com.taskermobile.data.local.mapper

import com.taskermobile.data.local.entity.TaskEntity
import com.taskermobile.data.model.Task

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    deadline = deadline,
    estimatedDuration = estimatedDuration,
    priority = priority,
    status = status,
    projectId = projectId,
    assignedUserId = assignedUserId,
    progressStatus = progressStatus,
    progress = progress,
    manualProgress = manualProgress,
    effortPercentage = effortPercentage,
    estimatedWeeks = estimatedWeeks,
    isDeleted = isDeleted,
    dependency = dependency,
    elapsedTime = elapsedTime,
    timeSpent = timeSpent
)

fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    deadline = deadline,
    estimatedDuration = estimatedDuration,
    priority = priority,
    status = status,
    projectId = projectId,
    assignedUserId = assignedUserId,
    progressStatus = progressStatus,
    progress = progress,
    manualProgress = manualProgress,
    effortPercentage = effortPercentage,
    estimatedWeeks = estimatedWeeks,
    isDeleted = isDeleted,
    dependency = dependency,
    elapsedTime = elapsedTime,
    timeSpent = timeSpent
) 