package com.taskermobile.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskermobile.data.model.Task
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull

class MyTasksViewModel(
    private val taskRepository: TaskRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _myTasks = MutableLiveData<List<Task>>()
    val myTasks: LiveData<List<Task>> get() = _myTasks


    init {
       // fetchMyTasks()
        fetchTasksForUser()
        checkAllTasks()
    }
    fun fetchTasksForUser() {
        viewModelScope.launch {
            // Start loading

            try {
                // Collect tasks from the repository
                taskRepository.getAllTasks().collect { taskList ->
                    // Handle the task list here
                    _myTasks.postValue(taskList)
                }
            } catch (e: Exception) {
                // Handle any errors
                Log.e("MyTasksViewModel", "Error fetching tasks", e)
            }

        }
    }

    // Function to fetch tasks for the current user
    //fun fetchMyTasks() {
      //  viewModelScope.launch {
            // Collect the username from SessionManager
        //    val username = sessionManager.username.firstOrNull() // Collect the username from the session
          //  if (username == null) {
            //    Log.e("MyTasksViewModel", "Username is null or not set")
              //  return@launch // Exit the function if username is null
            //}

            //Log.d("MyTasksViewModel", "Fetching tasks for username: $username")

            //try {
                // Fetch tasks assigned to the current user, with projectId = 1 for now
               // val response = taskRepository.getTasksByUsername(username, 1) // Default project ID for now
                //response.collect { tasks ->
                    // Handle the result
                  //  if (tasks.isNotEmpty()) {
                    //    _myTasks.postValue(tasks) // Post the list of tasks to LiveData
                      //  Log.d("MyTasksViewModel", "Fetched ${tasks.size} tasks for username: $username")
                    //} else {
                      //  Log.d("MyTasksViewModel", "No tasks found for username: $username")
                    //}
               // }
            //} catch (e: Exception) {
                // Log any errors encountered during the fetching process
              //  Log.e("MyTasksViewModel", "Error fetching assigned tasks", e)
           // }
       // }
    //}
    fun checkAllTasks() {
        viewModelScope.launch {
            taskRepository.getAllTasks().collect { tasks ->
                Log.d("MyTasksViewModel", "All tasks in database: $tasks")
            }
        }
    }
}