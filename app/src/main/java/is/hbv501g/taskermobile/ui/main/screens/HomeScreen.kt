package `is`.hbv501g.taskermobile.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.ui.shared.AppHeader
import `is`.hbv501g.taskermobile.ui.shared.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, sessionManager: SessionManager) {
    Scaffold(
        topBar = { AppHeader(title = "Tasker Home", navController = navController, backButton = false, sessionManager)},
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to your Task Manager!", style = MaterialTheme.typography.headlineSmall)
        }
    }
}
