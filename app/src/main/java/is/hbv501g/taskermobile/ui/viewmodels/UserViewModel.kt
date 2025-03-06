package `is`.hbv501g.taskermobile.ui.viewmodels
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hbv501g.taskermobile.data.api.UserService
import `is`.hbv501g.taskermobile.data.model.User
import `is`.hbv501g.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UserViewModel(
    private val userService: UserService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = sessionManager.userId.first()
            userId?.let {
                try {
                    val response = userService.getUserById(it)
                    if (response.isSuccessful) {
                        _currentUser.value = response.body()
                    } else {
                        Log.e("UserViewModel", "Failed to load user: ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Error fetching user", e)
                }
            }
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            try {
                val response = userService.getAllUsers()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _users.value = it
                    }
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching users", e)
            }
        }
    }
}
