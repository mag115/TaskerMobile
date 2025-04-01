package com.taskermobile.ui.main.controllers

import android.util.Log
import com.taskermobile.data.local.dao.TaskDao
import com.taskermobile.data.local.entity.NotificationEntity
import com.taskermobile.data.model.Task
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyTasksController(
    private val taskRepository: TaskRepository,
    private val sessionManager: SessionManager,
    val taskDao: TaskDao
) : TaskActions {

    override fun sendComment(task: Task, comment: String) {
        CoroutineScope(Dispatchers.IO).launch {
            taskRepository.addCommentToTask(task.id ?: 0L, comment)
            val notification = NotificationEntity(
                message = "New comment on task '${task.title}': $comment",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                id = task.id ?: 0L
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (refresh) taskRepository.refreshTasks()
                taskRepository.getAllTasks().collect { taskList ->
                    withContext(Dispatchers.Main) {
                        onResult(taskList)
                    }
                }
            } catch (e: Exception) {
                Log.e("MyTasksController", "Error fetching tasks", e)
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

}

