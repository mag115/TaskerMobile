package com.taskermobile.ui.adapters

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taskermobile.R
import com.taskermobile.data.model.Task
import com.taskermobile.databinding.ItemTaskBinding
import com.taskermobile.ui.main.controllers.TaskActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAdapter(
    private val onTimerClick: (Task) -> Unit = {},
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
        private var elapsedTime = 0L
        private var t = 0L
        private var handler = Handler(Looper.getMainLooper())
        private lateinit var runnable: Runnable
        private var isSpinnerListenerActive = false

        fun bind(task: Task) {
            binding.apply {
                taskTitle.text = task.title
                taskDescription.text = task.description
                taskPriority.text = "Priority: ${task.priority}"
                taskStatus.text = "Status: ${task.status}"
                taskDeadline.text = "Deadline: ${task.deadline ?: "No deadline"}"

                // Display stored time spent
                taskTimerLabel.text = formatTime(task.timeSpent.toLong())

                updateTimerButton(task)

                root.setOnClickListener { onTaskClick(task) }

                sendCommentButton.setOnClickListener {
                    val commentText = commentEditText.text.toString()
                    if (commentText.isNotBlank()) {
                        onCommentSend(task, commentText)
                        commentEditText.text.clear()
                    }
                }

                taskTimerButton.setOnClickListener {
                    if (timerRunning) {
                        stopTimer(task)
                    } else {
                        startTimer(task)
                    }
                }

                // Spinner setup for status change
                val statusOptions = root.context.resources.getStringArray(R.array.task_status_array)
                val adapter = ArrayAdapter(root.context, android.R.layout.simple_spinner_item, statusOptions)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                statusSpinner.adapter = adapter

                val statusIndex = statusOptions.indexOfFirst { it.equals(task.status, ignoreCase = true) }
                if (statusIndex >= 0) {
                    isSpinnerListenerActive = false
                    statusSpinner.setSelection(statusIndex, false)
                }

                statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        if (!isSpinnerListenerActive) {
                            isSpinnerListenerActive = true
                            return
                        }
                        val selectedStatus = parent.getItemAtPosition(position).toString()
                        if (task.status != selectedStatus) {
                            task.status = selectedStatus
                            CoroutineScope(Dispatchers.IO).launch {
                                taskActions.updateTask(task)
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
        }

        private fun updateTimerButton(task: Task) {
            binding.taskTimerButton.text = if (task.isTracking) "Stop Timer" else "Start Timer"
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
            task.timeSpent += (elapsedTime - t)
            task.elapsedTime = elapsedTime.toDouble()
            binding.taskTimerLabel.text = formatTime(task.timeSpent.toLong())

            CoroutineScope(Dispatchers.IO).launch {
                taskActions.stopTracking(task)
                taskActions.updateTask(task)
            }
        }

        private fun formatTime(seconds: Long): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
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
