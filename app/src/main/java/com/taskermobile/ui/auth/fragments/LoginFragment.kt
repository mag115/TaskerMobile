package com.taskermobile.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.taskermobile.R
import com.taskermobile.databinding.FragmentLoginBinding
import com.taskermobile.MainActivity
import com.taskermobile.ui.auth.AuthActivity
import com.taskermobile.ui.auth.controllers.AuthController

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var authController: AuthController
    private var loading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
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
            passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
        }
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            if (loading) return@setOnClickListener

            loading = true
            updateLoadingState()

            val username = binding.usernameInput.text.toString() // Use username, not email
            val password = binding.passwordInput.text.toString()

            authController.login(username, password) { success, message ->
                loading = false
                activity?.runOnUiThread {
                    updateLoadingState()
                    if (success) {
                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(activity, MainActivity::class.java))
                        activity?.finish()
                    } else {
                        binding.errorText.text = message ?: "Login failed"
                        binding.errorText.visibility = View.VISIBLE
                    }
                }
            }
        }


        binding.signupLink.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, SignupFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun updateLoadingState() {
        binding.loginButton.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 