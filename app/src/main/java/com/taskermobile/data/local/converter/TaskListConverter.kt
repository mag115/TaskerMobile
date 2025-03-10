package com.taskermobile.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.taskermobile.data.model.Task

class TaskListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromTaskList(tasks: List<Task>?): String? {
        if (tasks == null) return null
        return gson.toJson(tasks)
    }

    @TypeConverter
    fun toTaskList(data: String?): List<Task> {
        if (data.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<Task>>() {}.type
        return gson.fromJson(data, listType)
    }
}
