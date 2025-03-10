package com.taskermobile

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.taskermobile.data.model.Project
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.ActivityMainBinding
import com.taskermobile.ui.main.controllers.ProjectController
import com.taskermobile.ui.main.controllers.ProjectsState
import com.taskermobile.workers.NotificationWorker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import android.Manifest
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sessionManager: SessionManager
    private lateinit var projectController: ProjectController

    companion object {
        private const val REQUEST_CODE_NOTIFICATIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        setupNavigation()
        setupProjectSelector()
        requestNotificationPermission() // ✅ Ensure permission is requested at startup
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
                    }
                }
            }
        }

        binding.projectSelector.setOnProjectSelectedListener { project ->
            lifecycleScope.launch {
                project.id?.let { sessionManager.saveCurrentProjectId(it) }
            }
        }

        lifecycleScope.launch {
            projectController.fetchAllProjects()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // ✅ Set up WorkManager for background notification polling
    private fun setupNotificationWorker() {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "NotificationWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    // ✅ Request Notification Permission for Android 13+
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATIONS
                )
            } else {
                // ✅ Permission already granted, start NotificationWorker
                setupNotificationWorker()
            }
        } else {
            // ✅ Not Android 13+, start NotificationWorker without permission
            setupNotificationWorker()
        }
    }

    // ✅ Handle permission result for notifications
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupNotificationWorker() // ✅ Start worker if granted
            } else {
                Toast.makeText(this, "Notifications are disabled. You can enable them in settings.", Toast.LENGTH_LONG).show()
            }
        }
    }
}