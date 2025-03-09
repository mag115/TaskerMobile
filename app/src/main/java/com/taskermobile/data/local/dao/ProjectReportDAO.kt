package com.taskermobile.data.local.dao

import androidx.room.*
import com.taskermobile.data.local.entity.ProjectReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectReportDao {
    @Query("SELECT * FROM project_reports")
    fun getAllReports(): Flow<List<ProjectReportEntity>>

    @Query("SELECT * FROM project_reports WHERE id = :reportId LIMIT 1")
    fun getReportById(reportId: Long): Flow<ProjectReportEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ProjectReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ProjectReportEntity>)

    @Query("DELETE FROM project_reports WHERE id = :reportId")
    suspend fun deleteReport(reportId: Long)

    @Query("DELETE FROM project_reports")
    suspend fun deleteAll()
}
