package com.taskermobile.ui.main.controllers

import android.util.Log
import com.taskermobile.data.api.RetrofitClient
import com.taskermobile.data.model.User
import com.taskermobile.data.service.UserService
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

class UserController(private val sessionManager: SessionManager) {
    private val userService = RetrofitClient.createService<UserService>(sessionManager)

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        runBlocking {
            val userId = sessionManager.userId.first()
            userId?.let { id ->
                fetchCurrentUser(id)
            }
        }
    }

    private fun fetchCurrentUser(userId: Long) {
        runBlocking {
            try {
                val response = userService.getUserById(userId)
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        _currentUser.value = user
                    }
                } else {
                    Log.e("UserController", "Failed to fetch current user: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("UserController", "Exception fetching current user", e)
            }
        }
    }

    fun fetchAllUsers() {
        println("Starting to fetch all users...")
        runBlocking {
            try {
                val response = userService.getAllUsers()
                if (response.isSuccessful) {
                    response.body()?.let { usersList ->
                        println("Successfully fetched ${usersList.size} users")
                        _users.update { usersList }
                    } ?: run {
                        println("Response was successful but body was null")
                    }
                } else {
                    println("API request failed: HTTP ${response.code()} - ${response.message()}")
                    response.errorBody()?.let { errorBody ->
                        println("Error details: ${errorBody.string()}")
                    }
                }
            } catch (e: Exception) {
                println("Exception during user fetch: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
