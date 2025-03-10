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
    // For tasks, you can store them as a serialized String (JSON) or in a separate table
    // using relationships. For simplicity, we can use a converter to store them in one column.
    val tasksJson: String? // Using converter to store tasks
)
