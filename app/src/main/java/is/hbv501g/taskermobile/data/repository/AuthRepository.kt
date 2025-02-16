package `is`.hbv501g.taskermobile.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import `is`.hbv501g.taskermobile.data.api.AuthApiService
import `is`.hbv501g.taskermobile.data.model.LoginRequest
import `is`.hbv501g.taskermobile.data.model.SignupRequest
import `is`.hbv501g.taskermobile.data.model.SignupResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val dataStore: DataStore<Preferences>,
    private val authApiService: AuthApiService
) {
    private val authTokenKey = stringPreferencesKey("auth_token")

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val response = authApiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val token = response.body()?.token
                if (token != null) {
                    saveAuthToken(token)
                    Result.success(token)
                } else {
                    Result.failure(Exception("Empty token"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun signup(username: String, email: String, password: String): Result<SignupResponse> {
        return try {
            val response = authApiService.signup(SignupRequest(username, email, password))
            if (response.isSuccessful) {
                response.body()?.let { signupResponse ->
                    // Optionally save the token if your response contains one:
                    signupResponse.userId?.let { token ->
                        saveAuthToken(token)
                    }
                    Result.success(signupResponse)
                } ?: Result.failure(Exception("Empty signup response"))
            } else {
                Result.failure(Exception("Signup failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    private suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[authTokenKey] = token
        }
    }

    suspend fun clearAuthToken() {
        dataStore.edit { preferences ->
            preferences.remove(authTokenKey)
        }
    }

    fun getAuthToken(): Flow<String?> = dataStore.data.map { it[authTokenKey] }

    fun isAuthenticated(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[authTokenKey]?.isNotEmpty() ?: false
    }
}
