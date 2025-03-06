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
import `is`.hbv501g.taskermobile.ui.screens.ProjectsScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.HomeScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.WelcomeScreen
import `is`.hbv501g.taskermobile.ui.viewmodels.AuthViewModel
import `is`.hbv501g.taskermobile.ui.viewmodels.TaskViewModel
import `is`.hbv501g.taskermobile.ui.viewmodels.ProjectViewModel

@Composable
fun AppNavigation(
    sessionManager: SessionManager,
    authViewModel: AuthViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel(),
    projectViewModel: ProjectViewModel = viewModel()
) {
    val navController = rememberNavController()
    val isAuthenticated by sessionManager.authState.collectAsState(initial = false)

    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated) Routes.HOME else Routes.WELCOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(navController, sessionManager)
            }
            composable(Routes.HOME) {
                HomeScreen(navController, sessionManager)
            }
            composable(Routes.PROJECTS) {
                ProjectsScreen(navController, projectViewModel)
            }
        }
    }
}
