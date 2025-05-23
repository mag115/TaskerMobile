package com.taskermobile.ui.main.controllers
import com.taskermobile.data.model.Task
import com.taskermobile.data.model.User
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateTaskController(
    private val taskRepository: TaskRepository,
    private val userService: UserService
) {
    // Callback to return users
    fun fetchUsers(onResult: (List<User>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = userService.getAllUsers()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) { onResult(response.body() ?: emptyList()) }
                } else {
                    withContext(Dispatchers.Main) { onResult(emptyList()) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResult(emptyList()) }
            }
        }
    }

    // Create task with sync
    suspend fun createTask(task: Task): Result<Task> {
        return taskRepository.createTaskWithSync(task)
    }
}
