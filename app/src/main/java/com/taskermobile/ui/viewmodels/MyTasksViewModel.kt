package com.taskermobile.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskermobile.data.model.Task
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyTasksViewModel(
    private val taskRepository: TaskRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _myTasks = MutableLiveData<List<Task>>()
    val myTasks: LiveData<List<Task>> get() = _myTasks

    init {
        fetchMyTasks()
    }

    fun fetchMyTasks() {
        viewModelScope.launch {
            val userId = sessionManager.userId.first() ?: return@launch
            taskRepository.getTasksByUser(userId).collectLatest { tasks ->
                _myTasks.postValue(tasks)
            }
        }
    }
}
