package `is`.hbv501g.taskermobile.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import `is`.hbv501g.taskermobile.data.api.AuthApiService
import `is`.hbv501g.taskermobile.data.api.RetrofitClient
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.navigation.Routes
import `is`.hbv501g.taskermobile.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    navController: NavController,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(RetrofitClient.createService(AuthApiService::class.java, sessionManager), sessionManager)
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Tasker!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { navController.navigate(Routes.SIGNUP) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Account")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate(Routes.LOGIN) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Already have an account? Login")
        }
    }

    // Auto-navigate to Home if already logged in
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            authViewModel.loginState.collectLatest { state ->
                state?.onSuccess {
                    navController.navigate(Routes.HOME) {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            }
        }
    }
}