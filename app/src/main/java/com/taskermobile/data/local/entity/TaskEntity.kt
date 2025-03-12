package com.taskermobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.ColumnInfo
import com.taskermobile.data.model.Task
import com.taskermobile.data.model.Project
import com.taskermobile.data.model.User

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["assignedUserId"])
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: Long?,
    val title: String,
    val description: String,
    val deadline: String?,
    val reminderSent: Boolean = false,
    val estimatedDuration: Double?,
    val estimatedWeeks: Int?,
    val effortPercentage: Double?,
    val progressStatus: String?,
    val dependency: Long?,
    val progress: Double?,
    val manualProgress: Double?,
    val isDeleted: Boolean = false,
    var comments: MutableList<String> = mutableListOf(),

    @ColumnInfo(name = "projectId")
    val projectId: Long,

    @ColumnInfo(name = "assignedUserId")
    val assignedUserId: Long?,

    val status: String,
    val priority: String,
    val timeSpent: Double = 0.0,
    val elapsedTime: Double = 0.0,
    val scheduledProgress: Double? = null,
    val isSynced: Boolean = false
) {
    fun toTask(project: Project? = null, assignedUser: User? = null): Task = Task(
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
        isTracking = false, // No tracking info in TaskEntity, leave it false
        timerId = null, // No timerId in TaskEntity
        project = project,
        assignedUser = assignedUser,
        projectId = projectId,
        assignedUserId = assignedUserId,
        status = status,
        priority = priority,
        timeSpent = timeSpent,
        elapsedTime = elapsedTime,
        scheduledProgress = scheduledProgress
    )

    companion object {
        fun fromTask(task: Task): TaskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            deadline = task.deadline,
            reminderSent = task.reminderSent,
            estimatedDuration = task.estimatedDuration,
            estimatedWeeks = task.estimatedWeeks,
            effortPercentage = task.effortPercentage,
            progressStatus = task.progressStatus,
            dependency = task.dependency,
            progress = task.progress,
            manualProgress = task.manualProgress,
            isDeleted = task.isDeleted ?: false,
            projectId = task.projectId,
            assignedUserId = task.assignedUserId,
            status = task.status,
            priority = task.priority,
            timeSpent = task.timeSpent,
            elapsedTime = task.elapsedTime,
            scheduledProgress = task.scheduledProgress
        )
    }
}