package com.taskermobile.ui.main.controllers

import com.taskermobile.data.model.Task

interface TaskActions {
    fun sendComment(task: Task, comment: String)
    fun updateTask(task: Task)
    fun startTracking(task: Task)
    fun stopTracking(task: Task)
}
