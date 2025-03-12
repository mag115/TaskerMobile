package com.taskermobile.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.taskermobile.data.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val CURRENT_PROJECT_ID_KEY = longPreferencesKey("current_project_id")
        private val CURRENT_PROJECT_KEY = stringPreferencesKey("current_project") // New key for full project details
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val ROLE_KEY = stringPreferencesKey("role")
        private val PROJECTS_KEY = stringPreferencesKey("projects")
        private val EXPIRES_AT = longPreferencesKey("expires_at")
    }

    val token = context.dataStore.data.map { it[TOKEN_KEY] }
    val userId = context.dataStore.data.map { it[USER_ID_KEY] }
    val username = context.dataStore.data.map { it[USERNAME_KEY] }
    val role = context.dataStore.data.map { it[ROLE_KEY] }
    val currentProjectId: Flow<Long?> = context.dataStore.data.map { it[CURRENT_PROJECT_ID_KEY] }
    val authToken = context.dataStore.data.map { it[AUTH_TOKEN_KEY] }

    // Save login details...
    suspend fun saveLoginDetails(
        token: String,
        expiresIn: Long,
        userId: Long,
        username: String,
        role: String
    ) {
        val expiresAt = System.currentTimeMillis() + expiresIn
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            preferences[ROLE_KEY] = role
            preferences[EXPIRES_AT] = expiresAt
        }
    }

    // Save and retrieve current project by its ID (if needed)
    suspend fun saveCurrentProjectId(projectId: Long) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_PROJECT_ID_KEY] = projectId
        }
    }

    suspend fun getSelectedProjectId(): Long? {
        return context.dataStore.data.first()[CURRENT_PROJECT_ID_KEY]
    }

    suspend fun saveCurrentProject(project: Project) {
        // Save full project as JSON if needed
        val gson = Gson()
        val json = gson.toJson(project)
        context.dataStore.edit { preferences ->
            preferences[CURRENT_PROJECT_KEY] = json
            preferences[CURRENT_PROJECT_ID_KEY] = project.id ?: 0L
        }
    }

    // New: Retrieve the current Project object
    suspend fun getCurrentProject(): Project? {
        val json = context.dataStore.data.first()[CURRENT_PROJECT_KEY] ?: return null
        return Gson().fromJson(json, Project::class.java)
    }

    // (Other methods remain unchanged...)
    suspend fun saveUserInfo(userId: Long, username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun getUserProjects(): List<Project> {
        val projectsString = context.dataStore.data.first()[PROJECTS_KEY] ?: return emptyList()
        return projectsString.split(",").mapNotNull {
            it.toLongOrNull()?.let { id -> Project(id, "Project $id", "", "", "") }
        }
    }

    suspend fun isTokenExpired(): Boolean {
        val expiresAt = context.dataStore.data.first()[EXPIRES_AT]
        return expiresAt == null || System.currentTimeMillis() > expiresAt
    }

    val authState = context.dataStore.data.map { preferences ->
        val token = preferences[AUTH_TOKEN_KEY]
        val expiresAt = preferences[EXPIRES_AT] ?: 0
        token != null && expiresAt > System.currentTimeMillis()
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
