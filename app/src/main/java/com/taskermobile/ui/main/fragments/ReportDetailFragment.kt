package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.TaskerApplication
import com.taskermobile.data.model.ProjectReport
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentReportDetailBinding
import com.taskermobile.ui.adapters.TaskAdapter
import com.taskermobile.ui.main.controllers.ProjectReportController
import kotlinx.coroutines.launch

class ReportDetailFragment : Fragment() {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var reportController: ProjectReportController
    private lateinit var taskAdapter: TaskAdapter

    // Suppose you pass the reportId via arguments, Safe Args, etc.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Setup the controller, adapter, etc.
        setupDependencies()
        setupRecyclerView()

        // 2) Retrieve the report ID from arguments or safe args:
        val reportId = arguments?.getLong("reportId") ?: 0L

        // 3) Load the report and bind UI
        loadReportDetails(reportId)
    }

    private fun setupDependencies() {
        val sessionManager = SessionManager(requireContext())
        val db = (requireActivity().application as TaskerApplication).database
        reportController = ProjectReportController(sessionManager, db.projectReportDao())
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter()
        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun loadReportDetails(reportId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Show a progress bar or something if you like
            try {
                val report: ProjectReport? = reportController.loadReportDetails(reportId)
                if (report != null) {
                    bindReportToUI(report)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No report found for ID $reportId",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun bindReportToUI(report: ProjectReport) {
        // Bind the report's main fields
        binding.apply {
            reportIdText.text = "Report #${report.id}"
            reportDateText.text = "Date: ${report.reportDate ?: "N/A"}"
            // ... any other fields ...
        }

        // Submit tasks to your existing TaskAdapter
        taskAdapter.submitList(report.tasks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
