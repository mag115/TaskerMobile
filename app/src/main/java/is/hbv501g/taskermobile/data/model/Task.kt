package `is`.hbv501g.taskermobile.data.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Task(
    val id: Long? = null, // Optional because it's auto-generated in the backend
    val title: String,
    val description: String,
    val deadline: Date,
    @SerializedName("reminderSent") val isReminderSent: Boolean,
//    val assignedUser: User? = null // Optional, if the API returns this
)
