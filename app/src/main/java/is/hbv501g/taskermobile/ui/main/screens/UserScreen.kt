package `is`.hbv501g.taskermobile.ui.main.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.ui.controllers.UserController
import `is`.hbv501g.taskermobile.ui.shared.AppHeader
import `is`.hbv501g.taskermobile.ui.shared.BottomNavBar
import `is`.hbv501g.taskermobile.ui.shared.UserItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(navController: NavController, sessionManager: SessionManager) {
    val userController = remember { UserController(sessionManager) }
    val users by userController.users.collectAsState()

    LaunchedEffect(Unit) {
        userController.fetchAllUsers()
    }

    Scaffold(
        topBar = { AppHeader(title = "Tasker Home", navController = navController, backButton = false, sessionManager) },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        if (users.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(users) { user ->
                    UserItem(user)
                }
            }
        }
    }
}
