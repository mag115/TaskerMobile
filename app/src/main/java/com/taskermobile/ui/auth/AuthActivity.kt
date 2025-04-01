package com.taskermobile.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.taskermobile.R
import com.taskermobile.TaskerApplication
import com.taskermobile.data.api.RetroFitClient
import com.taskermobile.data.repository.AuthRepository
import com.taskermobile.data.session.SessionManager
import com.taskermobile.MainActivity
import com.taskermobile.ui.auth.controllers.AuthController
import com.taskermobile.ui.auth.fragments.LoginFragment
import com.taskermobile.ui.auth.fragments.WelcomeFragment
import com.taskermobile.util.CryptographyManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.crypto.Cipher

class AuthActivity : AppCompatActivity() {
    lateinit var authController: AuthController
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var cryptographyManager: CryptographyManager
    private lateinit var taskerApp: TaskerApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        authRepository = AuthRepository(RetroFitClient.authApiService)
        authController = AuthController(authRepository, sessionManager)
        cryptographyManager = CryptographyManager()
        taskerApp = application as TaskerApplication

        showStandardLoginFlow()
    }

    private fun showStandardLoginFlow() {
        Log.d("AuthActivity", "Showing standard login flow (WelcomeFragment).")
        taskerApp.clearDecryptedToken()
        setContentView(R.layout.activity_auth)
        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_container, WelcomeFragment())
            .commitAllowingStateLoss()
    }

    fun navigateToMain() {
        Log.d("AuthActivity", "Navigating to MainActivity.")
        startActivity(Intent(this@AuthActivity, MainActivity::class.java))
        finish()
    }

    fun clearBiometricData() {
        Log.w("AuthActivity", "Clearing biometric key and stored encrypted token due to error.")
        cryptographyManager.deleteKey(LoginFragment.KEY_NAME)
        lifecycleScope.launch {
            sessionManager.clearEncryptedToken()
            sessionManager.setBiometricLoginEnabled(false)
        }
    }
} 