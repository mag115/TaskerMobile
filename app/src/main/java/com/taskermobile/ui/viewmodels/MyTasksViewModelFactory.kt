package com.taskermobile.ui.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.session.SessionManager

class MyTasksViewModelFactory (
    private val taskRepository: TaskRepository,
    private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyTasksViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MyTasksViewModel(taskRepository, sessionManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
}