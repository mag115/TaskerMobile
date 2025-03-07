package `is`.hbv501g.taskermobile.data.model

import com.google.gson.annotations.SerializedName

data class Project(
    val id: Long? = null,
    val name: String,
    val description: String?,
    
    @SerializedName("createdAt")
    val createdAt: String?, 

    @SerializedName("updatedAt")
    val updatedAt: String?,

    @SerializedName("tasks")
    val tasks: List<Task> = emptyList(),

    @SerializedName("members")
    val members: List<User> = emptyList(),

    @SerializedName("owner")
    val owner: User? = null
)
