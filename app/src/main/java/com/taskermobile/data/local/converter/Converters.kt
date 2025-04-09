package com.taskermobile.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class Converters {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    @TypeConverter
    fun fromString(value: String?): MutableList<String> {
        return value?.let {
            val type = object : TypeToken<MutableList<String>>() {}.type
            Gson().fromJson(it, type)
        } ?: mutableListOf()
    }

    @TypeConverter
    fun listToString(list: MutableList<String>?): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromTimestamp(value: String?): Date? {
        return value?.let { dateFormat.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): String? {
        return date?.let { dateFormat.format(it) }
    }
}