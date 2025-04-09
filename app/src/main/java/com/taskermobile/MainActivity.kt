package com.taskermobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import androidx.work.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.taskermobile.data.api.RetroFitClient
import com.taskermobile.data.repository.AuthRepository
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.ActivityMainBinding
import com.taskermobile.navigation.NavigationManager
import com.taskermobile.ui.auth.controllers.AuthController
import com.taskermobile.ui.main.controllers.MainViewModel
import com.taskermobile.ui.shared.ProjectSelectorView
import com.taskermobile.workers.NotificationWorker
import com.taskermobile.workers.UnreadNotificationWorker
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
    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val REQUEST_CODE_NOTIFICATIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        setupToolbar()
        setupNavigation()
        Log.d("MainActivity", "Calling requestNotificationPermission")
        requestNotificationPermission()
        setupProjectSelector()

        // Update title when destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val titleTextView = findViewById<TextView>(R.id.toolbarTitle)
            titleTextView.text = destination.label
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_project_report -> {
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(navController.graph.startDestinationId, true)
                        .build()
                    navController.navigate(R.id.navigation_project_report, null, navOptions)
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.navigation_logout -> {
                    lifecycleScope.launch {
                        sessionManager.clearSession()
                        NavigationManager.navigateToAuth(this@MainActivity)
                    }
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                }
            }
        }
    }

    private fun setupProjectSelector() {
        // Get ProjectSelectorView from the toolbar
        val projectSelector = findViewById<ProjectSelectorView>(R.id.projectSelector)

        // Set up lifecycle-aware collection of projects from the ViewModel
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe the projects list
                viewModel.projects.collect { projects ->
                    if (projects.isNotEmpty()) {
                        projectSelector.setProjects(projects)
                    }
                }
            }
        }

        // Observe the current selected project
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentProject.collect { project ->
                    project?.let {
                        projectSelector.setCurrentProject(it)
                    }
                }
            }
        }

        // Observe loading state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    projectSelector.showLoading(isLoading)
                }
            }
        }

        // Set the listener to update when a project is selected
        projectSelector.setOnProjectSelectedListener { selectedProject ->
            viewModel.onProjectSelected(selectedProject)
            Toast.makeText(this, "Switched to project: ${selectedProject.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(this)

        // Initialize AuthController
        val authRepository = AuthRepository(RetroFitClient.authApiService)
        authController = AuthController(authRepository, sessionManager)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Hide the default title

        val titleTextView = findViewById<TextView>(R.id.toolbarTitle)

        // Set the initial title text
        titleTextView.text = getString(R.string.app_name)
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

        // Setup bottom navigation with NavController
        binding.bottomNavigation.setupWithNavController(navController)
        binding.navigationView.setupWithNavController(navController)

        // Setup ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Setup role-based navigation
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
        Log.d("MainActivity", "Setting up notification workers")
        try {
            // Set up the standard notification worker
            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "NotificationWorker",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
            Log.d("MainActivity", "Standard notification worker started")

            // Start unread notification worker
            UnreadNotificationWorker.startPolling(this)
            Log.d("MainActivity", "Unread notification polling started")
            
            // Do an immediate check for unread notifications
            UnreadNotificationWorker.checkForUnreadNotifications(this)
            Log.d("MainActivity", "Triggered immediate notification check")
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up notification workers", e)
        }
    }

    private fun requestNotificationPermission() {
        Log.d("MainActivity", "requestNotificationPermission called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("MainActivity", "Android version >= 13, checking permission")
            val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            Log.d("MainActivity", "Has notification permission: $hasPermission")
            
            if (!hasPermission) {
                Log.d("MainActivity", "Requesting notification permission from user")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATIONS
                )
            } else {
                Log.d("MainActivity", "Notification permission already granted, setting up worker")
                setupNotificationWorker()
            }
        } else {
            Log.d("MainActivity", "Android version < 13, no permission needed, setting up worker")
            setupNotificationWorker()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("MainActivity", "onRequestPermissionsResult called with requestCode: $requestCode")
        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            Log.d("MainActivity", "Notification permission granted: $granted")
            if (granted) {
                Log.d("MainActivity", "User granted notification permission, setting up worker")
                setupNotificationWorker()
            } else {
                Log.d("MainActivity", "User denied notification permission")
                Toast.makeText(this, "Notifications disabled. Enable them in settings.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop unread notification polling when the app is destroyed
        UnreadNotificationWorker.stopPolling(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
        
        // Check if notification permission is granted
        val permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not needed on earlier Android versions
        }
        
        // Restart workers if we have permission
        if (permissionGranted) {
            Log.d("MainActivity", "Notification permission granted, starting workers")
            setupNotificationWorker()
        }
    }
}
