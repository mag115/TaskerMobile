package com.taskermobile.ui.main.controllers

import com.taskermobile.data.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AllTasksController(private val taskController: TaskController) : TaskActions {

    fun getAllTasks(onResult: (List<Task>?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val tasks = taskController.getAllTasks()
            tasks.collect { taskList ->
                onResult(taskList)
            }
        }
    }

    override fun sendComment(task: Task, comment: String) {
        CoroutineScope(Dispatchers.IO).launch {
            taskController.addCommentToTask(task.id ?: 0L, comment)
        }
    }

    override fun updateTask(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            taskController.updateTask(task)
        }
    }

    override fun startTracking(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            task.isTracking = true
            task.timerId = System.currentTimeMillis()
            taskController.updateTask(task)
        }
    }

    override fun stopTracking(task: Task) { // ✅ ADDED THIS
        CoroutineScope(Dispatchers.IO).launch {
            val startTime = task.timerId ?: return@launch
            val timeElapsed = (System.currentTimeMillis() - startTime) / 1000

            task.timeSpent += timeElapsed
            task.isTracking = false
            task.timerId = null
            taskController.updateTask(task)
        }
    }
}

