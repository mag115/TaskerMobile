package com.taskermobile.data.session

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.taskermobile.data.model.Project
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val CURRENT_PROJECT_ID_KEY = longPreferencesKey("current_project_id")
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val ROLE_KEY = stringPreferencesKey("role") // 🔹 New: Store user role
        private val PROJECTS_KEY = stringPreferencesKey("projects") // 🔹 New: Store project list
        private val EXPIRES_AT = longPreferencesKey("expires_at")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val userId: Flow<Long?> = context.dataStore.data.map { it[USER_ID_KEY] }
    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME_KEY] }
    val role: Flow<String?> = context.dataStore.data.map { it[ROLE_KEY] }
    val currentProjectId: Flow<Long?> = context.dataStore.data.map { it[CURRENT_PROJECT_ID_KEY] }
    val authToken: Flow<String?> = context.dataStore.data.map { it[AUTH_TOKEN_KEY] }


    /** ✅ Save full login details, including role and projects */
    suspend fun saveLoginDetails(
        token: String,
        expiresIn: Long,
        userId: Long,
        username: String,
        role: String,
        projects: List<Long> // 🔹 Store project IDs
    ) {
        val expiresAt = System.currentTimeMillis() + expiresIn

        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            preferences[ROLE_KEY] = role
            preferences[EXPIRES_AT] = expiresAt
            preferences[PROJECTS_KEY] = projects.joinToString(",") // 🔹 Convert list to CSV
        }
    }

    /** ✅ Fetch stored projects */
    suspend fun getUserProjects(): List<Project> {
        val projectsString = context.dataStore.data.first()[PROJECTS_KEY] ?: return emptyList()
        return projectsString.split(",").mapNotNull {
            it.toLongOrNull()?.let { id -> Project(id, "Project $id", "", "", "") } // 🔹 Default names
        }
    }

    /** ✅ Save currently selected project */
    suspend fun saveCurrentProjectId(projectId: Long) {
        Log.d("SessionManager", "Saving project ID: $projectId")
        context.dataStore.edit { preferences ->
            preferences[CURRENT_PROJECT_ID_KEY] = projectId
        }
    }

    /** ✅ Retrieve selected project ID */
    suspend fun getSelectedProject(): Long? {
        return context.dataStore.data.first()[CURRENT_PROJECT_ID_KEY]
    }

    /** ✅ Check if token is expired */
    suspend fun isTokenExpired(): Boolean {
        val expiresAt = context.dataStore.data.first()[EXPIRES_AT]
        return expiresAt == null || System.currentTimeMillis() > expiresAt
    }

    /** ✅ Check if user is authenticated */
    val authState: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val token = preferences[AUTH_TOKEN_KEY]
        val expiresAt = preferences[EXPIRES_AT] ?: 0
        val isValid = token != null && expiresAt > 0 && System.currentTimeMillis() < expiresAt

        if (!isValid) {
            clearSession()
        }
        isValid
    }

    /** ✅ Clear session data */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
