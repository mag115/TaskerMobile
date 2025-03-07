package com.taskermobile.ui.main.controllers

import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.model.Project
import com.taskermobile.data.repository.ProjectRepository
import com.taskermobile.data.service.ProjectService
import com.taskermobile.data.session.SessionManager
import com.taskermobile.data.local.dao.ProjectDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProjectController(
    sessionManager: SessionManager,
    private val projectDao: ProjectDao
) {
    private val projectService = RetrofitClient.createService<ProjectService>(sessionManager)
    private val projectRepository = ProjectRepository(projectService, projectDao)
    private val _projects = MutableStateFlow<ProjectsState>(ProjectsState.Loading)
    val projects: StateFlow<ProjectsState> = _projects
    
    private val controllerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        // Observe local database changes
        controllerScope.launch {
            projectRepository.getLocalProjects()
                .collect { projects ->
                    _projects.emit(ProjectsState.Success(projects))
                }
        }
    }

    suspend fun fetchAllProjects() {
        _projects.emit(ProjectsState.Loading)
        try {
            val result = projectRepository.refreshProjects()
            if (!result.isSuccess) {
                result.exceptionOrNull()?.let { exception ->
                    _projects.emit(ProjectsState.Error(exception))
                }
            }
        } catch (e: Exception) {
            _projects.emit(ProjectsState.Error(e))
        }
    }

    suspend fun createProject(project: Project): Result<Project> {
        return projectRepository.createProject(project)
    }
}

sealed class ProjectsState {
    data object Loading : ProjectsState()
    data class Success(val projects: List<Project>) : ProjectsState()
    data class Error(val exception: Throwable) : ProjectsState()
} 