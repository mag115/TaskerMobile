package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.taskermobile.TaskerApplication
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentCreateProjectBinding
import com.taskermobile.ui.main.controllers.ProjectController
import kotlinx.coroutines.launch
import android.widget.Toast
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CreateProjectFragment : Fragment() {
    private var _binding: FragmentCreateProjectBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var projectController: ProjectController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateProjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDependencies()
        setupSubmitButton()
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(requireContext())
        val application = requireActivity().application as TaskerApplication
        val database = application.database
        projectController = ProjectController(
            sessionManager,
            database.projectDao(),
            database.taskDao(),
            database.userDao(),
            application
        )
    }

    private fun setupSubmitButton() {
        binding.createProjectButton.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                    val project = com.taskermobile.data.model.Project(
                        name = binding.projectNameInput.text.toString(),
                        description = binding.projectDescriptionInput.text.toString(),
                        createdAt = currentTime,
                        updatedAt = currentTime,
                        tasks = emptyList(),
                        members = emptyList(),
                        owner = null
                    )

                    val result = projectController.createProject(project)
                    if (result.isSuccess) {
                        Toast.makeText(requireContext(), "Project created successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), "Failed to create project: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.projectNameInput.text.isNullOrBlank()) {
            binding.projectNameInput.error = "Project name is required"
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 