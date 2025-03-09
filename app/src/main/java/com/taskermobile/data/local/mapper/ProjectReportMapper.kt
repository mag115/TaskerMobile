package com.taskermobile.data.local.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.taskermobile.data.local.entity.ProjectReportEntity
import com.taskermobile.data.model.ProjectReport
import com.taskermobile.data.model.Task

fun ProjectReport.toEntity(): ProjectReportEntity {
    val gson = Gson()
    return ProjectReportEntity(
        id = this.id,
        reportDate = this.reportDate,
        overallPerformance = this.overallPerformance,
        tasksJson = gson.toJson(this.tasks)
    )
}

fun ProjectReportEntity.toDomain(): ProjectReport {
    val gson = Gson()
    val tasksType = object : TypeToken<List<Task>>() {}.type
    val tasksList = if (!this.tasksJson.isNullOrEmpty()) {
        gson.fromJson<List<Task>>(this.tasksJson, tasksType)
    } else {
        emptyList()
    }

    return ProjectReport(
        id = this.id,
        reportDate = this.reportDate,
        overallPerformance = this.overallPerformance,
        tasks = tasksList
    )
}
