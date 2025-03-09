package com.taskermobile.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.taskermobile.R
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.repository.AuthRepository
import com.taskermobile.data.session.SessionManager
import com.taskermobile.MainActivity
import com.taskermobile.ui.auth.controllers.AuthController
import com.taskermobile.ui.auth.fragments.WelcomeFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    lateinit var authController: AuthController
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize dependencies
        sessionManager = SessionManager(this)
        authRepository = AuthRepository(RetrofitClient.authApiService)
        authController = AuthController(authRepository, sessionManager)

        // Check if user is already logged in with valid token
        lifecycleScope.launch {
            val isTokenValid = sessionManager.authState.first()
            if (isTokenValid) {
                // Valid token exists, redirect to MainActivity
                startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                finish()
                return@launch
            }

            // If no valid token, continue with auth flow
            setContentView(R.layout.activity_auth)

            // Set up the default fragment (Welcome)
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.auth_container, WelcomeFragment())
                    .commit()
            }
        }
    }
} 