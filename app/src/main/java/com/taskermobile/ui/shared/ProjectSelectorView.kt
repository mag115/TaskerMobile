package com.taskermobile.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import com.taskermobile.data.model.Project
import com.taskermobile.databinding.LayoutProjectSelectorBinding

class ProjectSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutProjectSelectorBinding
    private var onProjectSelected: ((Project) -> Unit)? = null
    private var currentProject: Project? = null
    private var projects: List<Project> = emptyList()

    init {
        binding = LayoutProjectSelectorBinding.inflate(LayoutInflater.from(context), this)
        setupClickListener()
        // Ensure the button is clickable
        binding.projectSelectorButton.isClickable = true
        binding.projectSelectorButton.isFocusable = true
    }

    private fun setupClickListener() {
        binding.projectSelectorButton.setOnClickListener { view ->
            if (projects.isEmpty()) return@setOnClickListener
            
            showProjectsPopup(view)
        }
    }

    private fun showProjectsPopup(anchorView: View) {
        PopupMenu(context, anchorView).apply {
            projects.forEach { project ->
                menu.add(project.name).apply {
                    isEnabled = project.id != currentProject?.id
                }
            }
            
            setOnMenuItemClickListener { menuItem ->
                val selectedProject = projects.find { it.name == menuItem.title }
                selectedProject?.let {
                    setCurrentProject(it)
                    onProjectSelected?.invoke(it)
                }
                true
            }
            
            show()
        }
    }

    fun setProjects(newProjects: List<Project>) {
        projects = newProjects
    }

    fun setCurrentProject(project: Project) {
        currentProject = project
        binding.projectSelectorButton.text = project.name
    }

    fun setOnProjectSelectedListener(listener: (Project) -> Unit) {
        onProjectSelected = listener
    }

    fun showLoading(show: Boolean) {
        binding.loadingIndicator.visibility = if (show) View.VISIBLE else View.GONE
        binding.projectSelectorButton.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }


} 