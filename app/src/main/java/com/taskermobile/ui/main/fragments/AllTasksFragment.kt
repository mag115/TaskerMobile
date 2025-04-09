package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.databinding.FragmentAllTasksBinding
import com.taskermobile.ui.adapters.AllTasksAdapter
import com.taskermobile.ui.main.controllers.AllTasksController
import com.taskermobile.ui.main.controllers.TaskController
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.taskermobile.data.model.Task

class AllTasksFragment : Fragment() {

    private var _binding: FragmentAllTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskAdapter: AllTasksAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var allTasksController: AllTasksController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDependencies()
        setupRecyclerView()
        setupSwipeRefresh()
        observeProjectChanges()
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(requireContext())
        val taskController = TaskController(requireContext(), sessionManager, requireActivity().application)
        allTasksController = AllTasksController(taskController)
    }

    private fun setupRecyclerView() {
        taskAdapter = AllTasksAdapter()
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tasksRecyclerView.adapter = taskAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadTasks()
        }
    }

    private fun observeProjectChanges() {
        lifecycleScope.launch {
            val projectId = sessionManager.currentProjectId.first()
            Log.d("AllTasksFragment", "Observing project changes. Current projectId: $projectId")
            if (projectId != null && projectId != 0L) {
                Log.d("AllTasksFragment", "Valid project ID found, loading tasks")
                loadTasks()
            } else {
                Log.d("AllTasksFragment", "No valid project ID found")
                binding.progressBar.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = "No project selected"
            }
        }
    }

    private fun loadTasks() {
        Log.d("AllTasksFragment", "Starting to load tasks")
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyStateText.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val projectId = sessionManager.currentProjectId.first()
                Log.d("AllTasksFragment", "Loading tasks for projectId: $projectId")
                
                allTasksController.getAllTasks(projectId) { tasks ->
                    if (!isAdded || activity == null) {
                        Log.d("AllTasksFragment", "Fragment not attached, skipping UI update")
                        return@getAllTasks
                    }

                    requireActivity().runOnUiThread {
                        Log.d("AllTasksFragment", "Updating UI with tasks")
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false

                        if (tasks.isNullOrEmpty()) {
                            Log.d("AllTasksFragment", "No tasks found, showing empty state")
                            binding.emptyStateText.visibility = View.VISIBLE
                            binding.emptyStateText.text = "No tasks found"
                            binding.tasksRecyclerView.visibility = View.GONE
                        } else {
                            Log.d("AllTasksFragment", "Tasks found, updating recycler view with ${tasks.size} tasks")
                            binding.emptyStateText.visibility = View.GONE
                            binding.tasksRecyclerView.visibility = View.VISIBLE
                            taskAdapter.submitList(tasks)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AllTasksFragment", "Error loading tasks", e)
                Log.e("AllTasksFragment", "Stack trace: ${e.stackTraceToString()}")
                requireActivity().runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    binding.emptyStateText.visibility = View.VISIBLE
                    binding.emptyStateText.text = "Error loading tasks: ${e.message}"
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
