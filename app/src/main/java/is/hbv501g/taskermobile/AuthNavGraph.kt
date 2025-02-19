package `is`.hbv501g.taskermobile

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.compose.rememberNavController
import `is`.hbv501g.taskermobile.data.api.RetrofitClient
import `is`.hbv501g.taskermobile.data.dataStore
import `is`.hbv501g.taskermobile.data.repository.AuthRepository
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.ui.screens.auth.HomeScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.LoginScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.SignupScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.WelcomeScreen

@Composable
fun AuthNavGraph(sessionManager: SessionManager) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Get the DataStore instance from our extension property
    val dataStore: DataStore<Preferences> = context.dataStore

    // Create the repository with the DataStore instance and API service
    val authRepository = AuthRepository(RetrofitClient.authApiService)
//    val sessionManager = SessionManager(context)


    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }
        composable("login") {
            // Pass repository to LoginScreen so that it can use a custom factory
            LoginScreen(navController = navController, repository = authRepository, sessionManager)
        }
        composable("signup") {
            SignupScreen(navController = navController)
        }
        composable("home") {
            // HomeScreen or other screen(s)
            HomeScreen(navController,  sessionManager)
        }
    }
}
