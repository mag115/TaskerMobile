package com.taskermobile.data.session

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val CURRENT_PROJECT_ID_KEY = longPreferencesKey("current_project_id")
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val EXPIRES_AT = longPreferencesKey("expires_at") // For expiration timestamp
        private val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
    }

    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val userId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val currentProjectId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_PROJECT_ID_KEY]
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserId(userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun saveCurrentProjectId(projectId: Long) {
        Log.d("SessionManager", "Saving current project ID: $projectId")
        context.dataStore.edit { preferences ->
            preferences[CURRENT_PROJECT_ID_KEY] = projectId
        }
    }

    // Save full login response (token + expiration)
    suspend fun saveLoginDetails(
        token: String,
        expiresIn: Long,
        userId: Long,
        username: String
    ) {
        val expiresAt = System.currentTimeMillis() + expiresIn // Convert seconds to milliseconds

        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            preferences[EXPIRES_AT] = expiresAt // Store calculated expiration
        }
    }

    // Check if token is expired
    suspend fun isTokenExpired(): Boolean {
        val expiresAt = context.dataStore.data.first()[EXPIRES_AT]
        return expiresAt == null || System.currentTimeMillis() > expiresAt
    }

    val authState: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            val token = preferences[AUTH_TOKEN_KEY]
            val expiresAt = preferences[EXPIRES_AT] ?: 0
            val currentTime = System.currentTimeMillis()

            Log.d("SessionManager", "Token: $token")
            Log.d("SessionManager", "Expires At (Millis): $expiresAt")
            Log.d("SessionManager", "Current Time (Millis): $currentTime")
            Log.d("SessionManager", "Time Until Expiry (Seconds): ${(expiresAt - currentTime) / 1000}")

            // Check token validity
            val isValid = token != null && expiresAt > 0 && currentTime < expiresAt

            Log.d("SessionManager", "Is Token Valid: $isValid")

            if (!isValid) {
                Log.d("SessionManager", "❌ Token is expired. Clearing session...")
                clearSession() // Invalidate session if token is expired
            }

            isValid
        }

    val validateToken: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val expiresAt = preferences[EXPIRES_AT]
        val isValid = expiresAt != null && System.currentTimeMillis() < expiresAt

        if (!isValid) {
            clearSession() // Clear session if expired
        }

        isValid
    }

    // Individual getters (optional)
    val authToken: Flow<String?> = context.dataStore.data.map { it[AUTH_TOKEN_KEY] }
    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME_KEY] }

    suspend fun getCurrentProjectId(): Long? {
        return context.dataStore.data.first()[CURRENT_PROJECT_ID_KEY]
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // **New: Save FCM Token**
    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[FCM_TOKEN_KEY] = token
        }
    }

    suspend fun getFcmToken(): String? {
        return context.dataStore.data.first()[FCM_TOKEN_KEY]
    }

    fun refreshFcmToken(onTokenRefreshed: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { newToken ->
                    CoroutineScope(Dispatchers.IO).launch {
                        saveFcmToken(newToken)
                        onTokenRefreshed(newToken)
                    }
                }
            }
        }
    }
}