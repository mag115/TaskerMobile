package `is`.hbv501g.taskermobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hbv501g.taskermobile.data.model.Project
import `is`.hbv501g.taskermobile.data.service.ProjectServiceImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProjectViewModel(
    private val projectService: ProjectServiceImpl
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> get() = _projects

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject: StateFlow<Project?> get() = _currentProject

    fun loadAllProjects() {
        viewModelScope.launch {
            projectService.getAllProjects().onSuccess { projectList ->
                _projects.value = projectList
            }
        }
    }

    fun getProjectById(projectId: Long) {
        viewModelScope.launch {
            projectService.getProjectById(projectId).onSuccess { project ->
                _currentProject.value = project
            }
        }
    }

    fun createProject(project: Project) {
        viewModelScope.launch {
            projectService.createProject(project).onSuccess { newProject ->
                _projects.value = _projects.value + newProject
            }
        }
    }
}