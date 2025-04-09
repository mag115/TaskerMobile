package com.taskermobile.ui.auth.controllers

import android.content.Context
import android.content.Intent
import android.util.Log
import android.app.Application
import com.taskermobile.TaskerApplication
import com.taskermobile.data.local.TaskerDatabase
import com.taskermobile.data.local.entity.UserEntity
import com.taskermobile.data.model.LoginResponse
import com.taskermobile.data.model.SignupRequest
import com.taskermobile.data.model.User
import com.taskermobile.data.repository.AuthRepository
import com.taskermobile.data.session.SessionManager
import com.taskermobile.ui.auth.AuthActivity
import com.taskermobile.ui.auth.fragments.LoginFragment
import com.taskermobile.util.CryptographyManager
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
    fun login(username: String, password: String, context: Context, onResult: (response: LoginResponse?, error: String?) -> Unit) {
        coroutineScope.launch {
            val result = repository.login(username, password)
            result.fold(
                onSuccess = { loginResponse ->
                    Log.d("AuthController", "Login successful for user: ${loginResponse.username}")
                    sessionManager.saveLoginDetails(
                        expiresIn = loginResponse.expiresIn,
                        userId = loginResponse.userId,
                        username = loginResponse.username,
                        role = loginResponse.role
                    )
                    
                    // Ensure user exists in Room database
                    ensureUserExistsInDatabase(loginResponse, context)
                    
                    withContext(Dispatchers.Main) {
                        onResult(loginResponse, null)
                    }
                },
                onFailure = { error ->
                    Log.e("AuthController", "Login failed: ${error.message}", error)
                    withContext(Dispatchers.Main) {
                        onResult(null, error.message ?: "Unknown error")
                    }
                }
            )
        }
    }

    private suspend fun ensureUserExistsInDatabase(loginResponse: LoginResponse, context: Context) {
        try {
            val database = TaskerDatabase.getDatabase(context)
            val userDao = database.userDao()
            
            // Check if user already exists in database
            val existingUser = userDao.getUserById(loginResponse.userId)
            
            if (existingUser == null) {
                // Create new user entity
                Log.d("AuthController", "Creating new user entity in database for user: ${loginResponse.username}")
                val userEntity = UserEntity(
                    id = loginResponse.userId,
                    username = loginResponse.username,
                    email = null, // Not provided in login response
                    role = loginResponse.role,
                    isSynced = true,
                    imageUri = null // Initially no profile image
                )
                userDao.insertUser(userEntity)
            } else {
                Log.d("AuthController", "User already exists in database: ${existingUser.username}")
            }
        } catch (e: Exception) {
            Log.e("AuthController", "Error ensuring user exists in database", e)
        }
    }

    /**
     * Handle user signup
     */
    fun signup(
        username: String,
        email: String,
        password: String,
        role: String,
        context: Context,
        onResult: (response: LoginResponse?, error: String?) -> Unit
    ) {
        coroutineScope.launch {
            val signupRequest = SignupRequest(username, email, password, role)
            val result = repository.signup(signupRequest)

            result.fold(
                onSuccess = { signupResponse ->
                    Log.d("AuthController", "Signup successful: ${signupResponse.username}")
                    sessionManager.saveLoginDetails(
                        expiresIn = signupResponse.expiresIn,
                        userId = signupResponse.userId,
                        username = signupResponse.username,
                        role = signupResponse.role
                    )
                    
                    // Ensure user exists in Room database
                    ensureUserExistsInDatabase(signupResponse, context)

                    withContext(Dispatchers.Main) {
                        onResult(signupResponse, null)
                    }
                },
                onFailure = { error ->
                    Log.e("AuthController", "Signup failed: ${error.message}", error)
                    withContext(Dispatchers.Main) {
                        onResult(null, error.message ?: "Signup failed")
                    }
                }
            )
        }
    }

    /**
     * Handle logout
     */
    fun logout(app: Application) {
        coroutineScope.launch {
            val isBiometricEnabled = sessionManager.isBiometricLoginEnabled()
            val encTokenPair = sessionManager.getEncryptedTokenAndIv()
    
            Log.d("AuthController", "Logout - Before clearing session: biometric enabled=$isBiometricEnabled, token=${encTokenPair.first != null}, iv=${encTokenPair.second != null}")
            
            sessionManager.clearSession()

            val isBiometricEnabledAfter = sessionManager.isBiometricLoginEnabled()
            val encTokenPairAfter = sessionManager.getEncryptedTokenAndIv()
            Log.d("AuthController", "Logout - After clearing session: biometric enabled=$isBiometricEnabledAfter, token=${encTokenPairAfter.first != null}, iv=${encTokenPairAfter.second != null}")

            Log.d("AuthController", "User logged out, session and tokens cleared.")

            withContext(Dispatchers.Main) {
                val intent = Intent(app, AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                app.startActivity(intent)
            }
        }
    }
}

