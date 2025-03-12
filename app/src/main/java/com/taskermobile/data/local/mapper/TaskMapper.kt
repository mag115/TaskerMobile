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
    scheduledProgress = scheduledProgress
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
    isTracking = false,  // Default value, as `TaskEntity` does not store tracking info
    timerId = null,  // Default value, `TaskEntity` does not store timerId
    project = null,  // Can be fetched separately if needed
    assignedUser = null,  // Can be fetched separately if needed
    projectId = projectId,
    assignedUserId = assignedUserId,
    status = status,
    priority = priority,
    timeSpent = timeSpent,
    elapsedTime = elapsedTime,
    scheduledProgress = scheduledProgress
)