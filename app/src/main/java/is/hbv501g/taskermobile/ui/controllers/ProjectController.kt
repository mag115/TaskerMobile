package `is`.hbv501g.taskermobile.ui.controllers

import `is`.hbv501g.taskermobile.data.api.RetrofitClient
import `is`.hbv501g.taskermobile.data.model.Project
import `is`.hbv501g.taskermobile.data.repository.ProjectRepository
import `is`.hbv501g.taskermobile.data.service.ProjectService
import `is`.hbv501g.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProjectController(sessionManager: SessionManager) {
    private val projectService = RetrofitClient.createService<ProjectService>(sessionManager)
    private val projectRepository = ProjectRepository(projectService)

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects = _projects.asStateFlow()

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject = _currentProject.asStateFlow()

    suspend fun loadAllProjects() {
        projectRepository.getAllProjects().onSuccess { projects ->
            _projects.value = projects
        }
    }

    suspend fun getProjectById(projectId: Long): Result<Project> {
        return projectRepository.getProjectById(projectId)
    }

    suspend fun createProject(project: Project): Result<Project> {
        return projectRepository.createProject(project)
    }

    suspend fun loadCurrentProject() {
        projectRepository.getCurrentProject().onSuccess { project ->
            _currentProject.value = project
        }
    }

    suspend fun setCurrentProject(projectId: Long): Result<Unit> {
        val result = projectRepository.setCurrentProject(projectId)
        if (result.isSuccess) {
            loadCurrentProject()
        }
        return result
    }

    suspend fun addMemberToProject(projectId: Long, userId: Long): Result<Project> {
        return projectRepository.addMemberToProject(projectId, userId)
    }
} 