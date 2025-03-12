package com.taskermobile.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.local.dao.TaskDao
import com.taskermobile.data.local.entity.NotificationEntity
import com.taskermobile.data.model.Task
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.launch
import com.taskermobile.data.local.entity.TaskEntity

class MyTasksViewModel(
    private val taskRepository: TaskRepository,
    private val sessionManager: SessionManager,
    private val taskDao: TaskDao,

) : ViewModel() {

    private val _myTasks = MutableLiveData<List<Task>?>()
    val myTasks: LiveData<List<Task>?> get() = _myTasks

    init {
        fetchTasksForUser()
        checkAllTasks()
    }

    fun saveTaskToLocalDatabase(task: Task) {
        viewModelScope.launch {
            val taskEntity = TaskEntity.fromTask(task) // Convert Task to TaskEntity
            taskRepository.insertTask(task)
        }
    }

    fun sendComment(task: Task, comment: String) {
        viewModelScope.launch {
            taskRepository.addCommentToTask(task.id ?: 0L, comment)

            // Notify all members
            val notification = NotificationEntity(
                message = "New comment on task '${task.title}': $comment",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                id=task.id ?: 0L
            )
            taskRepository.addNotification(notification)
        }
    }

    fun updateTaskInDatabaseAndBackend(task: Task) {
        viewModelScope.launch {
            val taskEntity = TaskEntity.fromTask(task)
            taskDao.updateTask(taskEntity)  // Update task in local database

            // Optionally, send the updated task time to the backend
            task.id?.let { updateTaskTime(it, task.timeSpent) }
        }
    }


    fun fetchTasksForUser() {
        viewModelScope.launch {
            try {
                // Fetch tasks from the repository
                taskRepository.getAllTasks().collect { taskList ->
                    // Post the task list to LiveData
                    _myTasks.postValue(taskList)
                }
            } catch (e: Exception) {
                Log.e("MyTasksViewModel", "Error fetching tasks", e)
            }
        }
    }

    fun updateTaskTime(taskId: Long, timeSpent: Double) {
        val timeRequest = hashMapOf<String, Any>(
            "taskId" to taskId,
            "timeSpent" to timeSpent
        )

        viewModelScope.launch {
            try {
                //api call to update time spent on the task
                Log.d("MyTasksViewModel", "Updating task time for task ID: $taskId with timeSpent: $timeSpent")
                val response = RetrofitClient.taskApiService.updateTime(timeRequest)
                if (response.isSuccessful) {
                    val updatedTask = response.body()
                    if (updatedTask != null) {
                        Log.d("MyTasksViewModel", "Successfully updated task with ID: ${updatedTask.id}.")
                        val updatedTasks = _myTasks.value?.map { task ->
                            if (task.id == updatedTask.id) {
                                updatedTask // Update the task
                            } else {
                                task // Leave other tasks unchanged
                            }
                        }
                        _myTasks.value = updatedTasks // Update the list
                    }
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error updating task time", e)
            }
        }
    }

    // Function to fetch all tasks
    fun checkAllTasks() {
        viewModelScope.launch {
            taskRepository.getAllTasks().collect { tasks ->
                Log.d("MyTasksViewModel", "All tasks in database: $tasks")
            }
        }
    }
}