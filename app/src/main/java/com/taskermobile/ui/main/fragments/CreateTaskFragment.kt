package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.widget.AdapterView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.taskermobile.data.api.RetroFitClient
import com.taskermobile.data.model.Task
import com.taskermobile.data.model.User
import com.taskermobile.data.repository.TaskRepository
import com.taskermobile.data.service.UserService
import com.taskermobile.data.service.TaskService
import com.taskermobile.data.session.SessionManager
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.databinding.FragmentCreateTaskBinding
import com.taskermobile.ui.main.controllers.CreateTaskController
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
    private var selectedUserId: Long? = null

    private lateinit var createTaskController: CreateTaskController

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

        sessionManager = SessionManager(requireContext())

        setupDependencies()
        setupSpinners()
        setupDatePicker()
        setupSubmitButton()

        // Fetch users
        createTaskController.fetchUsers { users ->
            populateUserDropdown(users)
        }
    }

    private fun setupDependencies() {
        val database = TaskerDatabase.getDatabase(requireContext())
        val application = requireActivity().application

        val userService = RetroFitClient.createService<UserService>(application, sessionManager)
        val taskService = RetroFitClient.createService<TaskService>(application, sessionManager)

        val taskRepository = TaskRepository(
            database.taskDao(),
            taskService,
            database.projectDao(),
            database.userDao(),
            database.notificationDao()
        )

        createTaskController = CreateTaskController(taskRepository, userService)
    }

    private fun populateUserDropdown(users: List<User>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            users.map { it.username }
        )
        binding.userDropdown.adapter = adapter

        binding.userDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedUserId = users[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedUserId = null
            }
        }
    }

    private fun setupSpinners() {
        binding.prioritySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            arrayOf("Low", "Medium", "High")
        )

        binding.statusSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            arrayOf("To Do", "In Progress", "Done")
        )
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

            viewLifecycleOwner.lifecycleScope.launch {
                val currentProjectId = sessionManager.currentProjectId.first()
                if (currentProjectId == null || currentProjectId == 0L) {
                    Toast.makeText(requireContext(), "No project selected", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val task = Task(
                    title = binding.titleInput.text.toString(),
                    description = binding.descriptionInput.text.toString(),
                    deadline = selectedDeadline?.format(DateTimeFormatter.ISO_DATE_TIME),
                    priority = binding.prioritySpinner.selectedItem.toString(),
                    status = binding.statusSpinner.selectedItem.toString(),
                    estimatedDuration = binding.durationInput.text.toString().toDoubleOrNull() ?: 0.0,
                    effortPercentage = binding.effortInput.text.toString().toDoubleOrNull() ?: 0.0,
                    dependency = binding.dependencyInput.text.toString().toLongOrNull(),
                    projectId = currentProjectId,
                    assignedUserId = selectedUserId,
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
                    scheduledProgress = null,
                    isTracking = false
                )

                showLoading(true)
                createTaskController.createTask(task) { success, errorMessage ->
                    showLoading(false)
                    if (success) {
                        Toast.makeText(requireContext(), "Task created successfully", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        binding.errorText.text = errorMessage
                        binding.errorText.visibility = View.VISIBLE
                    }
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
