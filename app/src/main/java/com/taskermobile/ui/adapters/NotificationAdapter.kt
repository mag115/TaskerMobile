package com.taskermobile.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskermobile.data.local.entity.NotificationEntity
import com.taskermobile.databinding.ItemNotificationBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.regex.Pattern

class NotificationAdapter(private val onClick: (NotificationEntity) -> Unit) :
    ListAdapter<NotificationEntity, NotificationAdapter.ViewHolder>(NotificationDiffCallback()) {

    companion object {
        private const val TAG = "NotificationAdapter"
        // Pattern to match ISO-8601 date format (2024-11-29T18:57)
        private val ISO_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?")
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNotificationBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationEntity) {
            // Format any ISO dates in the message
            val formattedMessage = formatDatesInMessage(notification.message)
            binding.notificationMessage.text = formattedMessage
            
            // Hide timestamp view entirely
            binding.notificationTimestamp.visibility = View.GONE

            // Show/hide red dot for unread messages
            binding.notificationUnreadIndicator.visibility =
                if (notification.isRead) View.GONE else View.VISIBLE

            // Click listener: Mark as read
            binding.root.setOnClickListener { onClick(notification) }
        }
        
        private fun formatDatesInMessage(message: String): String {
            val matcher = ISO_DATE_PATTERN.matcher(message)
            
            if (!matcher.find()) {
                return message
            }
            
            // Make a copy of the original message
            var formattedMessage = message
            
            // Reset the matcher
            matcher.reset()
            
            // Find all ISO dates in the message and format them
            while (matcher.find()) {
                val isoDateString = matcher.group()
                try {
                    val dateTime = LocalDateTime.parse(isoDateString)
                    val formattedDate = dateTime.format(DATE_FORMATTER)
                    
                    // Replace the ISO date with the formatted date
                    formattedMessage = formattedMessage.replace(isoDateString, formattedDate)
                    Log.d(TAG, "Formatted date in message: $isoDateString -> $formattedDate")
                } catch (e: DateTimeParseException) {
                    Log.e(TAG, "Failed to parse date: $isoDateString", e)
                }
            }
            
            return formattedMessage
        }
    }
}

/**
 *  DiffUtil to optimize RecyclerView updates
 */
class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
    override fun areItemsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
        return oldItem == newItem
    }
}
