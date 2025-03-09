package com.taskermobile.ui.main.fragments

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
import com.taskermobile.databinding.FragmentTasksBinding
import com.taskermobile.ui.adapters.TaskAdapter
import com.taskermobile.ui.main.controllers.TaskController
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log

class TasksFragment : Fragment() {
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskController: TaskController
    private lateinit var sessionManager: SessionManager
    private var loadTasksJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDependencies()
        setupRecyclerView()
        setupFab()
        observeProjectChanges()
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(requireContext())
        taskController = TaskController(requireContext(), sessionManager)
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter()
        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_tasksFragment_to_createTaskFragment)
        }
    }

    private fun observeProjectChanges() {
        // Single coroutine to handle both project changes and task loading
        viewLifecycleOwner.lifecycleScope.launch {
            sessionManager.currentProjectId.collect { projectId ->
                loadTasksJob?.cancel() // Cancel any existing task loading
                loadTasksJob = launch {
                    try {
                        _binding?.progressBar?.visibility = View.VISIBLE
                        _binding?.emptyStateText?.visibility = View.GONE
                        
                        if (projectId == null) {
                            _binding?.apply {
                                progressBar.visibility = View.GONE
                                emptyStateText.visibility = View.VISIBLE
                                emptyStateText.text = "No project selected"
                                tasksRecyclerView.visibility = View.GONE
                            }
                            return@launch
                        }
                        
                        Log.d("TasksFragment", "Loading tasks for project ID: $projectId")
                        
                        taskController.getTasksByProject(projectId).collect { tasks ->
                            _binding?.apply {
                                progressBar.visibility = View.GONE
                                if (tasks.isEmpty()) {
                                    emptyStateText.visibility = View.VISIBLE
                                    emptyStateText.text = "No tasks found"
                                    tasksRecyclerView.visibility = View.GONE
                                } else {
                                    emptyStateText.visibility = View.GONE
                                    tasksRecyclerView.visibility = View.VISIBLE
                                    taskAdapter.submitList(tasks)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        when (e) {
                            is kotlinx.coroutines.CancellationException -> {
                                Log.d("TasksFragment", "Task loading cancelled")
                                // Don't show error UI for normal cancellation
                            }
                            else -> {
                                _binding?.apply {
                                    progressBar.visibility = View.GONE
                                    emptyStateText.visibility = View.VISIBLE
                                    emptyStateText.text = e.message ?: "Error loading tasks"
                                    tasksRecyclerView.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        loadTasksJob?.cancel()
        _binding = null
        super.onDestroyView()
    }
} 