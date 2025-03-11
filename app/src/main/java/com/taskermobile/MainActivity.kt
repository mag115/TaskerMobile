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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import androidx.work.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.ActivityMainBinding
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

    companion object {
        private const val REQUEST_CODE_NOTIFICATIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDependencies()
        setupToolbar()  // ✅ Added setupToolbar() function
        setupNavigation()
        requestNotificationPermission()
    }

    private fun setupDependencies() {
        sessionManager = SessionManager(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar) // ✅ Ensures it's used as ActionBar

        // ✅ Get the buttons from the correct layout (NOT directly from toolbar)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        backButton?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        logoutButton?.setOnClickListener {
            lifecycleScope.launch {
                sessionManager.clearSession()
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
                R.id.navigation_projects
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
