package `is`.hbv501g.taskermobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import `is`.hbv501g.taskermobile.data.api.RetrofitClient
import `is`.hbv501g.taskermobile.data.dataStore
import `is`.hbv501g.taskermobile.data.repository.AuthRepository
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.ui.Routes
import `is`.hbv501g.taskermobile.ui.controllers.TaskController
import `is`.hbv501g.taskermobile.ui.main.screens.CreateTask
import `is`.hbv501g.taskermobile.ui.main.screens.UserScreen
import `is`.hbv501g.taskermobile.ui.screens.auth.HomeScreen
import `is`.hbv501g.taskermobile.ui.shared.BottomNavBar

@Composable
fun MainNavGraph(sessionManager: SessionManager) {
    val navController: NavHostController = rememberNavController()

    val context = LocalContext.current
    val dataStore: DataStore<Preferences> = context.dataStore
//    val authRepository = AuthRepository(dataStore, RetrofitClient.authApiService)
    val sessionManager = SessionManager(context)

    val bottomNavScreens = listOf("home", "tasks", "settings")

    Scaffold(
        bottomBar = {
            if (bottomNavScreens.contains(navController.currentDestination?.route)) {
                BottomNavBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home"){
                HomeScreen(navController, sessionManager)
            }
            composable("create-task") {
                CreateTask(navController, sessionManager)
            }
            composable("settings") {
                 UserScreen(navController, sessionManager)
            }
        }
    }
}
