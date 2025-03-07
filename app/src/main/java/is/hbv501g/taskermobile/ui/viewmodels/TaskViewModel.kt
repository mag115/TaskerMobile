package `is`.hbv501g.taskermobile.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hbv501g.taskermobile.data.model.Task
import `is`.hbv501g.taskermobile.data.service.TaskServiceImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class TaskViewModel(
    private val taskService: TaskServiceImpl
) : ViewModel() {

    private val _tasks = MutableLiveData<List<Task>>(emptyList())
    val tasks: LiveData<List<Task>> = _tasks

    init {
        viewModelScope.launch {
            // Collect tasks from the Flow (from local data source)
            taskService.getAllTasks().collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    fun createTask(task: Task) {
        viewModelScope.launch {
            taskService.createTaskWithSync(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskService.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskService.deleteTask(task)
        }
    }

    fun syncTasks() {
        viewModelScope.launch {
            taskService.syncUnsyncedTasks()
        }
    }
}