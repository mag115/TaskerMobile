package com.taskermobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.taskermobile.data.model.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: Long?,
    val title: String,
    val description: String,
    val deadline: String?,
    val estimatedDuration: Double?,
    val priority: String,
    val status: String,
    val projectId: Long,
    val assignedUserId: Long?,
    val progressStatus: String?,
    val progress: Double?,
    val manualProgress: Double?,
    val effortPercentage: Float?,
    val estimatedWeeks: Double?,
    val isDeleted: Boolean?,
    val dependency: String?,
    val elapsedTime: Int,
    val timeSpent: Int,
    val isSynced: Boolean = false // To track if the task has been synced with the remote server
) {
    fun toTask(): Task = Task(
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

    companion object {
        fun fromTask(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            deadline = task.deadline,
            estimatedDuration = task.estimatedDuration,
            priority = task.priority,
            status = task.status,
            projectId = task.projectId,
            assignedUserId = task.assignedUserId,
            progressStatus = task.progressStatus,
            progress = task.progress,
            manualProgress = task.manualProgress,
            effortPercentage = task.effortPercentage,
            estimatedWeeks = task.estimatedWeeks,
            isDeleted = task.isDeleted,
            dependency = task.dependency,
            elapsedTime = task.elapsedTime,
            timeSpent = task.timeSpent
        )
    }
} 