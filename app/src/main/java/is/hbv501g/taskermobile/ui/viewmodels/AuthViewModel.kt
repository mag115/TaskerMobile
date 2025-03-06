package `is`.hbv501g.taskermobile.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hbv501g.taskermobile.data.api.AuthApiService
import `is`.hbv501g.taskermobile.data.model.LoginRequest
import `is`.hbv501g.taskermobile.data.model.LoginResponse
import `is`.hbv501g.taskermobile.data.model.SignupRequest
import `is`.hbv501g.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<Result<LoginResponse>?>(null)
    val loginState: StateFlow<Result<LoginResponse>?> = _loginState.asStateFlow()

    fun getSessionManager(): SessionManager = sessionManager

    // Login User and Save Session
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = authApiService.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    val loginResponse = response.body()!!
                    sessionManager.saveLoginDetails(
                        token = loginResponse.token,
                        expiresIn = loginResponse.expiresIn,
                        userId = loginResponse.userId,
                        username = loginResponse.username
                    )
                    _loginState.value = Result.success(loginResponse)
                } else {
                    _loginState.value = Result.failure(Exception("Login failed"))
                }
            } catch (e: Exception) {
                _loginState.value = Result.failure(e)
            }
        }
    }

    // Signup User
    fun signup(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = authApiService.signup(SignupRequest(username, email, password))
                if (response.isSuccessful) {
                    val signupResponse = response.body()!!
                    sessionManager.saveLoginDetails(
                        token = signupResponse.token,
                        expiresIn = signupResponse.expiresIn,
                        userId = signupResponse.userId,
                        username = signupResponse.username
                    )
                    _loginState.value = Result.success(signupResponse)
                } else {
                    _loginState.value = Result.failure(Exception("Signup failed"))
                }
            } catch (e: Exception) {
                _loginState.value = Result.failure(e)
            }
        }
    }

    // Logout User
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _loginState.value = null
        }
    }
}
