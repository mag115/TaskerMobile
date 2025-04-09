package com.taskermobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
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
            Log.d(TAG, "Attempting to parse timestamp: $timestamp")
            
            if (timestamp.isBlank()) {
                Log.e(TAG, "Empty timestamp provided, using current time")
                return System.currentTimeMillis()
            }
            
            return try {
                // First attempt: Parse standard Spring Boot LocalDateTime format (without zone)
                if (timestamp.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?")) ||
                    timestamp.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"))) {
                    try {
                        // Parse as LocalDateTime (no timezone) and assume it's in system default
                        val dateTime = if (timestamp.contains(".")) {
                            LocalDateTime.parse(timestamp)
                        } else {
                            // If no fraction of seconds is present
                            val basicFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                            LocalDateTime.parse(timestamp, basicFormatter)
                        }
                        
                        // Interpret the timestamp as being in the system default zone
                        val millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        Log.d(TAG, "Successfully parsed Spring LocalDateTime format: $timestamp -> $millis")
                        return millis
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse standard Spring LocalDateTime: $timestamp", e)
                        // Continue to other formats
                    }
                }
            
                // Second attempt: try parsing with microseconds format
                val dateTime = LocalDateTime.parse(timestamp, formatter)
                val millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                Log.d(TAG, "Successfully parsed with microseconds format: $timestamp -> $millis")
                millis
            } catch (e: DateTimeParseException) {
                Log.e(TAG, "Failed to parse timestamp with microseconds: $timestamp", e)
                try {
                    // Third attempt: ISO format
                    val isoFormatter = DateTimeFormatter.ISO_DATE_TIME
                    val dateTime = LocalDateTime.parse(timestamp, isoFormatter)
                    val millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    Log.d(TAG, "Successfully parsed with ISO format: $timestamp -> $millis")
                    millis
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to parse timestamp with ISO format: $timestamp", e2)
                    // Fourth attempt: simple format
                    try {
                        val simpleFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        val dateTime = LocalDateTime.parse(timestamp, simpleFormatter)
                        val millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        Log.d(TAG, "Successfully parsed with simple format: $timestamp -> $millis")
                        millis
                    } catch (e3: Exception) {
                        Log.e(TAG, "All parsing attempts failed for timestamp: $timestamp", e3)
                        // Use current time as last resort
                        val currentTime = System.currentTimeMillis()
                        Log.d(TAG, "Using current time instead: $currentTime")
                        currentTime
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error parsing timestamp: $timestamp", e)
                val currentTime = System.currentTimeMillis()
                Log.d(TAG, "Using current time instead: $currentTime")
                currentTime
            }
        }
    }
}
