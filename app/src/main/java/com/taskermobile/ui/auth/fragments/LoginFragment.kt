package com.taskermobile.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.taskermobile.R
import com.taskermobile.data.model.LoginResponse
import com.taskermobile.data.session.SessionManager
import com.taskermobile.databinding.FragmentLoginBinding
import com.taskermobile.MainActivity
import com.taskermobile.ui.auth.AuthActivity
import com.taskermobile.ui.auth.controllers.AuthController
import com.taskermobile.util.CryptographyManager
import kotlinx.coroutines.launch
import com.taskermobile.TaskerApplication
import kotlinx.coroutines.flow.first
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException

enum class LoginStep {
    CREDENTIALS,
    BIOMETRIC_OPT_IN
}

enum class LoginOperation {
    NONE,
    ENCRYPT_SETUP,
    DECRYPT_LOGIN
}

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var authController: AuthController
    private lateinit var sessionManager: SessionManager
    private lateinit var cryptographyManager: CryptographyManager
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private var loginUserInfo: LoginResponse? = null

    private var loading = false
    private var currentLoginStep = LoginStep.CREDENTIALS
    private var currentBiometricOperation = LoginOperation.NONE
    private var receivedToken: String? = null
    private var ciphertextToDecrypt: ByteArray? = null

    companion object {
        const val KEY_NAME = "biometric_secret_key"
    }

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
        sessionManager = SessionManager(requireContext())
        cryptographyManager = CryptographyManager()
        executor = ContextCompat.getMainExecutor(requireContext())

        setupViews()
        setupBiometricPrompt()
        checkBiometricLoginAvailability() // Check if we should show biometric login option
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        Log.d("LoginFragment", "onResume called, checking biometric login availability")
        checkBiometricLoginAvailability()
    }

    private fun setupViews() {
        binding.apply {
            passwordInput.transformationMethod = PasswordTransformationMethod.getInstance()
            biometricLoginButton.visibility = View.GONE
        }
    }

    private fun checkBiometricLoginAvailability() {
        Log.d("LoginFragment", "Starting biometric login availability check")
        lifecycleScope.launch {
            try {
                val isBiometricEnabled = sessionManager.isBiometricLoginEnabled()
                val hasEncryptedToken = sessionManager.getEncryptedTokenCiphertext() != null
                val hasEncryptedTokenIv = sessionManager.getEncryptedTokenIv() != null
                val biometricManager = BiometricManager.from(requireContext())
                val canAuthStatus = biometricManager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                )
                val canAuthenticate = canAuthStatus == BiometricManager.BIOMETRIC_SUCCESS

                // Detailed status check 
                val statusString = when (canAuthStatus) {
                    BiometricManager.BIOMETRIC_SUCCESS -> "BIOMETRIC_SUCCESS"
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "BIOMETRIC_ERROR_NO_HARDWARE"
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "BIOMETRIC_ERROR_HW_UNAVAILABLE"
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "BIOMETRIC_ERROR_NONE_ENROLLED"
                    BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED"
                    BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "BIOMETRIC_ERROR_UNSUPPORTED"
                    BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "BIOMETRIC_STATUS_UNKNOWN"
                    else -> "UNKNOWN_STATUS($canAuthStatus)"
                }

                Log.d(
                    "LoginFragment",
                    "Biometric login check: enabled=$isBiometricEnabled, token=$hasEncryptedToken, iv=$hasEncryptedTokenIv, canAuth=$canAuthenticate, status=$statusString"
                )

                // We need all conditions to be true to offer biometric login
                val shouldOfferBiometricLogin =
                    isBiometricEnabled && hasEncryptedToken && hasEncryptedTokenIv && canAuthenticate

                activity?.runOnUiThread {
                    if (shouldOfferBiometricLogin) {
                        Log.d("LoginFragment", "Offering biometric login option")
                        binding.biometricLoginButton.visibility = View.VISIBLE
                    } else {
                        Log.d(
                            "LoginFragment",
                            "Not offering biometric login: enabled=$isBiometricEnabled, token=$hasEncryptedToken, iv=$hasEncryptedTokenIv, canAuth=$canAuthenticate, status=$statusString"
                        )
                        binding.biometricLoginButton.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginFragment", "Error checking biometric login availability", e)
                // On error, don't offer biometric login
                activity?.runOnUiThread {
                    binding.biometricLoginButton.visibility = View.GONE
                }
            }
        }
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            when (currentLoginStep) {
                LoginStep.CREDENTIALS -> handleCredentialsLogin()
                LoginStep.BIOMETRIC_OPT_IN -> handleBiometricOptInDecision()
            }
        }

        // Add biometric login button click listener
        binding.biometricLoginButton.setOnClickListener {
            Log.d("LoginFragment", "Biometric login button clicked")
            triggerBiometricLoginFlow()
        }

        binding.signupLink.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, SignupFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(
                        "LoginFragment",
                        "Biometric Authentication error: [$errorCode] $errString (Op: $currentBiometricOperation)"
                    )
                    Toast.makeText(context, "Authentication error: $errString", Toast.LENGTH_SHORT)
                        .show()

                    // Reset operation state
                    val operation = currentBiometricOperation
                    currentBiometricOperation = LoginOperation.NONE
                    ciphertextToDecrypt = null

                    if (operation == LoginOperation.ENCRYPT_SETUP) {
                        lifecycleScope.launch {
                            sessionManager.setBiometricLoginEnabled(false)
                            sessionManager.clearEncryptedToken()
                        }
                        finalizeLoginAndNavigate()
                    } else if (operation == LoginOperation.DECRYPT_LOGIN) {
                        showCredentialFields()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w(
                        "LoginFragment",
                        "Biometric Authentication failed. (Op: $currentBiometricOperation)"
                    )
                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.i(
                        "LoginFragment",
                        "Biometric Authentication succeeded! (Op: $currentBiometricOperation)"
                    )
                    val cipher = result.cryptoObject?.cipher

                    when (currentBiometricOperation) {
                        LoginOperation.ENCRYPT_SETUP -> handleEncryptionSuccess(cipher)
                        LoginOperation.DECRYPT_LOGIN -> handleDecryptionSuccess(cipher)
                        LoginOperation.NONE -> Log.w(
                            "LoginFragment",
                            "Auth success callback with NONE operation?"
                        )
                    }
                    currentBiometricOperation = LoginOperation.NONE
                    ciphertextToDecrypt = null
                }
            })

    }

    private fun handleCredentialsLogin() {
        if (loading) return

        loading = true
        updateLoadingState()
        clearError()

        val username = binding.usernameInput.text.toString()
        val password = binding.passwordInput.text.toString()

        authController.login(username, password) { response, message ->
            loading = false
            activity?.runOnUiThread {
                updateLoadingState()
                if (response != null) {
                    receivedToken = response.token
                    loginUserInfo = response
                    handleLoginSuccess()
                } else {
                    showError(message ?: "Login failed")
                }
            }
        }
    }

    private fun handleLoginSuccess() {
        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()


        val biometricManager = BiometricManager.from(requireContext())
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("LoginFragment", "Biometric authentication is available for setup.")
                // Show opt-in UI
                binding.enableBiometricCheckbox.visibility = View.VISIBLE
                binding.loginButton.text = getString(R.string.continue_text)
                binding.usernameInput.visibility = View.GONE
                binding.passwordInput.visibility = View.GONE
                binding.signupLink.visibility = View.GONE
                // Hide biometric login button during setup
                binding.biometricLoginButton.visibility = View.GONE
                currentLoginStep = LoginStep.BIOMETRIC_OPT_IN
            }

            else -> {
                Log.d(
                    "LoginFragment",
                    "Biometric authentication not available/enrolled. Proceeding without opt-in."
                )
                lifecycleScope.launch {
                    sessionManager.setBiometricLoginEnabled(false)
                    sessionManager.clearEncryptedToken()
                    cryptographyManager.deleteKey(KEY_NAME)
                    finalizeLoginAndNavigate()
                }
            }
        }
    }

    private fun handleBiometricOptInDecision() {
        val enableBiometric = binding.enableBiometricCheckbox.isChecked
        Log.d("LoginFragment", "Biometric checkbox checked: $enableBiometric")

        if (enableBiometric) {
            receivedToken?.let { token ->
                try {
                    Log.d(
                        "LoginFragment",
                        "Attempting biometric authentication for ENCRYPTION setup."
                    )
                    currentBiometricOperation = LoginOperation.ENCRYPT_SETUP
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
                    Log.e("LoginFragment", "Error initializing cipher for biometric prompt", e)
                    Toast.makeText(
                        context,
                        "Could not start biometric setup: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    currentBiometricOperation = LoginOperation.NONE
                    lifecycleScope.launch {
                        sessionManager.setBiometricLoginEnabled(false)
                    }
                    finalizeLoginAndNavigate()
                }
            } ?: run {
                Log.e(
                    "LoginFragment",
                    "Error: Token was null when trying to enable biometric login."
                )
                Toast.makeText(
                    context,
                    "Error enabling biometric login. Please try logging in again.",
                    Toast.LENGTH_LONG
                ).show()
                currentBiometricOperation = LoginOperation.NONE
                lifecycleScope.launch {
                    sessionManager.setBiometricLoginEnabled(false)
                }
                finalizeLoginAndNavigate()
            }
        } else {
            Log.d("LoginFragment", "Biometric login explicitly disabled by user.")
            currentBiometricOperation = LoginOperation.NONE
            lifecycleScope.launch {
                sessionManager.setBiometricLoginEnabled(false)
                sessionManager.clearEncryptedToken()
                cryptographyManager.deleteKey(KEY_NAME)
                finalizeLoginAndNavigate()
            }
        }
    }

    private fun handleEncryptionSuccess(cipher: Cipher?) {
        val tokenToEncrypt = receivedToken
        val loginInfo = loginUserInfo
        if (cipher != null && tokenToEncrypt != null) {
            lifecycleScope.launch {
                try {
                    Log.d("LoginFragment", "Encrypting and saving token after biometric auth.")
                    val encryptedData = cryptographyManager.encryptData(tokenToEncrypt, cipher)
                    sessionManager.saveEncryptedToken(
                        encryptedData.ciphertext,
                        encryptedData.initializationVector
                    )

                    if (loginInfo != null) {
                        sessionManager.saveEncryptedUserData(
                            encryptedToken = encryptedData.ciphertext,
                            iv = encryptedData.initializationVector,
                            userId = loginInfo.userId,
                            username = loginInfo.username,
                            role = loginInfo.role
                        )
                    }

                    sessionManager.setBiometricLoginEnabled(true)
                    Log.i("LoginFragment", "Biometric login enabled and token encrypted.")
                } catch (e: Exception) {
                    Log.e("LoginFragment", "Error encrypting/saving token post-auth", e)
                    Toast.makeText(
                        context,
                        "Error saving biometric preference: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    sessionManager.setBiometricLoginEnabled(false)
                    sessionManager.clearEncryptedToken()
                    cryptographyManager.deleteKey(KEY_NAME)
                } finally {
                    finalizeLoginAndNavigate()
                }
            }
        } else {
            Log.e("LoginFragment", "Encryption success callback but cipher or token was null.")
            Toast.makeText(
                context,
                "Error setting up biometric login. Cipher or token missing.",
                Toast.LENGTH_LONG
            ).show()
            currentBiometricOperation = LoginOperation.NONE // Reset state on immediate error
            lifecycleScope.launch { sessionManager.setBiometricLoginEnabled(false) }
            finalizeLoginAndNavigate()
        }
    }

    private fun handleDecryptionSuccess(cipher: Cipher?) {
        val savedCiphertext = ciphertextToDecrypt
        if (cipher != null && savedCiphertext != null) {
            lifecycleScope.launch {
                try {
                    Log.d("LoginFragment", "Decrypting token after biometric login auth.")
                    val decryptedToken = cryptographyManager.decryptData(savedCiphertext, cipher)
                    Log.i("LoginFragment", "Biometric login successful. Token decrypted.")
                    sessionManager.saveToken(decryptedToken)
                    val userInfo = sessionManager.getEncryptedUserInfo()
                    Log.d("LoginFragment", "Saving login details after biometric auth: ${userInfo?.username}, ${userInfo?.role}")
                    if (userInfo != null) {

                        sessionManager.saveLoginDetails(
                            expiresIn = 3600,
                            userId = userInfo.userId,
                            username = userInfo.username,
                            role = userInfo.role
                        )
                    }

                    (activity?.application as? TaskerApplication)?.currentDecryptedAuthToken =
                        decryptedToken
                    navigateToMain()
                } catch (e: Exception) {

                    Log.e("LoginFragment", "Error decrypting token post-auth", e)
                    Toast.makeText(
                        context,
                        "Biometric login failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    showCredentialFields()
                }
            }
        } else {
            Log.e("LoginFragment", "Decryption success callback but cipher or ciphertext was null.")
            Toast.makeText(context, "Biometric login failed. Data missing.", Toast.LENGTH_LONG)
                .show()
            showCredentialFields()
        }
    }

    private fun finalizeLoginAndNavigate() {
        lifecycleScope.launch {
            receivedToken?.let { _ ->
                try {
                    sessionManager.saveToken(receivedToken!!)
                    (activity?.application as? TaskerApplication)?.currentDecryptedAuthToken =
                        receivedToken
                    Log.d("LoginFragment", "Raw token saved and set in application state.")
                } catch (e: Exception) {
                    Log.e("LoginFragment", "Failed to save raw token or set in application", e)
                }
            } ?: Log.w(
                "LoginFragment",
                "Cannot save raw token or set in app, receivedToken is null."
            )

            navigateToMain()
        }
    }

    private fun navigateToMain() {
        Log.d("LoginFragment", "Navigating to MainActivity.")
        activity?.let {
            val intent = Intent(it, MainActivity::class.java)
            startActivity(intent)
            it.finish()
        }
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
    }

    private fun clearError() {
        binding.errorText.visibility = View.GONE
    }

    private fun updateLoadingState() {
        binding.loginButton.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        if (currentLoginStep == LoginStep.CREDENTIALS) {
            binding.usernameInput.isEnabled = !loading
            binding.passwordInput.isEnabled = !loading
            binding.signupLink.isEnabled = !loading
        } else {
            binding.enableBiometricCheckbox.isEnabled = !loading
        }
    }

    private fun showCredentialFields() {
        binding.usernameInput.visibility = View.VISIBLE
        binding.passwordInput.visibility = View.VISIBLE
        binding.loginButton.visibility = View.VISIBLE
        binding.signupLink.visibility = View.VISIBLE
        binding.biometricLoginButton.visibility = View.GONE
        binding.enableBiometricCheckbox.visibility = View.GONE
        currentLoginStep = LoginStep.CREDENTIALS
    }

    private fun triggerBiometricLoginFlow() {
        Log.d("LoginFragment", "Attempting biometric authentication for DECRYPTION login.")
        lifecycleScope.launch {
            val ciphertext = sessionManager.getEncryptedTokenCiphertext()
            val iv = sessionManager.getEncryptedTokenIv()

            if (ciphertext == null || iv == null) {
                Log.e("LoginFragment", "Cannot attempt biometric login: Encrypted data missing.")
                Toast.makeText(
                    context,
                    "Biometric data not found. Please login normally.",
                    Toast.LENGTH_LONG
                ).show()
                showCredentialFields()
                return@launch
            }

            try {
                ciphertextToDecrypt = ciphertext
                currentBiometricOperation = LoginOperation.DECRYPT_LOGIN
                val cipher = cryptographyManager.getInitializedCipherForDecryption(KEY_NAME, iv)
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.biometric_login_title))
                    .setSubtitle(getString(R.string.biometric_login_subtitle))
                    .setNegativeButtonText(getString(R.string.cancel))
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .setConfirmationRequired(false)
                    .build()
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            } catch (e: Exception) {
                Log.e("LoginFragment", "Error initializing cipher for biometric login", e)
                Toast.makeText(
                    context,
                    "Could not start biometric login: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                ciphertextToDecrypt = null // Clear temp data
                currentBiometricOperation = LoginOperation.NONE
                showCredentialFields() // Fallback to username/password
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 