package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.databinding.FragmentAllTasksBinding
import com.taskermobile.ui.adapters.TaskAdapter
import com.taskermobile.ui.main.controllers.AllTasksController
import com.taskermobile.ui.main.controllers.TaskController
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class AllTasksFragment : Fragment() {

    private var _binding: FragmentAllTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter
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

        // Initialize TaskController first
        val taskController = TaskController(requireContext(), sessionManager)

        // Pass TaskController into AllTasksController
        allTasksController = AllTasksController(taskController)
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            taskActions = allTasksController,
            onTaskClick = { task ->
                // Handle task click, e.g., navigate to details
            },
            onCommentSend = { task, comment ->
                lifecycleScope.launch {
                    allTasksController.sendComment(task, comment)
                }
            }
        )

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
            if (projectId != null && projectId != 0L) {
                loadTasks()
            } else {
                binding.progressBar.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
                binding.emptyStateText.text = "No project selected"
            }
        }
    }

    private fun loadTasks() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyStateText.visibility = View.GONE

        allTasksController.getAllTasks { tasks ->
            if (!isAdded || activity == null) return@getAllTasks // Prevent crash if fragment is detached

            requireActivity().runOnUiThread {  // Ensures UI updates on the Main Thread
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false

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
    }




    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
