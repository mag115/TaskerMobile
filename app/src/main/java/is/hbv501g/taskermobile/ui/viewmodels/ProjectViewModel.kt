package `is`.hbv501g.taskermobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hbv501g.taskermobile.data.model.Project
import `is`.hbv501g.taskermobile.data.service.ProjectService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProjectViewModel(private val projectService: ProjectService) : ViewModel() {
    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject: StateFlow<Project?> = _currentProject.asStateFlow()

    init {
        loadAllProjects()
    }

    fun loadAllProjects() {
        viewModelScope.launch {
            try {
                val response = projectService.getAllProjects()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _projects.value = it
                    }
                }
            } catch (e: HttpException) {
                e.printStackTrace()
            }
        }
    }

    fun getProjectById(projectId: Long) {
        viewModelScope.launch {
            try {
                val response = projectService.getProjectById(projectId)
                if (response.isSuccessful) {
                    _currentProject.value = response.body()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createProject(project: Project) {
        viewModelScope.launch {
            try {
                val response = projectService.createProject(project)
                if (response.isSuccessful) {
                    _projects.value = _projects.value + response.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}