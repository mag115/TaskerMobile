package com.taskermobile.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskermobile.data.model.Task
import com.taskermobile.databinding.ItemTaskBinding
import android.os.Handler
import android.util.Log
import com.taskermobile.ui.viewmodels.TaskUpdater

class TaskAdapter(
    private val onTimerClick: (Task) -> Unit,
    private val taskUpdater: TaskUpdater
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        private var timerRunning = false
        private var elapsedTime = 0L // elapsed time in seconds for the current session
        private var handler = Handler()
        private lateinit var runnable: Runnable
        private var t = 0L

        fun bind(task: Task) {
            binding.apply {
                taskTitle.text = task.title
                taskDescription.text = task.description
                taskPriority.text = "Priority: ${task.priority}"
                taskStatus.text = "Status: ${task.status}"
                taskDeadline.text = task.deadline?.let { "Deadline: $it" } ?: "No deadline"
                taskTimerLabel.text = formatTime(task.timeSpent.toLong())
                taskTimerButton.setOnClickListener {
                    if (timerRunning) {
                        stopTimer(task)
                    } else {
                        startTimer(task)
                    }
                }
            }
        }

        private fun startTimer(task: Task) {
            if (!timerRunning) {
                timerRunning = true
                elapsedTime = task.timeSpent.toLong()
                t = elapsedTime
                Log.d("TaskViewHolder", "Starting timer. Initial elapsedTime: $elapsedTime")
                runnable = object : Runnable {
                    override fun run() {
                        if (timerRunning) {
                            elapsedTime++
                            binding.taskTimerLabel.text = formatTime(elapsedTime)
                            Log.d("TaskViewHolder", "Timer running. elapsedTime: $elapsedTime")
                            handler.postDelayed(this, 1000)
                        }
                    }
                }
                handler.post(runnable)
            }
        }

        private fun stopTimer(task: Task) {
            timerRunning = false
            handler.removeCallbacks(runnable)
            task.timeSpent += elapsedTime - t
            task.elapsedTime = elapsedTime.toDouble()
            binding.taskTimerLabel.text = formatTime(task.timeSpent.toLong())
            // Use the interface method to update the task
            taskUpdater.updateTaskInDatabaseAndBackend(task)
            elapsedTime = 0
        }
    }

    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
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
