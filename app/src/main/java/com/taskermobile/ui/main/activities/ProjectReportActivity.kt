package com.taskermobile.ui.main.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.TaskerApplication
import com.taskermobile.data.model.ProjectReport
import com.taskermobile.data.model.ReportOptions
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.ActivityProjectReportBinding
import com.taskermobile.ui.adapters.ProjectReportAdapter
import com.taskermobile.ui.main.controllers.ProjectReportController
import com.taskermobile.ui.main.controllers.ReportState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProjectReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectReportBinding
    private lateinit var reportController: ProjectReportController
    private lateinit var reportAdapter: ProjectReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Dependencies
        setupDependencies()

        // 2. Set up RecyclerView
        setupRecyclerView()

        // 3. Observe the Flow
        observeReports()

        // 4. Load data
        lifecycleScope.launch {
            reportController.fetchAllReports()
        }

        // 5. FAB button for generating a report
        binding.fabGenerateReport.setOnClickListener {
            // Hardcoded projectId=1 for demo. In real usage, pass the correct ID
            onClickGenerateReport(projectId = 1L)
        }
    }

    private fun setupDependencies() {
        val sessionManager = SessionManager(this)
        val database = (application as TaskerApplication).database
        val reportDao = database.projectReportDao()
        reportController = ProjectReportController(sessionManager, reportDao)
    }

    private fun setupRecyclerView() {
        reportAdapter = ProjectReportAdapter(onItemClicked = { report ->
            // Could open a detail screen or show PDF export dialog, etc.
            onClickExportReport(report.id ?: return@ProjectReportAdapter)
        })
        binding.reportsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProjectReportActivity)
            adapter = reportAdapter
        }
    }

    private fun observeReports() {
        lifecycleScope.launchWhenStarted {
            reportController.uiState.collectLatest { state ->
                when (state) {
                    is ReportState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is ReportState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        reportAdapter.submitList(state.reports)
                    }
                    is ReportState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@ProjectReportActivity,
                            "Error: ${state.error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    // Called when the user taps the FAB
    private fun onClickGenerateReport(projectId: Long) {
        // Possibly show a dialog to pick "regular" vs "custom"
        // For demonstration, let's do a normal generate:
        lifecycleScope.launch {
            reportController.generateReport(projectId)
            Toast.makeText(
                this@ProjectReportActivity,
                "Generated report for project $projectId",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onClickCustomReport(projectId: Long) {
        val options = ReportOptions(
            includeTasks = true,
            includePerformance = true
        )
        lifecycleScope.launch {
            reportController.generateCustomReport(projectId, options)
            Toast.makeText(
                this@ProjectReportActivity,
                "Generated custom report for project $projectId",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadReportDetails(reportId: Long) {
        lifecycleScope.launch {
            val report: ProjectReport? = reportController.loadReportDetails(reportId)
            // Display or do something with the report object
        }
    }

    private fun onClickExportReport(reportId: Long) {
        lifecycleScope.launch {
            val result = reportController.exportReport(reportId)
            if (result.isSuccess) {
                val pdfBytes = result.getOrNull()
                // Save or open the PDF. For example:
                // openPdfFile(pdfBytes)
                Toast.makeText(
                    this@ProjectReportActivity,
                    "PDF Exported! Byte size: ${pdfBytes?.size}",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@ProjectReportActivity,
                    "Failed to export PDF: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
