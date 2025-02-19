package `is`.hbv501g.taskermobile.ui.controllers

import android.util.Log
import `is`.hbv501g.taskermobile.data.api.RetrofitClient
import `is`.hbv501g.taskermobile.data.api.UserService
import `is`.hbv501g.taskermobile.data.model.User
import `is`.hbv501g.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

class UserController(sessionManager: SessionManager) {
    private val userService = RetrofitClient.createService<UserService>(sessionManager)

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

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
