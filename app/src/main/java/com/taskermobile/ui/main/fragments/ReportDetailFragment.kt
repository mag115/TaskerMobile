package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.TaskerApplication
import com.taskermobile.data.model.ProjectReport
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentReportDetailBinding
import com.taskermobile.ui.adapters.TaskAdapter
import com.taskermobile.ui.main.controllers.ProjectReportController
import com.taskermobile.ui.viewmodels.MyTasksViewModel
import kotlinx.coroutines.launch

class ReportDetailFragment : Fragment() {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MyTasksViewModel
    // Use Safe Args to retrieve the passed reportId.
    private val args: ReportDetailFragmentArgs by navArgs()

    private lateinit var reportController: ProjectReportController
    private lateinit var taskAdapter: TaskAdapter

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
        setupDependencies()
        setupRecyclerView()
        loadReportDetails(args.reportId)
    }

    private fun setupDependencies() {
        val sessionManager = SessionManager(requireContext())
        val db = (requireActivity().application as TaskerApplication).database
        val reportDao = db.projectReportDao()
        val taskDao = db.taskDao()
        reportController = ProjectReportController(sessionManager, reportDao, taskDao)
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            { task ->
                // Handle task timer click here
            },
            { task, comment ->
                viewModel.sendComment(task, comment) // ✅ Fix: Pass function for handling comments
            },
            viewModel // ✅ Pass ViewModel correctly
        ) // Remove the timer logic from the adapter
        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun loadReportDetails(reportId: Long) {
        // Optionally show a progress indicator here
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val report: ProjectReport? = reportController.loadReportDetails(reportId)
                if (report != null) {
                    bindReportToUI(report)
                } else {
                    Toast.makeText(requireContext(), "No report found for ID $reportId", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun bindReportToUI(report: ProjectReport) {
        // Bind the main fields of the report to the UI.
        binding.apply {
            reportIdText.text = "Report #${report.id}"
            reportDateText.text = "Date: ${report.reportDate ?: "N/A"}"
            // Bind additional fields as necessary.
        }
        // Use your existing TaskAdapter to display the tasks list.
        taskAdapter.submitList(report.tasks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
