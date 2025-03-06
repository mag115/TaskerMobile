package `is`.hbv501g.taskermobile.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class User(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("created_at") val createdAt: Date? = null,
    @SerializedName("updated_at") val updatedAt: Date? = null,
    @SerializedName("owned_projects") val ownedProjects: List<Project> = emptyList(),
    @SerializedName("projects") val projects: List<Project> = emptyList()
)
