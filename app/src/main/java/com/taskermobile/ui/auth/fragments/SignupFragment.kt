package com.taskermobile.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.taskermobile.R
import com.taskermobile.databinding.FragmentSignupBinding
import com.taskermobile.MainActivity
import com.taskermobile.ui.auth.AuthActivity
import com.taskermobile.ui.auth.controllers.AuthController
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding
    private lateinit var authController: AuthController
    private var loading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authController = (activity as AuthActivity).authController

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        binding.apply {
            usernameInput.addTextChangedListener { validateInputs() }
            emailInput.addTextChangedListener { validateInputs() }
            passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
            confirmPasswordInput.transformationMethod = PasswordTransformationMethod.getInstance()
        }
    }

    private fun setupListeners() {
        binding.signupButton.setOnClickListener {
            if (loading) return@setOnClickListener

            val username = binding.usernameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()

            if (!validatePasswords(password, confirmPassword)) {
                showError("Passwords don't match")
                return@setOnClickListener
            }

            loading = true
            updateLoadingState()

            lifecycleScope.launch {
                authController.signup(username, email, password) { success, message ->
                    loading = false
                    activity?.runOnUiThread {
                        updateLoadingState()
                        if (success) {
                            Toast.makeText(context, "Signup Successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(activity, MainActivity::class.java))
                            activity?.finish()
                        } else {
                            showError(message ?: "Signup failed")
                        }
                    }
                }
            }
        }

        binding.loginLink.setOnClickListener {
            // Navigate to login fragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun validateInputs(): Boolean {
        val username = binding.usernameInput.text.toString()
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        return username.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank()
    }

    private fun validatePasswords(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword && password.isNotBlank()
    }

    private fun showError(message: String) {
        binding.errorText.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun updateLoadingState() {
        binding.signupButton.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
} 