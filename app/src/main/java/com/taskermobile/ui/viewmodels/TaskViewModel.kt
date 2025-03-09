package com.taskermobile.ui.viewmodels

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskermobile.data.model.Task
import com.taskermobile.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _task = MutableLiveData<Task>()
    val task: LiveData<Task> = _task

    private var timer: CountDownTimer? = null
    private var timeSpent: Double = 0.0

    fun startTimer(taskId: Long) {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeSpent += 1 / 3600.0 // increment by one second in hours
                viewModelScope.launch {
                    repository.updateTimeSpent(taskId, timeSpent)
                }
            }
            override fun onFinish() {}
        }.start()
    }

    fun stopTimer(taskId: Long) {
        timer?.cancel()
        viewModelScope.launch {
            repository.updateTimeSpent(taskId, timeSpent)
            _task.value = repository.getTaskById(taskId)
        }
    }
}
