package com.taskermobile.ui.main.controllers

import android.util.Log
import com.taskermobile.data.local.dao.TaskDao
import com.taskermobile.data.local.entity.NotificationEntity
import com.taskermobile.data.model.Task
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MyTasksController(
    private val taskRepository: TaskRepository,
    private val sessionManager: SessionManager,
    val taskDao: TaskDao
) : TaskActions {

    override fun sendComment(task: Task, comment: String) {
        CoroutineScope(Dispatchers.IO).launch {
            taskRepository.addCommentToTask(task.id ?: 0L, comment)
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            val notification = NotificationEntity(
                id = task.id ?: 0L,
                message = "New comment on task '${task.title}': $comment",
                timestamp = timestamp,
                isRead = false
            )
            taskRepository.addNotification(notification)
        }
    }

    override fun updateTask(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            taskRepository.updateTask(task)
        }
    }

    override fun startTracking(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            task.isTracking = true
            task.timerId = System.currentTimeMillis()
            taskRepository.updateTask(task)
        }
    }

    override fun stopTracking(task: Task) {
        CoroutineScope(Dispatchers.IO).launch {
            val startTime = task.timerId ?: return@launch
            val timeElapsed = (System.currentTimeMillis() - startTime) / 1000

            task.timeSpent += timeElapsed
            task.isTracking = false
            task.timerId = null
            taskRepository.updateTask(task)
        }
    }

    fun getMyTasks(onResult: (List<Task>?) -> Unit, refresh: Boolean = true){
        Log.d("MyTasksController", "getMyTasks called with refresh=$refresh")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Sync any pending local tasks to the backend during refresh
                if (refresh) {
                    Log.d("MyTasksController", "Syncing any unsynced tasks before refreshing")
                    taskRepository.syncUnsyncedTasks()
                }
                
                // Get current project ID if available
                val projectId = sessionManager.currentProjectId.firstOrNull()
                Log.d("MyTasksController", "Using project ID: $projectId")
                
                // Use the assigned tasks endpoint
                taskRepository.getAssignedTasks(projectId).collect { taskList ->
                    Log.d("MyTasksController", "Received ${taskList.size} assigned tasks")
                    if (taskList.isEmpty()) {
                        Log.w("MyTasksController", "No tasks found")
                    } else {
                        Log.d("MyTasksController", "First few task titles: ${taskList.take(3).map { it.title }}")
                    }
                    withContext(Dispatchers.Main) {
                        onResult(taskList)
                    }
                }
            } catch (e: Exception) {
                Log.e("MyTasksController", "Error fetching tasks", e)
                Log.e("MyTasksController", "Stack trace: ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

}

