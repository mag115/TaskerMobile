package com.taskermobile.data.local.mapper

import com.taskermobile.data.local.entity.TaskEntity
import com.taskermobile.data.model.Task

// Convert Task (domain model) to TaskEntity (database entity)
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
    isDeleted = isDeleted ?: false,  // If null, use false
    projectId = projectId,
    assignedUserId = assignedUserId,  // Can be null
    status = status,
    priority = priority,
    timeSpent = timeSpent,
    elapsedTime = elapsedTime,
    scheduledProgress = scheduledProgress,
    imageUri = imageUri
)


// Convert TaskEntity (database entity) to Task (domain model)
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
    isTracking = false,
    timerId = null,
    project = null,
    assignedUser = null,
    projectId = projectId ?: 0L, // Provide default value for non-nullable field
    assignedUserId = assignedUserId,
    status = status,
    priority = priority,
    timeSpent = timeSpent,
    elapsedTime = elapsedTime,
    scheduledProgress = scheduledProgress,
    imageUri = imageUri
)