package `is`.hbv501g.taskermobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.ui.shared.AppHeader
import `is`.hbv501g.taskermobile.ui.shared.BottomNavBar
import `is`.hbv501g.taskermobile.ui.shared.UserItem
import `is`.hbv501g.taskermobile.ui.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    navController: NavController,
    sessionManager: SessionManager,
    userViewModel: UserViewModel = viewModel()
) {
    // Observe the list of users from the viewmodel
    val users by userViewModel.users.collectAsState()

    // Trigger fetching users when this screen is first composed
    LaunchedEffect(Unit) {
        userViewModel.fetchAllUsers()
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "User Management",
                navController = navController,
                backButton = false,
                sessionManager = sessionManager
            )
        },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(users) { user ->
                    UserItem(user)
                }
            }
        }
    }
}
