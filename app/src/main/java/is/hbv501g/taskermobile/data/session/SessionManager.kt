package `is`.hbv501g.taskermobile.data.session

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val USER_ID = longPreferencesKey("userId")
        private val EXPIRES_AT = longPreferencesKey("expires_at") // For expiration timestamp
        private const val DEBUG = true // Toggle for logging
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
            preferences[USER_ID] = userId
            preferences[USERNAME_KEY] = username
            preferences[EXPIRES_AT] = expiresAt // Store calculated expiration
        }

        if (DEBUG) Log.d("SessionManager", "✅ Login saved: User $username, Expiry: $expiresAt")
    }

    // Check if token is expired
    suspend fun isTokenExpired(): Boolean {
        val expiresAt = context.dataStore.data.first()[EXPIRES_AT]
        return expiresAt == null || System.currentTimeMillis() > expiresAt
    }

    // Flow to observe authentication state
    val authState: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            val token = preferences[AUTH_TOKEN_KEY]
            val expiresAt = preferences[EXPIRES_AT] ?: 0
            val currentTime = System.currentTimeMillis()

            val isValid = token != null && expiresAt > 0 && currentTime < expiresAt

            if (DEBUG) {
                Log.d("SessionManager", "Token: $token")
                Log.d("SessionManager", "Expires At (Millis): $expiresAt")
                Log.d("SessionManager", "Current Time (Millis): $currentTime")
                Log.d("SessionManager", "Time Until Expiry (Seconds): ${(expiresAt - currentTime) / 1000}")
                Log.d("SessionManager", "Is Token Valid: $isValid")
            }

            isValid
        }

    // Individual getters
    val authToken: Flow<String?> = context.dataStore.data.map { it[AUTH_TOKEN_KEY] }
    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME_KEY] }
    val userId: Flow<Long?> = context.dataStore.data.map { it[USER_ID] }

    // Clear all session data
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        if (DEBUG) Log.d("SessionManager", "❌ Session cleared")
    }
}