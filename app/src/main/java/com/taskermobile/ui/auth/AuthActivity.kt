package com.taskermobile.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        sessionManager = SessionManager(this)
        authRepository = AuthRepository(RetrofitClient.authApiService)
        authController = AuthController(authRepository, sessionManager)

        lifecycleScope.launch {
            val isTokenValid = sessionManager.authState.first()
            Log.d("AuthActivity", "Checking auth state: $isTokenValid") // 🔥 Debugging log

            if (isTokenValid) {
                Log.d("AuthActivity", "Token valid, redirecting to MainActivity")
                startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                finish()
            } else {
                setContentView(R.layout.activity_auth)
                if (savedInstanceState == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.auth_container, WelcomeFragment())
                        .commit()
                }
            }
        }
    }

} 