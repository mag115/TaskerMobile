package com.taskermobile.ui.main.fragments

import android.util.Log
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.service.TaskService
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentMyTasksBinding
import com.taskermobile.ui.adapters.TaskAdapter
import com.taskermobile.ui.viewmodels.MyTasksViewModel
import com.taskermobile.ui.viewmodels.MyTasksViewModelFactory

class MyTasksFragment : Fragment() {
    private var _binding: FragmentMyTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MyTasksViewModel
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = TaskerDatabase.getDatabase(requireContext())
        val taskDao = database.taskDao()
        val projectDao = database.projectDao()
        val userDao = database.userDao()

        val sessionManager = SessionManager(requireContext())
        val taskService = RetrofitClient.createService<TaskService>(sessionManager)

        val taskRepository = TaskRepository(taskDao, taskService, projectDao, userDao)

        val factory = MyTasksViewModelFactory(taskRepository, sessionManager)
        viewModel = ViewModelProvider(this, factory)[MyTasksViewModel::class.java]

        adapter = TaskAdapter()
        binding.myTasksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.myTasksRecyclerView.adapter = adapter

        viewModel.myTasks.observe(viewLifecycleOwner) { tasks ->
            binding.progressBar.visibility = View.GONE // Hide the progress bar
            if (tasks.isEmpty()) {
                binding.errorText.visibility = View.VISIBLE
                binding.myTasksRecyclerView.visibility = View.GONE
            } else {
                binding.errorText.visibility = View.GONE
                binding.myTasksRecyclerView.visibility = View.VISIBLE
                adapter.submitList(tasks)
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            refreshTasks()
        }

        refreshTasks()
    }

    private fun refreshTasks() {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE
        viewModel.fetchTasksForUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}