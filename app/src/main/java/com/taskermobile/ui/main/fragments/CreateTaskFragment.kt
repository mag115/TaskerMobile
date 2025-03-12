package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.widget.AdapterView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.model.Task
import com.taskermobile.data.model.User
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.service.UserService
import com.taskermobile.data.service.TaskService
import com.taskermobile.data.session.SessionManager
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.databinding.FragmentCreateTaskBinding
import com.taskermobile.ui.viewmodels.CreateTaskViewModel
import com.taskermobile.ui.viewmodels.CreateTaskViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CreateTaskFragment : Fragment() {
    private var _binding: FragmentCreateTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var selectedDeadline: LocalDateTime? = null
    private var selectedUserId: Long? = null // Store selected user ID
    private var selectedUserName: String? = null

    // Initialize Database
    private val database by lazy { TaskerDatabase.getDatabase(requireContext()) }

    // Initialize API services
    private val userService: UserService by lazy {
        RetrofitClient.createService<UserService>(SessionManager(requireContext()))
    }
    private val taskService: TaskService by lazy {
        RetrofitClient.createService<TaskService>(sessionManager)
    }
    // Initialize Repository
    private val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao(), taskService,database.projectDao(), database.userDao())
    }

    // Initialize ViewModel using Factory
    private val viewModel: CreateTaskViewModel by viewModels {
        CreateTaskViewModelFactory(taskRepository, userService)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SessionManager
        sessionManager = SessionManager(requireContext())

        setupSpinners()
        setupDatePicker()
        setupSubmitButton()
        setupObservers()

        viewModel.fetchUsers() // Fetch users from backend
    }

    private fun setupObservers() {
        // Observe users and populate dropdown
        viewModel.users.observe(viewLifecycleOwner, Observer { users ->
            populateUserDropdown(users)
        })
    }

    private fun populateUserDropdown(users: List<User>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            users.map { it.username } // Display usernames in dropdown
        )
        binding.userDropdown.adapter = adapter

        binding.userDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedUserId = users[position].id // Store selected user ID
                //selectedUserName = users[position].username // This is the user name
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedUserId = null
            }
        }
    }

    private fun setupSpinners() {
        // Priority Spinner
        val priorities = arrayOf("Low", "Medium", "High")
        val priorityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            priorities
        )
        binding.prioritySpinner.adapter = priorityAdapter

        // Status Spinner
        val statuses = arrayOf("To Do", "In Progress", "Done")
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            statuses
        )
        binding.statusSpinner.adapter = statusAdapter
    }

    private fun setupDatePicker() {
        binding.deadlinePicker.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select deadline")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDeadline = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(selection),
                    ZoneId.systemDefault()
                )
                binding.deadlineText.text = selectedDeadline?.format(
                    DateTimeFormatter.ofPattern("MMM dd, yyyy")
                )
            }

            datePicker.show(parentFragmentManager, "DEADLINE_PICKER")
        }
    }

    private fun setupSubmitButton() {
        binding.createTaskButton.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            // Launch a coroutine to read the current project ID
            viewLifecycleOwner.lifecycleScope.launch {
                // Retrieve the current project ID from the session
                val currentProjectId = sessionManager.currentProjectId.first()
                if (currentProjectId == null || currentProjectId == 0L) {
                    Toast.makeText(requireContext(), "No project selected", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Build the task, using the current project ID
                val task = Task(
                    title = binding.titleInput.text.toString(),
                    description = binding.descriptionInput.text.toString(),
                    deadline = selectedDeadline?.format(DateTimeFormatter.ISO_DATE_TIME),
                    priority = binding.prioritySpinner.selectedItem.toString(),
                    status = binding.statusSpinner.selectedItem.toString(),
                    estimatedDuration = binding.durationInput.text.toString().toDoubleOrNull() ?: 0.0,
                    effortPercentage = binding.effortInput.text.toString().toDoubleOrNull() ?: 0.0,
                    dependency = binding.dependencyInput.text.toString().toLongOrNull(),
                    projectId = currentProjectId, // Use the current project ID from SessionManager
                    assignedUserId = selectedUserId, // Assign selected user
                    reminderSent = false,
                    estimatedWeeks = null,
                    progressStatus = null,
                    progress = null,
                    manualProgress = null,
                    isDeleted = false,
                    project = null,
                    assignedUser = null,
                    timeSpent = 0.0,
                    elapsedTime = 0.0,
                    scheduledProgress = null
                )

                try {
                    showLoading(true)
                    viewModel.createTask(task)
                    Toast.makeText(requireContext(), "Task created successfully", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } catch (e: Exception) {
                    binding.errorText.text = e.message
                    binding.errorText.visibility = View.VISIBLE
                } finally {
                    showLoading(false)
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (binding.titleInput.text.isNullOrBlank()) {
            binding.titleInput.error = "Title is required"
            return false
        }
        return true
    }

    private fun showLoading(isLoading: Boolean) {
        binding.createTaskButton.isEnabled = !isLoading
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}