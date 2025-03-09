package com.taskermobile

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.ActivityMainBinding
import com.taskermobile.ui.main.controllers.ProjectController
import com.taskermobile.ui.main.controllers.ProjectsState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sessionManager: SessionManager
    private lateinit var projectController: ProjectController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        setupNavigation()
        setupProjectSelector()


        // Subscribe to a Firebase topic
        FirebaseMessaging.getInstance().subscribeToTopic("tasker_notifications")
            .addOnCompleteListener { task: Task<Void?> ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Subscription to Tasker notifications failed")
                } else {
                    Log.d("FCM", "Successfully subscribed to Tasker notifications")
                }
            }
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(this)
        val projectDao = (application as TaskerApplication).database.projectDao()
        projectController = ProjectController(sessionManager, projectDao)
    }

    private fun setupNavigation() {
        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_tasks,
                R.id.navigation_projects,
                R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun setupProjectSelector() {
        lifecycleScope.launch {
            projectController.projects.collectLatest { state ->
                when (state) {
                    is ProjectsState.Loading -> {
                        binding.projectSelector.showLoading(true)
                    }
                    is ProjectsState.Success -> {
                        binding.projectSelector.showLoading(false)
                        binding.projectSelector.setProjects(state.projects)
                        
                        // Set current project
                        sessionManager.currentProjectId.collectLatest { currentProjectId ->
                            val currentProject = currentProjectId?.let { id ->
                                state.projects.find { it.id == id }
                            } ?: state.projects.firstOrNull()
                            
                            currentProject?.let {
                                binding.projectSelector.setCurrentProject(it)
                            }
                        }
                    }
                    is ProjectsState.Error -> {
                        binding.projectSelector.showLoading(false)
                        // Handle error state
                    }
                }
            }
        }

        binding.projectSelector.setOnProjectSelectedListener { project ->
            lifecycleScope.launch {
                project.id?.let { sessionManager.saveCurrentProjectId(it) }
            }
        }

        // Initial load of projects
        lifecycleScope.launch {
            projectController.fetchAllProjects()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
} 