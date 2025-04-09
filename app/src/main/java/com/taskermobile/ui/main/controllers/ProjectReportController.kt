package com.taskermobile.ui.main.controllers

import com.taskermobile.data.local.dao.ProjectReportDao
import com.taskermobile.data.local.dao.TaskDao
import com.taskermobile.data.local.mapper.toEntity
import com.taskermobile.data.api.RetroFitClient
import com.taskermobile.data.model.ProjectReport
import com.taskermobile.data.model.ReportOptions
import com.taskermobile.data.model.Task
import com.taskermobile.data.repository.ProjectReportRepository
import com.taskermobile.data.service.ProjectReportService
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.app.Application

class ProjectReportController(
    private val sessionManager: SessionManager,
    private val reportDao: ProjectReportDao,
    private val taskDao: TaskDao,
    private val application: Application
) : TaskActions {
    private val reportService: ProjectReportService =
        RetroFitClient.createService(application, sessionManager)
    private val reportRepository = ProjectReportRepository(reportService, reportDao, taskDao)

    private val _uiState = MutableStateFlow<ReportState>(ReportState.Loading)
    val uiState: StateFlow<ReportState> = _uiState

    private val controllerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        controllerScope.launch {
            reportRepository.getAllLocalReports().collect { reports ->
                _uiState.value = ReportState.Success(reports)
            }
        }
    }

    suspend fun fetchAllReports() {
        _uiState.value = ReportState.Loading
        val result = reportRepository.fetchAllReports()
        if (result.isFailure) {
            _uiState.value = ReportState.Error(result.exceptionOrNull()!!)
        }
    }

    suspend fun generateReport(projectId: Long) {
        _uiState.value = ReportState.Loading
        val result = reportRepository.generateProjectReport(projectId)
        if (result.isFailure) {
            _uiState.value = ReportState.Error(result.exceptionOrNull()!!)
        }
    }

    suspend fun generateCustomReport(projectId: Long, options: ReportOptions) {
        _uiState.value = ReportState.Loading
        val result = reportRepository.generateCustomProjectReport(projectId, options)
        if (result.isFailure) {
            _uiState.value = ReportState.Error(result.exceptionOrNull()!!)
        }
    }

    suspend fun loadReportDetails(reportId: Long): ProjectReport? {
        val currentProjectId = sessionManager.currentProjectId.first() ?: 0L
        val result = reportRepository.fetchReportById(reportId, currentProjectId)
        return result.getOrNull()
    }

    suspend fun exportReport(reportId: Long): Result<ByteArray> {
        return reportRepository.exportReportAsPdf(reportId)
    }

    /**  Implementing TaskActions **/

    override fun sendComment(task: Task, comment: String) {
        task.comments.add(comment)
        updateTask(task)
    }

    override fun updateTask(task: Task) {
        val taskEntity = task.toEntity()
        controllerScope.launch(Dispatchers.IO) {
            taskDao.updateTask(taskEntity)
        }
    }

    override fun startTracking(task: Task) {
        controllerScope.launch(Dispatchers.IO) {
            task.isTracking = true
            task.timerId = System.currentTimeMillis()
            updateTask(task)
        }
    }

    override fun stopTracking(task: Task) {
        controllerScope.launch(Dispatchers.IO) {
            val startTime = task.timerId ?: return@launch
            val timeElapsed = (System.currentTimeMillis() - startTime) / 1000

            task.timeSpent += timeElapsed
            task.isTracking = false
            task.timerId = null
            updateTask(task)
        }
    }

    override fun updateTaskProgress(taskId: Long, manualProgress: Double) {
        controllerScope.launch(Dispatchers.IO) {
            try {
                // Get task by ID
                val task = taskDao.getTaskById(taskId)?.toTask()
                
                // If task exists, update its progress
                if (task != null) {
                    // Create a new task with updated progress instead of direct assignment
                    val updatedTask = task.copy(manualProgress = manualProgress)
                    updateTask(updatedTask)
                }
            } catch (e: Exception) {
                // Handle any errors
                e.printStackTrace()
            }
        }
    }
}

sealed class ReportState {
    object Loading : ReportState()
    data class Success(val reports: List<ProjectReport>) : ReportState()
    data class Error(val error: Throwable) : ReportState()
}

