package `is`.hbv501g.taskermobile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import `is`.hbv501g.taskermobile.ui.screens.auth.HomeScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.LoginScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.SignupScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.WelcomeScreen
import `is`.hbv501g.taskermobile.ui.viewmodels.AuthViewModel

@Composable
fun AuthNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val sessionManager = authViewModel.getSessionManager()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(navController, sessionManager)
        }
        composable(Routes.LOGIN) {
            LoginScreen(navController, authViewModel)
        }
        composable(Routes.SIGNUP) {
            SignupScreen(navController, authViewModel)
        }
        composable(Routes.HOME) {
            HomeScreen(navController, sessionManager)
        }
    }
}