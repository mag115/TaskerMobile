package com.taskermobile.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.taskermobile.data.api.AuthApiService
import com.taskermobile.data.model.LoginRequest
import com.taskermobile.data.model.LoginResponse
import com.taskermobile.data.model.SignupRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AuthRepository(
    private val authApiService: AuthApiService
) {
    private val authTokenKey = stringPreferencesKey("auth_token")

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                authApiService.login(LoginRequest(email, password))
            }
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse != null && loginResponse.token != null) {
                    Result.success(loginResponse) // Correctly return LoginResponse
                } else {
                    Result.failure(Exception("Empty login response or missing token"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }


    suspend fun signup(username: String, email: String, password: String): Result<LoginResponse> {
        return try {
            val response = authApiService.signup(SignupRequest(username, email, password))
            if (response.isSuccessful) {
                val signupResponse = response.body()
                if (signupResponse != null) {
                    Result.success(signupResponse)
                } else {
                    Result.failure(Exception("Empty signup response"))
                }
            } else {
                Result.failure(Exception("Signup failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }
}