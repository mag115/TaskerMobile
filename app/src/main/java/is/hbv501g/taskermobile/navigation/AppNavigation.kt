package `is`.hbv501g.taskermobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.ui.Routes
import `is`.hbv501g.taskermobile.ui.screens.CreateTask
import `is`.hbv501g.taskermobile.ui.screens.UserScreen
import `is`.hbv501g.taskermobile.ui.screens.ProjectsScreen
import `is`.hbv501g.taskermobile.ui.screens.HomeScreen
import `is`.hbv501g.taskermobile.ui.screens.LoginScreen
import `is`.hbv501g.taskermobile.ui.screens.SignupScreen
import `is`.hbv501g.taskermobile.ui.screens.WelcomeScreen
import `is`.hbv501g.taskermobile.ui.viewmodels.AuthViewModel
import `is`.hbv501g.taskermobile.ui.viewmodels.ProjectViewModel
import `is`.hbv501g.taskermobile.ui.viewmodels.TaskViewModel
import `is`.hbv501g.taskermobile.ui.viewmodels.UserViewModel

@Composable
fun AppNavigation(
    sessionManager: SessionManager,
    authViewModel: AuthViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel(),
    projectViewModel: ProjectViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val navController = rememberNavController()
    // Observe authentication state via SessionManager (or via AuthViewModel if you prefer)
    val isAuthenticated by sessionManager.authState.collectAsState(initial = false)

    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated) Routes.HOME else Routes.WELCOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Auth screens
            composable(Routes.WELCOME) {
                WelcomeScreen(navController, sessionManager)
            }
            composable(Routes.LOGIN) {
                LoginScreen(navController, authViewModel)
            }
            composable(Routes.SIGNUP) {
                SignupScreen(navController, authViewModel)
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
                    navController = navController,
                    projectViewModel = projectViewModel,
                    sessionManager = sessionManager
                )
            }
            composable("settings") {
                UserScreen(navController, sessionManager)
            }
        }
    }
}