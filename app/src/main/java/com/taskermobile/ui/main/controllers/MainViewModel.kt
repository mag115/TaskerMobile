package com.taskermobile.ui.main.controllers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taskermobile.TaskerApplication
import com.taskermobile.data.api.RetroFitClient
import com.taskermobile.data.model.Project
import com.taskermobile.data.repository.ProjectRepository
import com.taskermobile.data.service.ProjectService
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager: SessionManager = SessionManager(application)
    private val projectService: ProjectService = RetroFitClient.createService(application, sessionManager)
    private val database = (application as TaskerApplication).database
    private val projectDao = database.projectDao()
    private val taskDao = database.taskDao()
    private val userDao = database.userDao()
    private val projectRepository = ProjectRepository(projectService, projectDao, taskDao, userDao)

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject: StateFlow<Project?> = _currentProject.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            loadProjects()
            
            sessionManager.getCurrentProject()?.let { project ->
                _currentProject.value = project
            }

            projectRepository.getLocalProjects().collectLatest { projectList ->
                _projects.value = projectList
                _isLoading.value = false
                
                if (_currentProject.value == null && projectList.isNotEmpty()) {
                    _currentProject.value = projectList.first()
                    saveCurrentProject(projectList.first())
                }
            }
        }
    }

    private suspend fun loadProjects() {
        _isLoading.value = true
        try {
            projectRepository.refreshProjects()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onProjectSelected(project: Project) {
        viewModelScope.launch {
            _currentProject.value = project
            saveCurrentProject(project)
        }
    }

    private suspend fun saveCurrentProject(project: Project) {
        sessionManager.saveCurrentProject(project)
    }

    fun refreshProjects() {
        viewModelScope.launch {
            loadProjects()
        }
    }
} 