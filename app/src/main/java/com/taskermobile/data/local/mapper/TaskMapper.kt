package com.taskermobile.data.local.mapper

import com.taskermobile.data.local.entity.TaskEntity
import com.taskermobile.data.model.Task

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    deadline = deadline,
    reminderSent = reminderSent,
    estimatedDuration = estimatedDuration,
    estimatedWeeks = estimatedWeeks,
    effortPercentage = effortPercentage,
    progressStatus = progressStatus,
    dependency = dependency,
    progress = progress,
    manualProgress = manualProgress,
    isDeleted = isDeleted ?: false,
    projectId = projectId,
    assignedUserId = assignedUserId,
    status = status,
    priority = priority,
    timeSpent = timeSpent,
    elapsedTime = elapsedTime,
    scheduledProgress = scheduledProgress
)

fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    deadline = deadline,
    reminderSent = reminderSent,
    estimatedDuration = estimatedDuration,
    estimatedWeeks = estimatedWeeks,
    effortPercentage = effortPercentage,
    progressStatus = progressStatus,
    dependency = dependency,
    progress = progress,
    manualProgress = manualProgress,
    isDeleted = isDeleted,
    project = null, 
    assignedUser = null, 
    projectId = projectId, 
    status = status,
    priority = priority,
    timeSpent = timeSpent,
    elapsedTime = elapsedTime,
    scheduledProgress = scheduledProgress
) 