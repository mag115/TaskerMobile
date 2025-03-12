package com.taskermobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taskermobile.data.repository.NotificationRepository
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.service.UserService

class CreateTaskViewModelFactory(
    private val taskRepository: TaskRepository,
    private val userService: UserService,
    private val notificationRepository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateTaskViewModel(taskRepository, userService, notificationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
