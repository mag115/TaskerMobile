package com.taskermobile.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.taskermobile.R
import com.taskermobile.data.model.NotificationResponse

class NotificationAdapter(
    private var notifications: List<NotificationResponse>,
    private val onItemClicked: (NotificationResponse) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.notificationMessage)
        val timestampTextView: TextView = itemView.findViewById(R.id.notificationTimestamp)
        val statusTextView: TextView = itemView.findViewById(R.id.notificationStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.messageTextView.text = notification.message
        holder.timestampTextView.text = notification.timestamp
        holder.statusTextView.text = if (!notification.isRead) "Unread" else ""
        holder.itemView.setOnClickListener { onItemClicked(notification) }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateData(newNotifications: List<NotificationResponse>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}