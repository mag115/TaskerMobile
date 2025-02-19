package `is`.hbv501g.taskermobile.ui.controllers

import `is`.hbv501g.taskermobile.data.api.RetrofitClient
import `is`.hbv501g.taskermobile.data.model.Task
import `is`.hbv501g.taskermobile.data.service.TaskService
import `is`.hbv501g.taskermobile.data.session.SessionManager

class TaskController(sessionManager: SessionManager) {
    private val taskService = RetrofitClient.createService<TaskService>(sessionManager)

    suspend fun createTask(task: Task,) {
        try {
            val response = taskService.createTask(task)

        } catch (e: Exception) {

        }
    }
}