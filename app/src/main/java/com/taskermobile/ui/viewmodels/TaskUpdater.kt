package com.taskermobile.ui.viewmodels

import com.taskermobile.data.model.Task

interface TaskUpdater {
    fun updateTaskInDatabaseAndBackend(task: Task)
}
