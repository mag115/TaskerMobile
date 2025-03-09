package com.taskermobile.data.model

data class NotificationResponse(
    val id: Long,
    val message: String,
    val isRead: Boolean,
    val timestamp: String
)