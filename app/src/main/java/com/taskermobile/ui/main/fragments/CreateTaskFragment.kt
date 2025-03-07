package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.taskermobile.data.model.Task
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentCreateTaskBinding
import com.taskermobile.ui.main.controllers.TaskController
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CreateTaskFragment : Fragment() {
    private var _binding: FragmentCreateTaskBinding? = null
    private val binding get() = _binding!!
    private lateinit var taskController: TaskController
    private lateinit var sessionManager: SessionManager
    private var selectedDeadline: LocalDateTime? = null

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
        // Initialize TaskController with SessionManager
        taskController = TaskController(requireContext(), sessionManager)

        setupSpinners()
        setupDatePicker()
        setupSubmitButton()
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

            val task = Task(
                title = binding.titleInput.text.toString(),
                description = binding.descriptionInput.text.toString(),
                deadline = selectedDeadline?.format(DateTimeFormatter.ISO_DATE_TIME),
                priority = binding.prioritySpinner.selectedItem.toString(),
                status = binding.statusSpinner.selectedItem.toString(),
                estimatedDuration = binding.durationInput.text.toString().toDoubleOrNull() ?: 0.0,
                effortPercentage = binding.effortInput.text.toString().toFloatOrNull() ?: 0f,
                dependency = binding.dependencyInput.text.toString(),
                projectId = 1L // Default project ID
            )

            lifecycleScope.launch {
                try {
                    showLoading(true)
                    taskController.createTask(task)
                    Toast.makeText(context, "Task created successfully", Toast.LENGTH_SHORT).show()
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