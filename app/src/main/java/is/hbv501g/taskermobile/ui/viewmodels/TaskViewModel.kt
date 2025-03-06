package `is`.hbv501g.taskermobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hbv501g.taskermobile.data.local.dao.TaskDao
import `is`.hbv501g.taskermobile.data.local.entity.TaskEntity
import `is`.hbv501g.taskermobile.data.model.Task
import `is`.hbv501g.taskermobile.data.service.TaskService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class TaskViewModel(
    private val taskDao: TaskDao,
    private val taskService: TaskService
) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        getAllTasks()
    }

    // Load all tasks from the local database
    fun getAllTasks() {
        viewModelScope.launch {
            taskDao.getAllTasks().collect { entities ->
                _tasks.value = entities.map { it.toTask() }
            }
        }
    }

    // Create a new task, store it locally, and sync with the server
    fun createTask(task: Task) {
        viewModelScope.launch {
            val taskEntity = TaskEntity.fromTask(task)
            val taskId = taskDao.insertTask(taskEntity)

            try {
                val response = taskService.createTask(task)
                if (response.isSuccessful) {
                    taskDao.markTasksAsSynced(listOf(taskId)) //  Use batch update
                }
            } catch (e: HttpException) {
                e.printStackTrace()
            }
        }
    }

    // Delete a task from local storage
    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            taskDao.deleteTask(taskId)
        }
    }

    // Sync unsynced tasks with the backend
    fun syncTasks() {
        viewModelScope.launch {
            taskDao.getUnsyncedTasks().collect { unsyncedTasks ->
                unsyncedTasks.forEach { taskEntity ->
                    try {
                        val response = taskService.createTask(taskEntity.toTask())
                        if (response.isSuccessful) {
                            taskDao.markTasksAsSynced(listOf(taskEntity.id))
                        }
                    } catch (e: HttpException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
