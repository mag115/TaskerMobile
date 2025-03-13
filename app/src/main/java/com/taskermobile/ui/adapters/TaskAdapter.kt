package com.taskermobile.ui.adapters

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskermobile.data.model.Task
import com.taskermobile.databinding.ItemTaskBinding
import com.taskermobile.ui.main.controllers.TaskActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAdapter(
    private val taskActions: TaskActions,
    private val onTaskClick: (Task) -> Unit,
    private val onCommentSend: (Task, String) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        Log.d("TaskAdapter", "Binding task: ${task.title}")
        holder.bind(task)
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var timerRunning = false
        private var handler = Handler(Looper.getMainLooper())
        private var runnable: Runnable? = null

        fun bind(task: Task) {
            binding.apply {
                taskTitle.text = task.title
                taskDescription.text = task.description
                taskPriority.text = "Priority: ${task.priority}"
                taskStatus.text = "Status: ${task.status}"
                taskDeadline.text = "Deadline: ${task.deadline ?: "No deadline"}"

                // Display stored time spent
                taskTimerLabel.text = formatTime(task.timeSpent.toLong())

                root.setOnClickListener { onTaskClick(task) }

                sendCommentButton.setOnClickListener {
                    val commentText = commentEditText.text.toString()
                    if (commentText.isNotBlank()) {
                        onCommentSend(task, commentText)
                        commentEditText.text.clear()
                    }
                }

                // Set button label based on tracking state
                updateTimerButton(task)

                taskTimerButton.setOnClickListener {
                    if (task.isTracking) {
                        stopTimer(task)
                    } else {
                        startTimer(task)
                    }
                }
            }
        }

        private fun updateTimerButton(task: Task) {
            binding.taskTimerButton.text = if (task.isTracking) "Stop Timer" else "Start Timer"
        }

        private fun startTimer(task: Task) {
            if (!timerRunning) {
                timerRunning = true
                task.isTracking = true
                task.timerId = System.currentTimeMillis() // Store start time

                runnable = object : Runnable {
                    override fun run() {
                        val elapsedSeconds = ((System.currentTimeMillis() - (task.timerId ?: 0)) / 1000)
                        binding.taskTimerLabel.text = formatTime(elapsedSeconds)
                        handler.postDelayed(this, 1000)
                    }
                }
                handler.post(runnable!!)

                updateTimerButton(task)

                CoroutineScope(Dispatchers.IO).launch {
                    taskActions.startTracking(task)
                }
            }
        }

        private fun stopTimer(task: Task) {
            if (timerRunning) {
                timerRunning = false
                task.isTracking = false
                handler.removeCallbacks(runnable!!)

                val startTime = task.timerId ?: return
                val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000

                task.timeSpent += elapsedSeconds
                task.timerId = null // Reset timer ID

                binding.taskTimerLabel.text = formatTime(task.timeSpent.toLong())
                updateTimerButton(task)

                CoroutineScope(Dispatchers.IO).launch {
                    taskActions.stopTracking(task)
                    taskActions.updateTask(task)
                }
            }
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
