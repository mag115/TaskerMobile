package com.taskermobile.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson

class Converters {



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
}