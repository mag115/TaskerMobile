package com.taskermobile.data.repository

import com.taskermobile.data.local.dao.ProjectReportDao
import com.taskermobile.data.local.mapper.toDomain
import com.taskermobile.data.local.mapper.toEntity
import com.taskermobile.data.model.ProjectReport
import com.taskermobile.data.model.ReportOptions
import com.taskermobile.data.service.ProjectReportService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

class ProjectReportRepository(
    private val reportService: ProjectReportService,
    private val reportDao: ProjectReportDao
) {
    /**
     * Retrieve all reports from local DB as a Flow.
     */
    fun getAllLocalReports(): Flow<List<ProjectReport>> {
        return reportDao.getAllReports().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Fetch all reports from the server, cache them locally.
     */
    suspend fun fetchAllReports(): Result<List<ProjectReport>> {
        return try {
            val response = reportService.getAllReports()
            if (response.isSuccessful) {
                val reports = response.body().orEmpty()
                // Cache locally
                val entities = reports.map { it.toEntity() }
                reportDao.deleteAll()
                reportDao.insertReports(entities)
                Result.success(reports)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a regular project report on the server and cache locally.
     */
    suspend fun generateProjectReport(projectId: Long): Result<ProjectReport> {
        return try {
            val response = reportService.generateProjectReport(projectId)
            if (response.isSuccessful) {
                response.body()?.let { report ->
                    // Insert in local DB
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

    /**
     * Generate a custom project report on the server and cache locally.
     */
    suspend fun generateCustomProjectReport(
        projectId: Long,
        options: ReportOptions
    ): Result<ProjectReport> {
        return try {
            val response = reportService.generateCustomProjectReport(projectId, options)
            if (response.isSuccessful) {
                response.body()?.let { report ->
                    // Insert in local DB
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

    /**
     * Get a specific report from local DB first; if not found or forced refresh, fetch from server.
     */
    fun getLocalReportById(reportId: Long): Flow<ProjectReport?> {
        return reportDao.getReportById(reportId).map { it?.toDomain() }
    }

    suspend fun fetchReportById(reportId: Long): Result<ProjectReport> {
        return try {
            val response = reportService.getReportById(reportId)
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

    /**
     * Export PDF. Typically you might save it to the device or open a viewer.
     */
    suspend fun exportReportAsPdf(reportId: Long): Result<ByteArray> {
        return try {
            val response = reportService.exportProjectReportPdf(reportId)
            if (response.isSuccessful) {
                val pdfBytes = response.body()?.bytes()
                if (pdfBytes != null) {
                    // Optionally: Save PDF to device
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
