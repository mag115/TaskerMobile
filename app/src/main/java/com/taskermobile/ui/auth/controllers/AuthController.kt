package com.taskermobile.ui.auth.controllers

import android.util.Log
import com.taskermobile.data.model.SignupRequest
import com.taskermobile.data.repository.AuthRepository
import com.taskermobile.data.session.SessionManager
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
                onSuccess = { loginResponse ->
                    Log.d("AuthController", "Login successful: $loginResponse")
                    sessionManager.saveLoginDetails(
                        token = loginResponse.token,
                        expiresIn = loginResponse.expiresIn,
                        userId = loginResponse.userId,
                        username = loginResponse.username,
                        role = loginResponse.role  // ✅ Ensure role is saved
                    )
                    withContext(Dispatchers.Main) {
                        onResult(true, null)
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

    fun signup(
        username: String,
        email: String,
        password: String,
        role: String, // ✅ Include role
        onResult: (Boolean, String?) -> Unit
    ) {
        coroutineScope.launch {
            val signupRequest = SignupRequest(username, email, password, role) // ✅ Wrap parameters in object
            val result = repository.signup(signupRequest) // ✅ Pass as single object

            result.fold(
                onSuccess = { signupResponse ->
                    Log.d("AuthController", "Signup successful: ${signupResponse.username}")

                    sessionManager.saveLoginDetails(
                        token = signupResponse.token,
                        expiresIn = signupResponse.expiresIn,
                        userId = signupResponse.userId,
                        username = signupResponse.username,
                        role = signupResponse.role
                    )

                    withContext(Dispatchers.Main) {
                        onResult(true, null)
                    }
                },
                onFailure = { error ->
                    Log.e("AuthController", "Signup failed: ${error.message}", error)
                    withContext(Dispatchers.Main) {
                        onResult(false, error.message ?: "Signup failed")
                    }
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
