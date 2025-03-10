package com.taskermobile.ui.main.controllers

import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.model.ProjectReport
import com.taskermobile.data.model.ReportOptions
import com.taskermobile.data.repository.ProjectReportRepository
import com.taskermobile.data.service.ProjectReportService
import com.taskermobile.data.session.SessionManager
import com.taskermobile.data.local.dao.ProjectReportDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProjectReportController(
    sessionManager: SessionManager,
    private val reportDao: ProjectReportDao
) {
    private val reportService: ProjectReportService =
        RetrofitClient.createService(sessionManager)
    private val reportRepository = ProjectReportRepository(reportService, reportDao)

    private val _uiState = MutableStateFlow<ReportState>(ReportState.Loading)
    val uiState: StateFlow<ReportState> = _uiState

    private val controllerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        // Observe local DB flow
        controllerScope.launch {
            reportRepository.getAllLocalReports().collect { reports ->
                _uiState.value = ReportState.Success(reports)
            }
        }
    }

    // Fetch from server & refresh local
    suspend fun fetchAllReports() {
        _uiState.value = ReportState.Loading
        val result = reportRepository.fetchAllReports()
        if (result.isFailure) {
            _uiState.value = ReportState.Error(result.exceptionOrNull()!!)
        }
    }

    // Generate a normal report
    suspend fun generateReport(projectId: Long) {
        _uiState.value = ReportState.Loading
        val result = reportRepository.generateProjectReport(projectId)
        if (result.isFailure) {
            _uiState.value = ReportState.Error(result.exceptionOrNull()!!)
        }
    }

    // Generate a custom report
    suspend fun generateCustomReport(projectId: Long, options: ReportOptions) {
        _uiState.value = ReportState.Loading
        val result = reportRepository.generateCustomProjectReport(projectId, options)
        if (result.isFailure) {
            _uiState.value = ReportState.Error(result.exceptionOrNull()!!)
        }
    }

    // Get a single report from server
    suspend fun loadReportDetails(reportId: Long): ProjectReport? {
        val result = reportRepository.fetchReportById(reportId)
        return result.getOrNull()
    }

    // Export PDF
    suspend fun exportReport(reportId: Long): Result<ByteArray> {
        return reportRepository.exportReportAsPdf(reportId)
    }
}

sealed class ReportState {
    object Loading : ReportState()
    data class Success(val reports: List<ProjectReport>) : ReportState()
    data class Error(val error: Throwable) : ReportState()
}
