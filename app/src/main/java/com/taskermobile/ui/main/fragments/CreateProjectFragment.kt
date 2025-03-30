package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.taskermobile.TaskerApplication
import com.taskermobile.data.model.Project
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentCreateProjectBinding
import com.taskermobile.ui.main.controllers.ProjectController
import kotlinx.coroutines.launch

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
        setupCreateButton()
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(requireContext())
        val application = requireActivity().application
        val projectDao = (application as TaskerApplication).database.projectDao()
        projectController = ProjectController(sessionManager, projectDao, application)
    }

    private fun setupCreateButton() {
        binding.createProjectButton.setOnClickListener {
            val projectName = binding.projectNameInput.text?.toString()?.trim()
            val projectDescription = binding.projectDescriptionInput.text?.toString()?.trim()

            if (projectName.isNullOrEmpty()) {
                binding.projectNameLayout.error = "Project name is required"
                return@setOnClickListener
            }

            createProject(projectName, projectDescription ?: "")
        }
    }

    private fun createProject(name: String, description: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                binding.createProjectButton.isEnabled = false

                val project = Project(
                    name = name,
                    description = description,
                    createdAt = null,
                    updatedAt = null,
                    tasks = emptyList(),
                    members = emptyList(),
                    owner = null
                )

                projectController.createProject(project)
                Toast.makeText(requireContext(), "Project created successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Failed to create project", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.createProjectButton.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 