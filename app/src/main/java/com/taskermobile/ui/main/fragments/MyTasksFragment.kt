package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskermobile.data.api.RetrofitClient
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

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyTasksFragment : Fragment() {
    private var _binding: FragmentMyTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var myTasksController: MyTasksController
    private lateinit var selectedTask: Task

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.d("MyTasksFragment", "Picked image URI: $it")
            val updatedTask = selectedTask.copy(imageUri = it.toString())

            val currentList = taskAdapter.currentList.toMutableList()
            val index = currentList.indexOfFirst { it.id == selectedTask.id }

            if (index != -1) {
                currentList[index] = updatedTask
                taskAdapter.submitList(currentList)
            }

            myTasksController.updateTask(updatedTask)
            CoroutineScope(Dispatchers.IO).launch {
                myTasksController.taskDao.updateImageUri(updatedTask.id, updatedTask.imageUri ?: "")
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

        setupDependencies()
        setupRecyclerView()
        setupSwipeRefresh()
        loadTasks()
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(requireContext())

        val database = TaskerDatabase.getDatabase(requireContext())
        val taskDao = database.taskDao()
        val projectDao = database.projectDao()
        val userDao = database.userDao()
        val notificationDao = database.notificationDao()
        val taskService = RetrofitClient.createService<TaskService>(sessionManager)

        val taskRepository = TaskRepository(taskDao, taskService, projectDao, userDao, notificationDao)

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

                //merge backend tasks w room images
                CoroutineScope(Dispatchers.IO).launch {
                    val localTasksFlow = myTasksController.taskDao.getAllTasks()
                    val localTasks = localTasksFlow.first()

                    val mergedTasks = tasks.map { taskFromServer ->
                        val local = localTasks.find { it.id == taskFromServer.id }
                        if (!local?.imageUri.isNullOrEmpty()) {
                            taskFromServer.copy(imageUri = local?.imageUri)
                        } else taskFromServer
                    }

                    withContext(Dispatchers.Main) {
                        taskAdapter.submitList(mergedTasks)
                    }
                }
            }
        }, refresh = false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
