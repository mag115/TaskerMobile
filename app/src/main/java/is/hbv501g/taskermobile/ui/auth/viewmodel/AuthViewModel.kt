package `is`.hbv501g.taskermobile.ui.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hbv501g.taskermobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    val authState: Flow<Boolean> = repository.isAuthenticated()

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val result = repository.login(email, password)
        result.fold(
            onSuccess = {
                Log.d("AuthViewModel", "Login successful: $it")
                onResult(true)
            },
            onFailure = { error ->
                Log.e("AuthViewModel", "Login failed: ${error.message}", error)
                onResult(false)
            }
        )
    }

    fun signup(email: String, password: String, onResult: (Boolean, String?) -> Unit) = viewModelScope.launch {
        val result = repository.signup(email, password)
        result.fold(
            onSuccess = {
                Log.d("AuthViewModel", "Signup successful: $it")
                onResult(true, null)
            },
            onFailure = { throwable ->
                Log.e("AuthViewModel", "Signup failed: ${throwable.message}", throwable)
                onResult(false, throwable.message)
            }
        )
    }

    fun logout() = viewModelScope.launch {
        repository.clearAuthToken()
        Log.d("AuthViewModel", "User logged out")
    }
}
