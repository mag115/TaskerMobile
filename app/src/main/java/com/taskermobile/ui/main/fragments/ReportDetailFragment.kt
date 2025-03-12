package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.TaskerApplication
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.service.TaskService
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentReportDetailBinding
import com.taskermobile.ui.adapters.TaskAdapter
import com.taskermobile.ui.main.controllers.ProjectReportController
import com.taskermobile.ui.viewmodels.MyTasksViewModel
import com.taskermobile.ui.viewmodels.MyTasksViewModelFactory
import kotlinx.coroutines.launch

class ReportDetailFragment : Fragment() {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MyTasksViewModel
    private lateinit var taskAdapter: TaskAdapter
    // Use Safe Args to retrieve the passed reportId.
    private val args: ReportDetailFragmentArgs by navArgs()

    private lateinit var reportController: ProjectReportController

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
        initViewModel() // Initialize the viewModel here
        setupRecyclerView() // viewModel is available to pass to the adapter
        loadReportDetails(args.reportId)
    }

    private fun setupDependencies() {
        // Set up your session manager and report controller.
        val sessionManager = SessionManager(requireContext())
        val app = requireActivity().application as TaskerApplication
        val db = app.database
        val reportDao = db.projectReportDao()
        val taskDao = db.taskDao()
        reportController = ProjectReportController(sessionManager, reportDao, taskDao)
    }

    private fun initViewModel() {
        // Retrieve the database from the Application and create the repository and factory.
        val app = requireActivity().application as TaskerApplication
        val db = app.database
        val taskDao = db.taskDao()
        val projectDao = db.projectDao()
        val userDao = db.userDao()
        val sessionManager = SessionManager(requireContext())
        val taskService = RetrofitClient.createService<TaskService>(sessionManager)
        val taskRepository = TaskRepository(taskDao, taskService, projectDao, userDao)
        val factory = MyTasksViewModelFactory(taskRepository, sessionManager, taskDao)
        viewModel = ViewModelProvider(this, factory)[MyTasksViewModel::class.java]
    }

    private fun setupRecyclerView() {
<<<<<<< HEAD
        taskAdapter = TaskAdapter(
            { task ->
                // Handle task timer click here
            },
            { task, comment ->
                viewModel.sendComment(task, comment) // ✅ Fix: Pass function for handling comments
            },
            viewModel // ✅ Pass ViewModel correctly
        ) // Remove the timer logic from the adapter
=======
        // Create the adapter passing the viewModel (which implements task updates)
        taskAdapter = TaskAdapter({ task ->
            // Timer click logic can be added here if needed.
        }, viewModel)
>>>>>>> e2db3df90dc2ef5f9ae4bd1ed933e8090134723b
        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun loadReportDetails(reportId: Long) {
        // Optionally show a progress indicator here.
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val report = reportController.loadReportDetails(reportId)
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

    private fun bindReportToUI(report: com.taskermobile.data.model.ProjectReport) {
        binding.apply {
            reportIdText.text = "Report #${report.id}"
            reportDateText.text = "Date: ${report.reportDate ?: "N/A"}"
            // Bind additional fields as necessary.
        }
        // Use the TaskAdapter to display the tasks from the report.
        taskAdapter.submitList(report.tasks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
