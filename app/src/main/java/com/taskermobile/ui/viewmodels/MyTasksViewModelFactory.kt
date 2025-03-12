package com.taskermobile.ui.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taskermobile.data.local.dao.TaskDao
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.session.SessionManager

class MyTasksViewModelFactory (

    private val taskRepository: TaskRepository,
    private val sessionManager: SessionManager,
    private val taskDao: TaskDao,
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyTasksViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MyTasksViewModel(taskRepository, sessionManager, taskDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
}