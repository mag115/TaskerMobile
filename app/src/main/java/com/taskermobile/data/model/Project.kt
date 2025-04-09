package com.taskermobile.data.model

import com.google.gson.annotations.SerializedName
import com.taskermobile.data.model.Task

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

    @SerializedName("owner")
    val owner: User? = null,

    val isSynced: Boolean = false
)