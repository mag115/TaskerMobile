package com.taskermobile.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskermobile.data.model.Task
import com.taskermobile.databinding.ItemAllTaskBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

class AllTasksAdapter : ListAdapter<Task, AllTasksAdapter.TaskViewHolder>(TaskDiffCallback()) {

    companion object {
        private const val TAG = "AllTasksAdapter"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemAllTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(private val binding: ItemAllTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.taskTitle.text = task.title
            binding.taskDescription.text = task.description
            binding.taskStatus.text = task.status
            binding.taskPriority.text = task.priority
            binding.taskProgress.text = "${task.progress ?: 0}%"
            
            // Format and set deadline if the view exists in the layout
            if (binding.taskDeadline != null) {
                binding.taskDeadline.text = formatDeadline(task.deadline)
                
                // Set visibility based on whether there's a deadline
                binding.taskDeadline.visibility = if (task.deadline.isNullOrBlank()) View.GONE else View.VISIBLE
            }
        }
        
        private fun formatDeadline(deadlineStr: String?): String {
            if (deadlineStr.isNullOrBlank()) {
                return "No deadline"
            }
            
            try {
                // Parse the deadline string to LocalDateTime
                val deadline = LocalDateTime.parse(deadlineStr)
                val now = LocalDateTime.now()
                
                // Calculate days until deadline
                val daysUntil = ChronoUnit.DAYS.between(now, deadline)
                
                return when {
                    daysUntil < 0 -> {
                        val daysAgo = Math.abs(daysUntil)
                        "Due: ${deadline.format(DATE_FORMATTER)} (${daysAgo}d overdue)"
                    }
                    daysUntil == 0L -> {
                        val hoursUntil = ChronoUnit.HOURS.between(now, deadline)
                        if (hoursUntil < 0) {
                            "Due: Today (overdue)"
                        } else if (hoursUntil == 0L) {
                            val minutesUntil = ChronoUnit.MINUTES.between(now, deadline)
                            if (minutesUntil <= 0) {
                                "Due: Now!"
                            } else {
                                "Due: Today (${minutesUntil}m)"
                            }
                        } else {
                            "Due: Today (${hoursUntil}h)"
                        }
                    }
                    daysUntil == 1L -> "Due: Tomorrow"
                    daysUntil < 7 -> "Due: ${daysUntil} days"
                    else -> "Due: ${deadline.format(DATE_FORMATTER)}"
                }
            } catch (e: DateTimeParseException) {
                Log.e(TAG, "Error parsing deadline: $deadlineStr", e)
                return deadlineStr
            } catch (e: Exception) {
                Log.e(TAG, "Error formatting deadline: $deadlineStr", e)
                return deadlineStr
            }
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
} 