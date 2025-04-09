package com.taskermobile.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Long?,
    val username: String,
    val email: String? = null,
    val role: String,
    val isSynced: Boolean = false,
    var imageUri: String? = null
)