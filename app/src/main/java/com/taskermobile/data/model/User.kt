package com.taskermobile.data.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class User(
    val id: Long?,
    val username: String,
    val email: String,
    val role: String,

    @SerializedName("created_at")
    val createdAt: Date?,

    @SerializedName("updated_at")
    val updatedAt: Date?,

    @SerializedName("ownedProjects")
    val ownedProjects: List<Project> = emptyList(),

    @SerializedName("projects")
    val projects: List<Project> = emptyList()
)