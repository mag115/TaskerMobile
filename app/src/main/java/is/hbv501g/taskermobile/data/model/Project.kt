package `is`.hbv501g.taskermobile.data.model

import com.google.gson.annotations.SerializedName
import `is`.hbv501g.taskermobile.data.model.Task

data class Project(
    val id: Long?,
    val name: String,
    val description: String?,

    @SerializedName("createdAt")
    val createdAt: String?, // Use String if LocalDateTime parsing causes issues

    @SerializedName("updatedAt")
    val updatedAt: String?,

    val tasks: List<Task> = emptyList(),
//    val members: List<User> = emptyList(),
//    val owner: User
)
