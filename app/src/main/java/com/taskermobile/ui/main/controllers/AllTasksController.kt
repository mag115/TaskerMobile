package com.taskermobile.ui.main.controllers

import com.taskermobile.data.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class AllTasksController(private val taskController: TaskController) : TaskActions {

    fun getAllTasks(projectId: Long? = null, onResult: (List<Task>?) -> Unit) {
        Log.d("AllTasksController", "getAllTasks called with projectId: $projectId")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First try to fetch from API
                val apiTasks = taskController.fetchTasksFromApi(projectId)
                Log.d("AllTasksController", "API tasks count: ${apiTasks?.size ?: 0}")
                
                if (apiTasks != null) {
                    // If API call successful, update local database and return tasks
                    apiTasks.forEach { task ->
                        taskController.updateTask(task)
                    }
                    onResult(apiTasks)
                } else {
                    // If API call fails, fall back to local data
                    Log.d("AllTasksController", "API call failed, falling back to local data")
                    val tasks = if (projectId != null) {
                        taskController.getTasksByProject(projectId)
                    } else {
                        taskController.getAllTasks()
                    }
                    
                    tasks.collect { taskList ->
                        Log.d("AllTasksController", "Collected ${taskList.size} tasks from local database")
                        onResult(taskList)
                    }
                }
            } catch (e: Exception) {
                Log.e("AllTasksController", "Error in getAllTasks", e)
                Log.e("AllTasksController", "Stack trace: ${e.stackTraceToString()}")
                onResult(null)
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

    override fun stopTracking(task: Task) { // ADDED THIS
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

