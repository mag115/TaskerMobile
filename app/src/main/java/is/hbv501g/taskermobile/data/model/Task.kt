package `is`.hbv501g.taskermobile.data.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Task(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("estimated_duration") val estimatedDuration: Double?,
    @SerializedName("priority") val priority: String,
    @SerializedName("status") val status: String,
    @SerializedName("project_id") val projectId: Long,
    @SerializedName("assigned_user_id") val assignedUserId: Long? = null,
    @SerializedName("progress_status") val progressStatus: String? = null,
    @SerializedName("progress") val progress: Double? = null,
    @SerializedName("manual_progress") val manualProgress: Double? = null,
    @SerializedName("effort_percentage") val effortPercentage: Float? = null,
    @SerializedName("estimated_weeks") val estimatedWeeks: Double? = null,
    @SerializedName("is_deleted") val isDeleted: Boolean? = false,
    @SerializedName("dependency") val dependency: String? = null,
    @SerializedName("elapsed_time") val elapsedTime: Int = 0,
    @SerializedName("time_spent") val timeSpent: Int = 0
)

