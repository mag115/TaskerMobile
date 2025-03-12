package com.taskermobile.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskermobile.data.model.Task
import com.taskermobile.data.model.User
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.service.UserService
import kotlinx.coroutines.launch
import retrofit2.Response

class CreateTaskViewModel(
    private val taskRepository: TaskRepository,
    private val userService: UserService
) : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val _taskCreationStatus = MutableLiveData<Boolean>()
    val taskCreationStatus: LiveData<Boolean> get() = _taskCreationStatus

    // Fetch all users for dropdown
    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val response: Response<List<User>> = userService.getAllUsers()
                if (response.isSuccessful) {
                    _users.postValue(response.body() ?: emptyList())
                } else {
                    _users.postValue(emptyList())
                }
            } catch (e: Exception) {
                _users.postValue(emptyList())
            }
        }
    }

    // Create task and assign a user
    // In your CreateTaskViewModel
    fun createTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.insertTask(task)
                // Or use createTaskWithSync if you're using remote syncing
            } catch (e: Exception) {
                // Handle errors
                throw e
            }
        }
    }
}