package com.taskermobile.data.service

import com.taskermobile.data.model.ProjectReport
import com.taskermobile.data.model.ReportOptions
import retrofit2.Response
import retrofit2.http.*

interface ProjectReportService {

    @GET("/reports")
    suspend fun getAllReports(): Response<List<ProjectReport>>

    @POST("/reports/generate")
    suspend fun generateProjectReport(
        @Query("projectId") projectId: Long
    ): Response<ProjectReport>

    @POST("/reports/generate/custom")
    suspend fun generateCustomProjectReport(
        @Query("projectId") projectId: Long,
        @Body reportOptions: ReportOptions
    ): Response<ProjectReport>

    @GET("/reports/{reportId}")
    suspend fun getReportById(
        @Path("reportId") reportId: Long
    ): Response<ProjectReport>

    /**
     * PDF Export Endpoint
     * This returns raw bytes; you can handle it as a ResponseBody, or if you
     * want to store the PDF locally, read from the body.
     */
    @GET("/reports/{reportId}/export")
    suspend fun exportProjectReportPdf(
        @Path("reportId") reportId: Long
    ): Response<okhttp3.ResponseBody>
}
