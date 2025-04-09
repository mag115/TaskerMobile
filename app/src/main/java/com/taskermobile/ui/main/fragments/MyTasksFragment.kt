package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.data.api.RetroFitClient
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.data.model.Task
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.service.TaskService
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentMyTasksBinding
import com.taskermobile.ui.adapters.TaskAdapter
import com.taskermobile.ui.main.controllers.MyTasksController
import androidx.activity.result.contract.ActivityResultContracts

import kotlinx.coroutines.flow.first

import android.app.Application


import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent

class MyTasksFragment : Fragment() {
    private var _binding: FragmentMyTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var application: Application

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var myTasksController: MyTasksController
    private lateinit var selectedTask: Task

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.d("MyTasksFragment", "Picked image URI: $it")
            
            try {
                // Take a persistent URI permission to keep access after restart
                requireContext().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                
                val updatedTask = selectedTask.copy(imageUri = it.toString())

                val currentList = taskAdapter.currentList.toMutableList()
                val index = currentList.indexOfFirst { task -> task.id == selectedTask.id }

                if (index != -1) {
                    currentList[index] = updatedTask
                    taskAdapter.submitList(currentList)
                }

                myTasksController.updateTask(updatedTask)
                CoroutineScope(Dispatchers.IO).launch {
                    myTasksController.taskDao.updateImageUri(updatedTask.id, updatedTask.imageUri ?: "")
                    Log.d("MyTasksFragment", "Image URI saved to database: ${updatedTask.imageUri}")
                }
            } catch (e: SecurityException) {
                Log.e("MyTasksFragment", "Failed to take persistent URI permission", e)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyTasksBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        application = requireActivity().application 
        setupDependencies()
        setupRecyclerView()
        setupSwipeRefresh()
        loadTasks()
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(requireContext())

        val database = TaskerDatabase.getDatabase(requireContext())
        val taskDao = database.taskDao()
        val userDao = database.userDao()
        val projectDao = database.projectDao()
        val notificationDao = database.notificationDao()
        val taskService = RetroFitClient.createService<TaskService>(application, sessionManager)

        val taskRepository = TaskRepository(taskDao, taskService, userDao, notificationDao, projectDao)

        myTasksController = MyTasksController(taskRepository, sessionManager, taskDao)
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            taskActions = myTasksController,
            onTaskClick = { task ->
            },
            onCommentSend = { task, comment ->
                myTasksController.sendComment(task, comment)
            },
            onAttachPhoto = { task ->
                selectedTask = task
                pickImageLauncher.launch("image/*")
            },

        )

        binding.myTasksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.myTasksRecyclerView.adapter = taskAdapter
    }


    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadTasks()

        }
    }

    private fun loadTasks() {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE

        myTasksController.getMyTasks({ tasks ->
            if (!isAdded || _binding == null) return@getMyTasks

            binding.progressBar.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false

            if (tasks.isNullOrEmpty()) {
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = "No tasks assigned to you"
                binding.myTasksRecyclerView.visibility = View.GONE
            } else {
                binding.errorText.visibility = View.GONE
                binding.myTasksRecyclerView.visibility = View.VISIBLE

                //merge backend tasks w room images and ensure timer states are correct
                CoroutineScope(Dispatchers.IO).launch {
                    val localTasksFlow = myTasksController.taskDao.getAllTasks()
                    val localTasks = localTasksFlow.first()

                    val mergedTasks = tasks.map { taskFromServer ->
                        val local = localTasks.find { it.id == taskFromServer.id }
                        
                        // Start with default task from server
                        var mergedTask = taskFromServer
                        
                        // Copy image URI if exists locally
                        if (!local?.imageUri.isNullOrEmpty()) {
                            mergedTask = mergedTask.copy(imageUri = local?.imageUri)
                        }
                        
                        // Check for tracking status
                        if (mergedTask.isTracking && mergedTask.timerId != null) {
                            // Calculate current elapsed time based on tracking start time
                            val now = System.currentTimeMillis()
                            val trackingStartTime = mergedTask.timerId ?: now
                            val currentTimeElapsed = (now - trackingStartTime) / 1000
                            
                            // Update elapsed time if tracking
                            if (currentTimeElapsed > 0) {
                                mergedTask = mergedTask.copy(
                                    timeSpent = mergedTask.timeSpent + currentTimeElapsed,
                                    elapsedTime = mergedTask.timeSpent + currentTimeElapsed
                                )
                                Log.d("MyTasksFragment", "Updated tracking task ${mergedTask.id} elapsed time: ${mergedTask.timeSpent}s")
                            }
                        }
                        
                        mergedTask
                    }

                    withContext(Dispatchers.Main) {
                        taskAdapter.submitList(mergedTasks)
                    }
                }
            }
        }, refresh = true)  // Set to true to ensure we always get fresh data
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
