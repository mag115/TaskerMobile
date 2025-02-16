package `is`.hbv501g.taskermobile.controller

import android.util.Log
import `is`.hbv501g.taskermobile.data.repository.AuthRepository
import `is`.hbv501g.taskermobile.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthController(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Handle user login
     */
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        coroutineScope.launch {
            val result = repository.login(email, password)
            result.fold(
                onSuccess = { token ->
                    Log.d("AuthController", "Login successful: $token")
                    sessionManager.saveAuthToken(token) // Save token
                    withContext(Dispatchers.Main) {
                        onResult(true, null) // Switch back to UI thread
                    }
                },
                onFailure = { error ->
                    Log.e("AuthController", "Login failed: ${error.message}", error)
                    withContext(Dispatchers.Main) {
                        onResult(false, error.message ?: "Unknown error")
                    }
                }
            )
        }
    }

    /**
     * Handle user signup
     */
    fun signup(username: String, email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        coroutineScope.launch {
            val result = repository.signup(username, email, password)
            result.fold(
                onSuccess = { signupResponse ->
                    Log.d("AuthController", "Signup successful: ${signupResponse.message}")
                    if (signupResponse.success) {
                        // Optionally, if your signup response contains a token (here represented by userId), save it.
                        signupResponse.userId?.let { token ->
                            sessionManager.saveAuthToken(token)
                        }
                        sessionManager.saveUsername(username)
                        onResult(true, null)
                    } else {
                        onResult(false, signupResponse.message)
                    }
                },
                onFailure = { error ->
                    Log.e("AuthController", "Signup failed: ${error.message}", error)
                    onResult(false, error.message ?: "Signup failed")
                }
            )
        }
    }

    /**
     * Handle logout
     */
    fun logout() {
        coroutineScope.launch {
            sessionManager.clearSession()
            Log.d("AuthController", "User logged out")
        }
    }
}
