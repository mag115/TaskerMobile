package com.taskermobile.data.model

import com.google.gson.annotations.SerializedName

data class ProjectReport(
    val id: Long? = null,

    @SerializedName("tasks")
    val tasks: List<Task> = emptyList(),

    @SerializedName("reportDate")
    val reportDate: String? = null,

    @SerializedName("overallPerformance")
    val overallPerformance: String? = null
)
