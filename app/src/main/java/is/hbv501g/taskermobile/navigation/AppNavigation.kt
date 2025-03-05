package `is`.hbv501g.taskermobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import `is`.hbv501g.taskermobile.data.api.RetrofitClient
import `is`.hbv501g.taskermobile.data.repository.AuthRepository
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.ui.Routes
import `is`.hbv501g.taskermobile.ui.controllers.ProjectController
import `is`.hbv501g.taskermobile.ui.controllers.UserController
import `is`.hbv501g.taskermobile.ui.main.screens.CreateTask
import `is`.hbv501g.taskermobile.ui.main.screens.UserScreen
import `is`.hbv501g.taskermobile.ui.screens.ProjectsScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.HomeScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.LoginScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.SignupScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.WelcomeScreen
import `is`.hbv501g.taskermobile.ui.shared.BottomNavBar

@Composable
fun AppNavigation(sessionManager: SessionManager) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authRepository = AuthRepository(RetrofitClient.authApiService)
    val isAuthenticated by sessionManager.authState.collectAsState(initial = false)
    val projectController = remember { ProjectController(sessionManager) }
    val userController = remember { UserController(sessionManager) }

    val bottomNavScreens = listOf(Routes.HOME, Routes.CREATE_TASKS, Routes.PROJECTS, "settings")

    Scaffold(
       
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated) Routes.HOME else Routes.WELCOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Auth screens
            composable(Routes.WELCOME) {
                WelcomeScreen(navController)
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    navController = navController,
                    repository = authRepository,
                    sessionManager = sessionManager
                )
            }
            composable(Routes.SIGNUP) {
                SignupScreen(navController)
            }

            // Main app screens
            composable(Routes.HOME) {
                HomeScreen(navController, sessionManager)
            }
            composable(Routes.CREATE_TASKS) {
                CreateTask(navController, sessionManager)
            }
            composable(Routes.PROJECTS) {
                ProjectsScreen(
                    projectController = projectController,
                    userController = userController,
                    navController = navController,
                    sessionManager = sessionManager
                )
            }
            composable("settings") {
                UserScreen(navController, sessionManager)
            }
        }
    }
} 