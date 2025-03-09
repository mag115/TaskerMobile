package com.taskermobile.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.taskermobile.TaskerApplication
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentProjectsBinding
import com.taskermobile.ui.adapters.ProjectAdapter
import com.taskermobile.ui.main.controllers.ProjectController
import com.taskermobile.ui.main.controllers.ProjectsState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProjectsFragment : Fragment() {
    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var projectController: ProjectController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDependencies()
        setupRecyclerView()
        setupFab()
        loadProjects()
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(requireContext())
        val projectDao = (requireActivity().application as TaskerApplication).database.projectDao()
        projectController = ProjectController(sessionManager, projectDao)
    }

    private fun setupRecyclerView() {
        projectAdapter = ProjectAdapter()
        binding.projectsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = projectAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddProject.setOnClickListener {
            findNavController().navigate(ProjectsFragmentDirections.actionProjectsFragmentToCreateProject())
        }
    }

    private fun loadProjects() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                projectController.fetchAllProjects()
                projectController.projects.collectLatest { state ->
                    when (state) {
                        is ProjectsState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.errorText.visibility = View.GONE
                        }
                        is ProjectsState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.errorText.visibility = View.GONE
                            projectAdapter.submitList(state.projects)
                        }
                        is ProjectsState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.errorText.visibility = View.VISIBLE
                            binding.errorText.text = state.exception.message
                        }
                    }
                }
            } catch (e: Exception) {
                binding.errorText.text = e.message
                binding.errorText.visibility = View.VISIBLE
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 