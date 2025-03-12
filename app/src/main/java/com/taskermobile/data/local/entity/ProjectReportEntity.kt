package com.taskermobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.taskermobile.data.local.converter.TaskListConverter

@Entity(tableName = "project_reports")
data class ProjectReportEntity(
    @PrimaryKey
    val id: Long?,
    val reportDate: String?,
    val overallPerformance: String?,
    val tasksJson: String? // Using converter to store tasks
)
