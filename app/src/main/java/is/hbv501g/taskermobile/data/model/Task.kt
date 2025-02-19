package `is`.hbv501g.taskermobile.data.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Task(
    val id: Long? = null,
    val title: String,
    val description: String,
    val deadline: String?,
    val estimatedDuration: Double?,
    val priority: String,
    val status: String,
    val projectId: Long,
    val assignedUserId: Long? = null,
    val progressStatus: String? = null,
    val progress: Double? = null,
    val manualProgress: Double? = null,
    val effortPercentage: Float? = null,
    val estimatedWeeks: Double? = null,
    val isDeleted: Boolean? = false,
    val dependency: String? = null,
    val elapsedTime: Int = 0,
    val timeSpent: Int = 0,
)
