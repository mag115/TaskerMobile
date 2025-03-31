package com.taskermobile.data.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Task(
    val id: Long? = null,
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
    val isDeleted: Boolean? = false,
    var isTracking: Boolean,
    var timerId: Long? = null,
    
    @SerializedName("project")
    val project: Project?,
    
    @SerializedName("assignedUser")
    val assignedUser: User?,
    
    val projectId: Long,
    val assignedUserId: Long? = assignedUser?.id,
    
    var status: String,
    val priority: String,
    var timeSpent: Double = 0.0,
    var elapsedTime: Double = 0.0,
    val scheduledProgress: Double? = null,

    var comments: MutableList<String> = mutableListOf(),
    var imageUri: String? = null
) {
    fun calculateActualProgress(): Double {
        return if (estimatedDuration != null && estimatedDuration > 0) {
            minOf((timeSpent / (estimatedDuration * 3600)) * 100, 100.0) // Cap at 100%
        } else {
            0.0
        }
    }

    fun calculateScheduledProgress(): Double {
        if (deadline != null) {
            try {
                val deadlineDate = java.time.LocalDateTime.parse(deadline)
                val now = java.time.LocalDateTime.now()
                val totalDuration = java.time.temporal.ChronoUnit.SECONDS.between(now, deadlineDate)
                val timeSinceStart = if (estimatedDuration != null) {
                    (estimatedDuration * 3600).toLong()
                } else {
                    totalDuration
                }

                val elapsedSeconds = java.time.temporal.ChronoUnit.SECONDS.between(
                    now.minusSeconds(timeSinceStart),
                    now
                )

                return minOf((elapsedSeconds.toDouble() / totalDuration.toDouble()) * 100, 100.0)
            } catch (e: Exception) {
                return 0.0
            }
        }
        return 0.0
    }

    fun calculateScheduledProgressStatus(): String {
        if (deadline != null) {
            val scheduledProgress = calculateScheduledProgress()
            val actualProgress = progress ?: 0.0

            return when {
                actualProgress >= 100 -> "Completed"
                actualProgress >= scheduledProgress -> "On Track"
                else -> "Behind Schedule"
            }
        }
        return "On Track"
    }

    fun updateProgressStatus(): String {
        if (deadline != null) {
            val scheduledProgress = calculateScheduledProgress()
            val actualProgress = progress ?: 0.0

            return if (actualProgress >= scheduledProgress) {
                "On Track"
            } else {
                "Behind Schedule"
            }
        }
        return "On Track"
    }
}