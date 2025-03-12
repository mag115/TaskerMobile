package com.taskermobile.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.model.Task
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AllTasksViewModel(
    private val taskRepository: TaskRepository,
    private val sessionManager: SessionManager
) : ViewModel(), TaskUpdater {

    // LiveData that will hold the tasks for the current project
    private val _tasks = MutableLiveData<List<Task>?>()
    val tasks: LiveData<List<Task>?> get() = _tasks

    // Fetch tasks filtered by projectId
    fun fetchTasksForProject(projectId: Long) {
        viewModelScope.launch {
            try {
                taskRepository.getTasksByProject(projectId).collect { taskList ->
                    _tasks.postValue(taskList)
                }
            } catch (e: Exception) {
                Log.e("AllTasksViewModel", "Error fetching tasks for project", e)
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
                val response = RetrofitClient.taskApiService.updateTime(timeRequest)
                if (response.isSuccessful) {
                    val updatedTask = response.body()
                    if (updatedTask != null) {
                        _tasks.value = _tasks.value?.map { task ->
                            if (task.id == updatedTask.id) updatedTask else task
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AllTasksViewModel", "Error updating task time", e)
            }
        }
    }

    // Implement the interface method.
    override fun updateTaskInDatabaseAndBackend(task: Task) {
        task.id?.let { updateTaskTime(it, task.timeSpent) }
    }
}
