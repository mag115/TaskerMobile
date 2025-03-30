package com.taskermobile.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.taskermobile.R
import com.taskermobile.databinding.FragmentSignupBinding
import com.taskermobile.MainActivity
import com.taskermobile.TaskerApplication
import com.taskermobile.data.session.SessionManager
import com.taskermobile.ui.auth.AuthActivity
import com.taskermobile.ui.auth.controllers.AuthController
import com.taskermobile.util.CryptographyManager
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.crypto.Cipher

enum class SignupStep {
    CREDENTIALS,
    BIOMETRIC_OPT_IN
}

enum class BiometricOperation {
    NONE,
    ENCRYPT_SETUP
}

class SignupFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding
    private lateinit var authController: AuthController
    private lateinit var sessionManager: SessionManager
    private lateinit var cryptographyManager: CryptographyManager
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    
    private var loading = false
    private var currentSignupStep = SignupStep.CREDENTIALS
    private var currentBiometricOperation = BiometricOperation.NONE
    private var receivedToken: String? = null

    companion object {
        const val KEY_NAME = "biometric_secret_key"
        private const val TAG = "SignupFragment"
    }

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
        sessionManager = SessionManager(requireContext())
        cryptographyManager = CryptographyManager()
        executor = ContextCompat.getMainExecutor(requireContext())

        setupViews()
        setupBiometricPrompt()
        setupListeners()
    }

    private fun setupViews() {
        binding.apply {
            usernameInput.addTextChangedListener { validateInputs() }
            emailInput.addTextChangedListener { validateInputs() }
            passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
            confirmPasswordInput.transformationMethod = PasswordTransformationMethod.getInstance()

            // Setup Role Selection Spinner
            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.user_roles,  // Defined in strings.xml
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.roleSpinner.adapter = adapter
            }
            
            // Initially hide biometric opt-in UI
            enableBiometricCheckbox.visibility = View.GONE
        }
    }

    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(TAG, "Biometric Authentication error: [$errorCode] $errString (Op: $currentBiometricOperation)")
                    Toast.makeText(context, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    
                    currentBiometricOperation = BiometricOperation.NONE
                    
                    lifecycleScope.launch {
                        sessionManager.setBiometricLoginEnabled(false)
                        sessionManager.clearEncryptedToken()
                    }
                    finalizeSignupAndNavigate() 
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w(TAG, "Biometric Authentication failed. (Op: $currentBiometricOperation)")
                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.i(TAG, "Biometric Authentication succeeded! (Op: $currentBiometricOperation)")
                    val cipher = result.cryptoObject?.cipher

                    when (currentBiometricOperation) {
                        BiometricOperation.ENCRYPT_SETUP -> handleEncryptionSuccess(cipher)
                        BiometricOperation.NONE -> Log.w(TAG, "Auth success callback with NONE operation?")
                    }
                    currentBiometricOperation = BiometricOperation.NONE
                }
            })
    }

    private fun setupListeners() {
        binding.signupButton.setOnClickListener {
            when (currentSignupStep) {
                SignupStep.CREDENTIALS -> handleCredentialsSignup()
                SignupStep.BIOMETRIC_OPT_IN -> handleBiometricOptInDecision()
            }
        }

        binding.loginLink.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun handleCredentialsSignup() {
        if (loading) return@handleCredentialsSignup

        val username = binding.usernameInput.text.toString()
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()
        val role = binding.roleSpinner.selectedItem.toString() // Capture role

        if (!validatePasswords(password, confirmPassword)) {
            showError("Passwords don't match")
            return
        }

        loading = true
        updateLoadingState()

        lifecycleScope.launch {
            authController.signup(username, email, password, role) { response, message ->
                loading = false
                activity?.runOnUiThread {
                    updateLoadingState()
                    if (response != null) {
                        receivedToken = response.token
                        handleSignupSuccess()
                    } else {
                        showError(message ?: "Signup failed")
                    }
                }
            }
        }
    }

    private fun handleSignupSuccess() {
        Toast.makeText(context, "Signup Successful!", Toast.LENGTH_SHORT).show()
        
        val biometricManager = BiometricManager.from(requireContext())
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d(TAG, "Biometric authentication is available for setup.")
                binding.apply {
                    enableBiometricCheckbox.visibility = View.VISIBLE
                    signupButton.text = getString(R.string.continue_text)
                    usernameInput.visibility = View.GONE
                    emailInput.visibility = View.GONE
                    passwordInput.visibility = View.GONE
                    confirmPasswordInput.visibility = View.GONE
                    roleSpinner.visibility = View.GONE
                    loginLink.visibility = View.GONE
                }
                currentSignupStep = SignupStep.BIOMETRIC_OPT_IN
            }
            else -> {
                Log.d(TAG, "Biometric authentication not available/enrolled. Proceeding without opt-in.")
                lifecycleScope.launch {
                    sessionManager.setBiometricLoginEnabled(false)
                    sessionManager.clearEncryptedToken()
                    cryptographyManager.deleteKey(KEY_NAME)
                    finalizeSignupAndNavigate()
                }
            }
        }
    }

    private fun handleBiometricOptInDecision() {
        val enableBiometric = binding.enableBiometricCheckbox.isChecked
        Log.d(TAG, "Biometric checkbox checked: $enableBiometric")

        if (enableBiometric) {
            receivedToken?.let { token ->
                try {
                    Log.d(TAG, "Attempting biometric authentication for ENCRYPTION setup.")
                    currentBiometricOperation = BiometricOperation.ENCRYPT_SETUP
                    val cipher = cryptographyManager.getInitializedCipherForEncryption(KEY_NAME)
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle(getString(R.string.biometric_auth_title))
                        .setSubtitle(getString(R.string.biometric_auth_subtitle_encrypt))
                        .setNegativeButtonText(getString(R.string.cancel))
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                        .setConfirmationRequired(false)
                        .build()
                    biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing cipher for biometric prompt", e)
                    Toast.makeText(context, "Could not start biometric setup: ${e.message}", Toast.LENGTH_LONG).show()
                    currentBiometricOperation = BiometricOperation.NONE 
                    lifecycleScope.launch {
                        sessionManager.setBiometricLoginEnabled(false)
                    }
                    finalizeSignupAndNavigate()
                }
            } ?: run {
                Log.e(TAG, "Error: Token was null when trying to enable biometric login.")
                Toast.makeText(context, "Error enabling biometric login. Please try logging in again.", Toast.LENGTH_LONG).show()
                lifecycleScope.launch {
                    sessionManager.setBiometricLoginEnabled(false)
                }
                finalizeSignupAndNavigate()
            }
        } else {
            Log.d(TAG, "Biometric login explicitly disabled by user.")
            currentBiometricOperation = BiometricOperation.NONE
            lifecycleScope.launch {
                sessionManager.setBiometricLoginEnabled(false)
                sessionManager.clearEncryptedToken()
                cryptographyManager.deleteKey(KEY_NAME)
                finalizeSignupAndNavigate()
            }
        }
    }

    private fun handleEncryptionSuccess(cipher: Cipher?) {
        val tokenToEncrypt = receivedToken
        if (cipher != null && tokenToEncrypt != null) {
            lifecycleScope.launch {
                try {
                    Log.d(TAG, "Encrypting and saving token after biometric auth.")
                    val encryptedData = cryptographyManager.encryptData(tokenToEncrypt, cipher)
                    sessionManager.saveEncryptedToken(encryptedData.ciphertext, encryptedData.initializationVector)
                    sessionManager.setBiometricLoginEnabled(true)
                    Log.i(TAG, "Biometric login enabled and token encrypted.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error encrypting/saving token post-auth", e)
                    Toast.makeText(context, "Error saving biometric preference: ${e.message}", Toast.LENGTH_LONG).show()
                    sessionManager.setBiometricLoginEnabled(false)
                    sessionManager.clearEncryptedToken()
                    cryptographyManager.deleteKey(KEY_NAME)
                } finally {
                    finalizeSignupAndNavigate() 
                }
            }
        } else {
            Log.e(TAG, "Encryption success callback but cipher or token was null.")
            Toast.makeText(context, "Error setting up biometric login. Cipher or token missing.", Toast.LENGTH_LONG).show()
            lifecycleScope.launch { sessionManager.setBiometricLoginEnabled(false) }
            finalizeSignupAndNavigate()
        }
    }

    private fun finalizeSignupAndNavigate() {
        lifecycleScope.launch {
            receivedToken?.let { token ->
                try {
                    sessionManager.saveToken(token)
                    (activity?.application as? TaskerApplication)?.currentDecryptedAuthToken = token
                    Log.d(TAG, "Raw token saved and set in application state.")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save raw token or set in application", e)
                }
            } ?: Log.w(TAG, "Cannot save raw token or set in app, receivedToken is null.")

            navigateToMain()
        }
    }

    private fun navigateToMain() {
        Log.d(TAG, "Navigating to MainActivity.")
        activity?.let {
            val intent = Intent(it, MainActivity::class.java)
            startActivity(intent)
            it.finish()
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
        
        if (currentSignupStep == SignupStep.CREDENTIALS) {
            binding.usernameInput.isEnabled = !loading
            binding.emailInput.isEnabled = !loading
            binding.passwordInput.isEnabled = !loading
            binding.confirmPasswordInput.isEnabled = !loading
            binding.roleSpinner.isEnabled = !loading
            binding.loginLink.isEnabled = !loading
        } else {
            binding.enableBiometricCheckbox.isEnabled = !loading
        }
    }
} 