package `is`.hbv501g.taskermobile.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import `is`.hbv501g.taskermobile.data.api.AuthApiService
import `is`.hbv501g.taskermobile.data.api.RetrofitClient
import `is`.hbv501g.taskermobile.data.session.SessionManager
import `is`.hbv501g.taskermobile.navigation.Routes
import `is`.hbv501g.taskermobile.ui.shared.AppHeader
import `is`.hbv501g.taskermobile.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.flow.collectLatest


@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val loginState by authViewModel.loginState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppHeader(navController = navController, sessionManager = authViewModel.sessionManager) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    loading = true
                    authViewModel.login(email, password)
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Login")
                }
            }

            if (!errorMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // Observe Login State and Navigate on Success
    LaunchedEffect(loginState) {
        loginState?.let {
            it.fold(
                onSuccess = {
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onFailure = { error ->
                    loading = false
                    errorMessage = error.message ?: "Login failed"
                }
            )
        }
    }
}