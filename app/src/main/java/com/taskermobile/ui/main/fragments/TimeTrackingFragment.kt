package com.taskermobile.ui.main.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.databinding.FragmentTimeTrackingBinding
import com.taskermobile.ui.adapters.TaskAdapter
import com.taskermobile.ui.viewmodels.TaskViewModel

class TimeTrackingFragment : Fragment() {

    private var _binding: FragmentTimeTrackingBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTimeTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe tasks from the ViewModel (using LiveData or Flow converted to LiveData)
        taskViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            adapter = TaskAdapter(tasks) { task, newTimeSpent ->
                // When timer stops, update the task's time spent
                taskViewModel.updateTaskTime(task.id ?: return@TaskAdapter, newTimeSpent)
            }
            binding.recyclerView.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}