package com.taskermobile.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.taskermobile.data.model.Task
import com.taskermobile.ui.main.controllers.TaskController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val taskController: TaskController) : ViewModel() {

    val allTasks: StateFlow<List<Task>> =
        taskController.getAllTasks().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun tasksForUser(userId: Long): StateFlow<List<Task>> =
        taskController.getTasksByUser(userId).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateTaskTime(taskId: Long, newTimeSpent: Double) {
        viewModelScope.launch {
            taskController.updateTaskTime(taskId, newTimeSpent)
        }
    }
}

class TaskViewModelFactory(private val taskController: TaskController) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(taskController) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}