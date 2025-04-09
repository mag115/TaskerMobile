package com.taskermobile.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskermobile.data.local.entity.NotificationEntity
import com.taskermobile.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NotificationAdapter(private val onClick: (NotificationEntity) -> Unit) :
    ListAdapter<NotificationEntity, NotificationAdapter.ViewHolder>(NotificationDiffCallback()) {

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
            binding.notificationMessage.text = notification.message

            // Format timestamp using a more reliable approach
            val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getDefault() // Use device's timezone
            val formattedDate = dateFormat.format(Date(notification.timestampMillis))
            binding.notificationTimestamp.text = formattedDate

            // Show/hide red dot for unread messages
            binding.notificationUnreadIndicator.visibility =
                if (notification.isRead) View.GONE else View.VISIBLE

            // Click listener: Mark as read
            binding.root.setOnClickListener { onClick(notification) }
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
