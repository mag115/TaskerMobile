package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.databinding.FragmentMyTasksBinding
import com.taskermobile.ui.adapters.TaskAdapter
import com.taskermobile.ui.viewmodels.MyTasksViewModel

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

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MyTasksViewModel::class.java]

        // Setup RecyclerView
        adapter = TaskAdapter()
        binding.myTasksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.myTasksRecyclerView.adapter = adapter

        // Observe LiveData from ViewModel
        viewModel.myTasks.observe(viewLifecycleOwner) { tasks ->
            binding.progressBar.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false // Stop refresh animation

            if (tasks.isEmpty()) {
                binding.errorText.visibility = View.VISIBLE
                binding.myTasksRecyclerView.visibility = View.GONE
            } else {
                binding.errorText.visibility = View.GONE
                binding.myTasksRecyclerView.visibility = View.VISIBLE
                adapter.submitList(tasks)
            }
        }

        // Pull to Refresh
        binding.swipeRefresh.setOnRefreshListener {
            refreshTasks()
        }

        refreshTasks() // Initial fetch
    }

    private fun refreshTasks() {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE
        viewModel.fetchMyTasks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
