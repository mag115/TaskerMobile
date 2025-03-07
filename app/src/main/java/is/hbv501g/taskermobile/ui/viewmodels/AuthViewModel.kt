package `is`.hbv501g.taskermobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hbv501g.taskermobile.data.model.LoginResponse
import `is`.hbv501g.taskermobile.data.service.AuthenticationService
import `is`.hbv501g.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authenticationService: AuthenticationService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<Result<LoginResponse>?>(null)
    val loginState: StateFlow<Result<LoginResponse>?> get() = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val result = authenticationService.login(username, password)
            _loginState.value = result
        }
    }

    fun signup(username: String, email: String, password: String) {
        viewModelScope.launch {
            val result = authenticationService.signup(username, email, password)
            _loginState.value = result
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _loginState.value = null
        }
    }

    fun getSessionManager(): SessionManager = sessionManager
}