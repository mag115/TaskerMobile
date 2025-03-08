package com.taskermobile.ui.adapters

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.taskermobile.data.model.Task
import com.taskermobile.databinding.ItemTaskBinding

class TaskAdapter(
    private val tasks: List<Task>,
    // Callback: When timer stops, send the updated time (in seconds)
    private val onTimeUpdated: (task: Task, newTimeSpent: Double) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        var isTracking = false
        var elapsedSeconds = 0L
        private var handler: Handler? = null

        // Runnable that updates the displayed time every second
        private val updateRunnable = object : Runnable {
            override fun run() {
                elapsedSeconds++
                val totalSeconds = (task.timeSpent ?: 0) + elapsedSeconds
                binding.taskTimeSpent.text = "Time Spent: ${formatTime(totalSeconds)}"
                handler?.postDelayed(this, 1000)
            }
        }

        lateinit var task: Task

        fun bind(task: Task) {
            this.task = task
            binding.taskTitle.text = task.title
            // Assume timeSpent is stored in seconds; if null, default to 0
            binding.taskTimeSpent.text = "Time Spent: ${formatTime((task.timeSpent ?: 0).toInt())}"
            binding.timerButton.text = if (isTracking) "Stop Tracking" else "Start Tracking"

            binding.timerButton.setOnClickListener {
                if (isTracking) {
                    // Stop tracking:
                    handler?.removeCallbacks(updateRunnable)
                    isTracking = false
                    binding.timerButton.text = "Start Tracking"
                    // Update the task's timeSpent by adding the elapsed time
                    val newTimeSpent = (task.timeSpent ?: 0) + elapsedSeconds
                    // Reset timer
                    elapsedSeconds = 0L
                    onTimeUpdated(task, newTimeSpent.toDouble())
                } else {
                    // Start tracking:
                    isTracking = true
                    handler = Handler(Looper.getMainLooper())
                    handler?.post(updateRunnable)
                    binding.timerButton.text = "Stop Tracking"
                }
            }
        }

        private fun formatTime(totalSeconds: Int): String {
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d:%02d", hours, minutes, seconds)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size
}