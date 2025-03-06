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
fun SignupScreen(navController: NavController, sessionManager: SessionManager) {
    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(RetrofitClient.createService(AuthApiService::class.java, sessionManager), sessionManager)
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = { AppHeader(navController = navController, sessionManager = sessionManager) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Sign Up", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default
            )
            Spacer(modifier = Modifier.height(10.dp))
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
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        errorMessage = "Passwords don't match"
                        return@Button
                    }
                    loading = true
                    authViewModel.signup(username, email, password)
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Account")
                }
            }
            TextButton(onClick = { navController.navigate("login") }) {
                Text("Already have an account? Login")
            }
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // Observe Signup State using `collectLatest()`
    LaunchedEffect(authViewModel.loginState) {
        authViewModel.loginState.collectLatest { state ->
            state?.let {
                it.fold(
                    onSuccess = {
                        Toast.makeText(context, "Signup Successful!", Toast.LENGTH_SHORT).show()
                        navController.navigate(Routes.HOME) {
                            popUpTo("signup") { inclusive = true }
                        }
                    },
                    onFailure = { error ->
                        errorMessage = error.message ?: "Signup failed"
                    }
                )
            }
        }
    }
}