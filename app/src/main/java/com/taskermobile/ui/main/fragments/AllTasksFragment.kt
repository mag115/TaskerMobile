package com.taskermobile.ui.main.fragments

import AllTasksViewModelFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.R
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentAllTasksBinding
import com.taskermobile.ui.adapters.TaskAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.service.TaskService
import com.taskermobile.ui.viewmodels.AllTasksViewModel

class AllTasksFragment : Fragment() {
    private var _binding: FragmentAllTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var sessionManager: SessionManager
    private var loadTasksJob: Job? = null
    private lateinit var viewModel: AllTasksViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDependencies()
        initViewModel()
        setupRecyclerView()
        setupFab()
        setupSwipeRefresh()
        observeProjectChanges()
        observeTasks()
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(requireContext())
    }

    private fun initViewModel() {
        val database = TaskerDatabase.getDatabase(requireContext())
        val taskDao = database.taskDao()
        val projectDao = database.projectDao()
        val userDao = database.userDao()

        val taskService = RetrofitClient.createService<TaskService>(sessionManager)
        val taskRepository = TaskRepository(taskDao, taskService, projectDao, userDao)

        // Use the factory for AllTasksViewModel
        val factory = AllTasksViewModelFactory(taskRepository, sessionManager)
        viewModel = ViewModelProvider(this, factory)[AllTasksViewModel::class.java]
    }

    private fun setupRecyclerView() {
        // Pass the viewModel (which implements TaskUpdater) to the adapter
        taskAdapter = TaskAdapter({ task ->
            // Timer click logic if needed
        }, viewModel)
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tasksRecyclerView.adapter = taskAdapter
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_allTasksFragment_to_createTaskFragment)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val projectId = sessionManager.currentProjectId.first()
                    if (projectId != null && projectId != 0L) {
                        viewModel.fetchTasksForProject(projectId)
                    }
                } catch (e: Exception) {
                    Log.e("AllTasksFragment", "Error refreshing tasks: ${e.message}")
                } finally {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    // Listen for changes in the current project and fetch tasks accordingly
    private fun observeProjectChanges() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionManager.currentProjectId.collect { projectId ->
                    if (projectId == null || projectId == 0L) {
                        binding.apply {
                            progressBar.visibility = View.GONE
                            emptyStateText.visibility = View.VISIBLE
                            emptyStateText.text = "No project selected"
                            tasksRecyclerView.visibility = View.GONE
                        }
                    } else {
                        Log.d("AllTasksFragment", "Loading tasks for project ID: $projectId")
                        viewModel.fetchTasksForProject(projectId)
                    }
                }
            }
        }
    }

    // Observe the tasks LiveData to update the UI
    private fun observeTasks() {
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            binding.progressBar.visibility = View.GONE
            if (tasks.isNullOrEmpty()) {
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = "No tasks found"
                binding.tasksRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateText.visibility = View.GONE
                binding.tasksRecyclerView.visibility = View.VISIBLE
                taskAdapter.submitList(tasks)
            }
        }
    }

    override fun onDestroyView() {
        loadTasksJob?.cancel()
        _binding = null
        super.onDestroyView()
    }
}
