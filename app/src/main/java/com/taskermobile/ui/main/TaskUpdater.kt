package com.taskermobile.ui.main

import com.taskermobile.data.model.Task

interface TaskUpdater {
    fun updateTaskInDatabaseAndBackend(task: Task)
}
