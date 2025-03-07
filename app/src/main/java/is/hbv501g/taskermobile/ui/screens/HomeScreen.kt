package `is`.hbv501g.taskermobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import `is`.hbv501g.taskermobile.ui.shared.AppHeader
import `is`.hbv501g.taskermobile.ui.shared.BottomNavBar
import `is`.hbv501g.taskermobile.data.session.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, sessionManager: SessionManager) {
    // Collect the username from sessionManager
    val username by sessionManager.username.collectAsState(initial = "User")

    Scaffold(
        topBar = {
            AppHeader(
                title = "Tasker Home",
                navController = navController,
                backButton = false,
                sessionManager = sessionManager
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to your Task Manager, $username!",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}