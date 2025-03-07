package `is`.hbv501g.taskermobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `is`.hbv501g.taskermobile.data.model.User
import `is`.hbv501g.taskermobile.data.service.UserServiceImpl
import `is`.hbv501g.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserViewModel(
    private val userService: UserServiceImpl,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> get() = _users

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> get() = _currentUser

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = sessionManager.userId.first()
            if (userId != null) {
                userService.getUserById(userId).onSuccess { user ->
                    _currentUser.value = user
                }
            }
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            userService.getAllUsers().onSuccess { userList ->
                _users.value = userList
            }
        }
    }
}
