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
import com.taskermobile.data.model.ProjectReport
import com.taskermobile.data.model.ReportOptions
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
        setupDependencies()
        setupRecyclerView()
        observeReports()

        // Load the reports when the fragment is shown
        viewLifecycleOwner.lifecycleScope.launch {
            reportController.fetchAllReports()
        }

        // Set up FAB click to generate a new report (using hardcoded projectId = 1 for demo)
        binding.fabGenerateReport.setOnClickListener {
            onClickGenerateReport(projectId = 1L)
        }
    }

    private fun setupDependencies() {
        val sessionManager = SessionManager(requireContext())
        val database = (requireActivity().application as TaskerApplication).database
        val reportDao = database.projectReportDao()
        val taskDao = database.taskDao()
        reportController = ProjectReportController(sessionManager, reportDao, taskDao)
    }

    private fun setupRecyclerView() {
        reportAdapter = ProjectReportAdapter(onItemClicked = { report ->
            // Instead of exporting the PDF, navigate to the detail screen
            navigateToReportDetail(report.id ?: return@ProjectReportAdapter)
        })
        binding.reportsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reportAdapter
        }
    }

    private fun observeReports() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
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
                        Toast.makeText(requireContext(), "Error: ${state.error.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun onClickGenerateReport(projectId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            reportController.generateReport(projectId)
            Toast.makeText(requireContext(), "Generated report for project $projectId", Toast.LENGTH_SHORT).show()
        }
    }

    // New function: Navigate to the report detail fragment
    private fun navigateToReportDetail(reportId: Long) {
        // Using Navigation Component's safe args:
        val action = ProjectReportFragmentDirections.actionProjectReportFragmentToReportDetailFragment(reportId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
