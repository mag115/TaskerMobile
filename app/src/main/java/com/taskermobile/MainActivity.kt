package com.taskermobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import androidx.work.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.repository.AuthRepository
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.ActivityMainBinding
import com.taskermobile.ui.auth.controllers.AuthController
import com.taskermobile.ui.shared.ProjectSelectorView
import com.taskermobile.workers.NotificationWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sessionManager: SessionManager
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var authController: AuthController


    companion object {
        private const val REQUEST_CODE_NOTIFICATIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        setupToolbar()
        setupNavigation()
        requestNotificationPermission()
        setupProjectSelector()

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_project_report -> {
                    // Pop the back stack up to the graph's start destination,
                    // then navigate to the report list destination.
                    val navOptions = NavOptions.Builder()
                        // Clear everything so that we start fresh.
                        .setPopUpTo(navController.graph.startDestinationId, true)
                        .build()
                    navController.navigate(R.id.navigation_project_report, null, navOptions)
                    drawerLayout.closeDrawers()
                    true
                }
                else -> {
                    // For all other items, use the default behavior.
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                }
            }
        }
    }

    private fun setupProjectSelector() {
        // Get your ProjectSelectorView from the toolbar (it’s defined in your MaterialToolbar)
        val projectSelector = findViewById<ProjectSelectorView>(R.id.projectSelector)

        // Instantiate ProjectRepository
        val projectDao = (application as TaskerApplication).database.projectDao()
        val projectService = RetrofitClient.createService<com.taskermobile.data.service.ProjectService>(sessionManager)
        val projectRepository = com.taskermobile.data.repository.ProjectRepository(projectService, projectDao)

        // Now load the projects from the repository.
        lifecycleScope.launch {
            try {
                // Use .first() to get the current list from the Flow
                val projects = projectRepository.getLocalProjects().first()
                println("Loaded projects: ${projects.size}")
                if (projects.isNotEmpty()) {
                    projectSelector.setProjects(projects)
                    projectSelector.setCurrentProject(projects.first())
                } else {
                    // Optionally, you can call a refresh method to load from remote.
                    // For example: projectRepository.refreshProjects()
                    Toast.makeText(this@MainActivity, "No projects available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error loading projects: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Continuously collect updates from the local database
        lifecycleScope.launch {
            projectRepository.getLocalProjects().collect { projects ->
                if (projects.isNotEmpty()) {
                    projectSelector.setProjects(projects)
                    // Optionally, update the current project if none is set
                    if (projectSelector.getCurrentProject() == null) {
                        projectSelector.setCurrentProject(projects.first())
                    }
                } else {
                    Toast.makeText(this@MainActivity, "No projects available", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set the listener to update global state when a project is selected.
        projectSelector.setOnProjectSelectedListener { selectedProject ->
            lifecycleScope.launch {
                sessionManager.saveCurrentProject(selectedProject)
            }
            Toast.makeText(this, "Switched to project: ${selectedProject.name}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupDependencies() {
        sessionManager = SessionManager(this)

        // Initialize AuthController
        val authRepository = AuthRepository(RetrofitClient.authApiService)
        authController = AuthController(authRepository, sessionManager)
    }


    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)

        val logoutButton = findViewById<Button>(R.id.logoutButton) // ✅ Now correctly referenced
        val backButton = findViewById<ImageButton>(R.id.backButton)

        backButton?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        logoutButton?.setOnClickListener {
            lifecycleScope.launch {
                sessionManager.clearSession()
                authController.logout(this@MainActivity) // ✅ Ensure logout works
                Toast.makeText(this@MainActivity, "Logged Out", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    private fun setupNavigation() {
        drawerLayout = binding.drawerLayout

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_my_tasks,
                R.id.navigation_all_tasks,
                R.id.navigation_notifications,
                R.id.navigation_projects,
                R.id.navigation_project_report
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)
        binding.navigationView.setupWithNavController(navController)

        val toolbar = binding.appBarLayout.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        lifecycleScope.launch {
            val role = sessionManager.role.first() ?: "TEAM_MEMBER"
            updateBottomNavigationMenu(role)
        }
    }

    private fun updateBottomNavigationMenu(role: String?) {
        val menu = binding.bottomNavigation.menu

        when (role) {
            "PROJECT_MANAGER", "TEAM_MEMBER" -> {
                menu.findItem(R.id.navigation_my_tasks).isVisible = true
                menu.findItem(R.id.navigation_all_tasks).isVisible = true
                menu.findItem(R.id.navigation_notifications).isVisible = true
                menu.findItem(R.id.navigation_projects).isVisible = true
            }
            else -> {
                menu.findItem(R.id.navigation_my_tasks).isVisible = false
                menu.findItem(R.id.navigation_all_tasks).isVisible = false
                menu.findItem(R.id.navigation_notifications).isVisible = false
                menu.findItem(R.id.navigation_projects).isVisible = false
            }
        }
    }

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
                setupNotificationWorker()
            }
        } else {
            setupNotificationWorker()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupNotificationWorker()
            } else {
                Toast.makeText(this, "Notifications disabled. Enable them in settings.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
