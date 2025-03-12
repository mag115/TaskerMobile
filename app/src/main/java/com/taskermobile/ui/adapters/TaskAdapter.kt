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

<<<<<<< HEAD

class TaskAdapter(
    private val onTimerClick: (Task) -> Unit,
    private val onCommentSend: (Task, String) -> Unit, // Added a callback for comments
    private val viewModel: MyTasksViewModel
=======
class TaskAdapter(
    private val onTimerClick: (Task) -> Unit,
    private val taskUpdater: TaskUpdater
>>>>>>> e2db3df90dc2ef5f9ae4bd1ed933e8090134723b
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var timerRunning = false
<<<<<<< HEAD
        private var elapsedTime = 0L
=======
        private var elapsedTime = 0L // elapsed time in seconds for the current session
>>>>>>> e2db3df90dc2ef5f9ae4bd1ed933e8090134723b
        private var handler = Handler()
        private lateinit var runnable: Runnable
        private var t = 0L

        fun bind(task: Task) {
            binding.apply {
                taskTitle.text = task.title
<<<<<<< HEAD
                taskDescription.text = task.description ?: "No description" // ✅ Add fallback
                taskPriority.text = "Priority: ${task.priority ?: "N/A"}"
                taskStatus.text = "Status: ${task.status ?: "N/A"}"
                taskDeadline.text = "Deadline: ${task.deadline ?: "No deadline"}"
                taskTimerLabel.text = formatTime(task.timeSpent.toLong())

=======
                taskDescription.text = task.description
                taskPriority.text = "Priority: ${task.priority}"
                taskStatus.text = "Status: ${task.status}"
                taskDeadline.text = task.deadline?.let { "Deadline: $it" } ?: "No deadline"
                taskTimerLabel.text = formatTime(task.timeSpent.toLong())
>>>>>>> e2db3df90dc2ef5f9ae4bd1ed933e8090134723b
                taskTimerButton.setOnClickListener {
                    if (timerRunning) {
                        stopTimer(task)
                    } else {
                        startTimer(task)
                    }
                }

                // **Handle comment submission**
                sendCommentButton.setOnClickListener {
                    val commentText = commentEditText.text.toString()
                    if (commentText.isNotBlank()) {
                        onCommentSend(task, commentText) // Call ViewModel function
                        commentEditText.text.clear() // Clear text after sending
                    }
                }
            }
        }

        private fun startTimer(task: Task) {
            if (!timerRunning) {
                timerRunning = true
                elapsedTime = task.timeSpent.toLong()
                t = elapsedTime
<<<<<<< HEAD

=======
                Log.d("TaskViewHolder", "Starting timer. Initial elapsedTime: $elapsedTime")
>>>>>>> e2db3df90dc2ef5f9ae4bd1ed933e8090134723b
                runnable = object : Runnable {
                    override fun run() {
                        if (timerRunning) {
                            elapsedTime++
                            binding.taskTimerLabel.text = formatTime(elapsedTime)
<<<<<<< HEAD
=======
                            Log.d("TaskViewHolder", "Timer running. elapsedTime: $elapsedTime")
>>>>>>> e2db3df90dc2ef5f9ae4bd1ed933e8090134723b
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
<<<<<<< HEAD

            task.timeSpent += elapsedTime - t
            task.elapsedTime = elapsedTime.toDouble()

            binding.taskTimerLabel.text = formatTime(task.timeSpent.toLong())

            viewModel.updateTaskInDatabaseAndBackend(task)
=======
            task.timeSpent += elapsedTime - t
            task.elapsedTime = elapsedTime.toDouble()
            binding.taskTimerLabel.text = formatTime(task.timeSpent.toLong())
            // Use the interface method to update the task
            taskUpdater.updateTaskInDatabaseAndBackend(task)
>>>>>>> e2db3df90dc2ef5f9ae4bd1ed933e8090134723b
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
<<<<<<< HEAD

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}
=======
    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}
>>>>>>> e2db3df90dc2ef5f9ae4bd1ed933e8090134723b
