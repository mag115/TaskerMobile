package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.TaskerApplication
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentProjectReportBinding
import com.taskermobile.ui.adapters.ProjectReportAdapter
import com.taskermobile.ui.main.controllers.ProjectReportController
import com.taskermobile.ui.main.controllers.ReportState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProjectReportFragment : Fragment() {

    private var _binding: FragmentProjectReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var reportController: ProjectReportController
    private lateinit var reportAdapter: ProjectReportAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupDependencies()
        setupRecyclerView()
        observeReports()

        // Load the reports when the fragment is shown
        lifecycleScope.launch {
            reportController.fetchAllReports()
        }

        // Set up FAB click to generate a new report (for demonstration, using hardcoded projectId = 1)
        binding.fabGenerateReport.setOnClickListener {
            onClickGenerateReport(projectId = 1L)
        }

        // Update adapter's project name using the current project from SessionManager.
        lifecycleScope.launch {
            sessionManager.getCurrentProject()?.let { project ->
                reportAdapter.setProjectName(project.name)
            }
        }
    }

    private fun setupDependencies() {
        val application = requireActivity().application // Get application
        val database = (application as TaskerApplication).database
        val reportDao = database.projectReportDao()
        val taskDao = database.taskDao()
        // Pass application context to ProjectReportController
        reportController = ProjectReportController(sessionManager, reportDao, taskDao, application)
    }

    private fun setupRecyclerView() {
        // Provide an initial default name; it will be updated once the current project is loaded.
        reportAdapter = ProjectReportAdapter("Current Project", onItemClicked = { report ->
            navigateToReportDetail(report.id ?: return@ProjectReportAdapter)
        })
        binding.reportsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportAdapter
        }
    }

    private fun observeReports() {
        lifecycleScope.launchWhenStarted {
            reportController.uiState.collectLatest { state ->
                when (state) {
                    is ReportState.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is ReportState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        reportAdapter.submitList(state.reports)
                    }
                    is ReportState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error: ${state.error.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun onClickGenerateReport(projectId: Long) {
        lifecycleScope.launch {
            reportController.generateReport(projectId)
            Toast.makeText(requireContext(), "Generated report for project $projectId", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToReportDetail(reportId: Long) {
        val action = ProjectReportFragmentDirections.actionProjectReportFragmentToReportDetailFragment(reportId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
