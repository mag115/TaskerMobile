package com.taskermobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import android.util.Log

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: Long,
    val message: String,
    val isRead: Boolean = false,
    @SerializedName("timestamp")
    val timestamp: String,
    val timestampMillis: Long = parseTimestamp(timestamp)
) {
    companion object {
        private const val TAG = "NotificationEntity"
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")

        private fun parseTimestamp(timestamp: String): Long {
            return try {
                // First try parsing with microseconds format
                val dateTime = LocalDateTime.parse(timestamp, formatter)
                dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (e: DateTimeParseException) {
                Log.e(TAG, "Failed to parse timestamp with microseconds: $timestamp", e)
                try {
                    // Fallback to ISO format if microseconds format fails
                    val isoFormatter = DateTimeFormatter.ISO_DATE_TIME
                    val dateTime = LocalDateTime.parse(timestamp, isoFormatter)
                    dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to parse timestamp with ISO format: $timestamp", e2)
                    System.currentTimeMillis()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error parsing timestamp: $timestamp", e)
                System.currentTimeMillis()
            }
        }
    }
}
