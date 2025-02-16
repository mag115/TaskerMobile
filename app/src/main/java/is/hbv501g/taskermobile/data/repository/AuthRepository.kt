package `is`.hbv501g.taskermobile.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import `is`.hbv501g.taskermobile.data.api.AuthApiService
import `is`.hbv501g.taskermobile.data.model.LoginRequest
import `is`.hbv501g.taskermobile.data.model.SignupRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val dataStore: DataStore<Preferences>,
    private val authApiService: AuthApiService
) {
    private val authTokenKey = stringPreferencesKey("auth_token")

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = authApiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.token?.let { saveAuthToken(it) }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun signup(email: String, password: String): Result<Unit> {
        return try {
            val response = authApiService.signup(SignupRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.token?.let { saveAuthToken(it) }
                Result.success(Unit)
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