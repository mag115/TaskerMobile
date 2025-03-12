package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.os.Handler
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
import com.taskermobile.ui.main.controllers.TaskController
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.taskermobile.data.model.Task
import com.taskermobile.ui.viewmodels.AllTasksViewModel
import com.taskermobile.ui.viewmodels.MyTasksViewModel

class AllTasksFragment : Fragment() {
    private var _binding: FragmentAllTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskController: TaskController
    private lateinit var sessionManager: SessionManager
    private var loadTasksJob: Job? = null
    private lateinit var viewModel: MyTasksViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDependencies()
        setupRecyclerView()
        setupFab()
        setupSwipeRefresh()
        observeProjectChanges()
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(requireContext())
        taskController = TaskController(requireContext(), sessionManager)
    }

    private fun setupRecyclerView() {
        // Pass the ViewModel to the adapter, so the adapter can access the updateTaskTime method
        taskAdapter = TaskAdapter({ task ->
            // Here you can call whatever is necessary for each task click
            // Timer click logic
        }, viewModel)
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(context)
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
                    if (projectId != null) {
                        taskController.syncTasks()
                        taskController.refreshProjectTasks(projectId)
                    }
                } catch (e: Exception) {
                    Log.e("AllTasksFragment", "Error syncing tasks: ${e.message}")
                } finally {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun observeProjectChanges() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sessionManager.currentProjectId.collect { projectId ->
                    loadTasksJob?.cancel()
                    loadTasksJob = launch {
                        // Now it's safe to use binding because this block is active only when view is STARTED
                        binding.progressBar.visibility = View.VISIBLE
                        binding.emptyStateText.visibility = View.GONE

                        if (projectId == null || projectId == 0L) {
                            binding.apply {
                                progressBar.visibility = View.GONE
                                emptyStateText.visibility = View.VISIBLE
                                emptyStateText.text = "No project selected"
                                tasksRecyclerView.visibility = View.GONE
                            }
                            return@launch
                        }

                        Log.d("AllTasksFragment", "Loading tasks for project ID: $projectId")
                        taskController.getAllTasks().collect { tasks ->
                            Log.d("AllTasksFragment", "Received ${tasks.size} tasks from repository") // 🛠️ Debugging log

                            binding.apply {
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

                    }
                }
            }
        }
    }

    // Updates the task data (time spent)
    private fun updateTaskList(task: Task) {
        task.id?.let { viewModel.updateTaskTime(it, task.timeSpent) }
    }

    override fun onDestroyView() {
        loadTasksJob?.cancel()
        _binding = null
        super.onDestroyView()
    }
}
