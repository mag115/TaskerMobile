package com.taskermobile.data.repository

import com.taskermobile.data.local.dao.ProjectReportDao
import com.taskermobile.data.local.dao.TaskDao
import com.taskermobile.data.local.mapper.toDomain
import com.taskermobile.data.local.mapper.toEntity
import com.taskermobile.data.model.ProjectReport
import com.taskermobile.data.model.ReportOptions
import com.taskermobile.data.service.ProjectReportService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

class ProjectReportRepository(
    private val reportService: ProjectReportService,
    private val reportDao: ProjectReportDao,
    private val taskDao: TaskDao  // NEW dependency
) {
    fun getAllLocalReports(): Flow<List<ProjectReport>> {
        return reportDao.getAllReports().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun fetchAllReports(): Result<List<ProjectReport>> {
        return try {
            val response = reportService.getAllReports()
            if (response.isSuccessful) {
                response.body()?.let { reports ->
                    val entities = reports.map { it.toEntity() }
                    reportDao.deleteAll()
                    reportDao.insertReports(entities)
                    Result.success(reports)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateProjectReport(projectId: Long): Result<ProjectReport> {
        return try {
            val response = reportService.generateProjectReport(projectId)
            if (response.isSuccessful) {
                response.body()?.let { report ->
                    // If the report returned from the backend has an empty tasks list,
                    // fetch tasks for the given project from the local TaskDao.
                    val updatedReport = if (report.tasks.isEmpty()) {
                        // Query tasks for the project from local DB.
                        val tasks = taskDao.getTasksByProject(projectId).first()
                        report.copy(tasks = tasks.map { it.toTask() })
                    } else {
                        report
                    }
                    // Cache the updated report
                    reportDao.insertReport(updatedReport.toEntity())
                    Result.success(updatedReport)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateCustomProjectReport(
        projectId: Long,
        options: ReportOptions
    ): Result<ProjectReport> {
        return try {
            val response = reportService.generateCustomProjectReport(projectId, options)
            if (response.isSuccessful) {
                response.body()?.let { report ->
                    reportDao.insertReport(report.toEntity())
                    Result.success(report)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLocalReportById(reportId: Long): Flow<ProjectReport?> {
        return reportDao.getReportById(reportId).map { it?.toDomain() }
    }

    suspend fun fetchReportById(reportId: Long, projectId: Long): Result<ProjectReport> {
        return try {
            val response = reportService.getReportById(reportId)
            if (response.isSuccessful) {
                response.body()?.let { report ->
                    // If tasks list is empty, load tasks for the project from local DB.
                    val finalReport = if (report.tasks.isEmpty()) {
                        val localTasks = taskDao.getTasksByProject(projectId).first()  // get tasks for this project
                        report.copy(tasks = localTasks.map { it.toTask() })
                    } else {
                        report
                    }
                    // Cache the updated report locally.
                    reportDao.insertReport(finalReport.toEntity())
                    Result.success(finalReport)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun exportReportAsPdf(reportId: Long): Result<ByteArray> {
        return try {
            val response = reportService.exportProjectReportPdf(reportId)
            if (response.isSuccessful) {
                val pdfBytes = response.body()?.bytes()
                if (pdfBytes != null) {
                    Result.success(pdfBytes)
                } else {
                    Result.failure(Exception("PDF is empty"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
