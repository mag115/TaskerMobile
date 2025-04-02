package com.taskermobile.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.taskermobile.data.model.Project
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val CURRENT_PROJECT_ID_KEY = longPreferencesKey("current_project_id")
        private val CURRENT_PROJECT_KEY =
            stringPreferencesKey("current_project") // New key for full project details
        private val ENCRYPTED_AUTH_TOKEN_KEY =
            stringPreferencesKey("encrypted_auth_token") // Encrypted token (Base64)
        private val AUTH_TOKEN_IV_KEY =
            stringPreferencesKey("auth_token_iv") // IV for token decryption (Base64)
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val ROLE_KEY = stringPreferencesKey("role")
        private val PROJECTS_KEY = stringPreferencesKey("projects")
        private val EXPIRES_AT = longPreferencesKey("expires_at")
        private val PROFILE_PIC_URI_KEY = stringPreferencesKey("profile_pic_uri")
        private val BIOMETRIC_ENABLED_KEY =
            booleanPreferencesKey("biometric_enabled") // New key for biometric preference
    }

    val token = context.dataStore.data.map { it[TOKEN_KEY] }
    val userId = context.dataStore.data.map { it[USER_ID_KEY] }
    val username = context.dataStore.data.map { it[USERNAME_KEY] }
    val role = context.dataStore.data.map { it[ROLE_KEY] }
    val currentProjectId: Flow<Long?> = context.dataStore.data.map { it[CURRENT_PROJECT_ID_KEY] }

    // Flows for encrypted token and IV (stored as Base64 strings)
    val encryptedAuthTokenFlow: Flow<String?> =
        context.dataStore.data.map { it[ENCRYPTED_AUTH_TOKEN_KEY] }
    val authTokenIvFlow: Flow<String?> = context.dataStore.data.map { it[AUTH_TOKEN_IV_KEY] }

    // Flow to observe if biometric login is enabled by the user
    val biometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map {
        it[BIOMETRIC_ENABLED_KEY] ?: false // Defaults to false if not set
    }

    // Suspend function to get the current biometric preference
    suspend fun isBiometricLoginEnabled(): Boolean {
        return context.dataStore.data.first()[BIOMETRIC_ENABLED_KEY] ?: false
    }

    // Function to update the biometric login preference
    suspend fun setBiometricLoginEnabled(enabled: Boolean) {
        context.dataStore.edit {
            it[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }

    // Save login metadata (token saved separately and encrypted)
    suspend fun saveLoginDetails(
        expiresIn: Long,
        userId: Long,
        username: String,
        role: String

    ) {
        Log.d("SessionManager", "saveLoginDetails: userId=$userId, username=$username, role=$role")
        val expiresAt =
            System.currentTimeMillis() + expiresIn * 1000 // Assuming expiresIn is in seconds
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[USERNAME_KEY] = username
            preferences[ROLE_KEY] = role
            preferences[EXPIRES_AT] = expiresAt
        }
    }

    suspend fun saveProfilePictureUri(uri: String, userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("profile_pic_uri_$userId")] = uri
        }
    }

    fun profilePictureUri(userId: Long): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey("profile_pic_uri_$userId")]
        }
    }

    val profilePictureUri: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[PROFILE_PIC_URI_KEY]
    }

    // Save encrypted token and IV (as Base64 strings)
    suspend fun saveEncryptedToken(encryptedToken: ByteArray, iv: ByteArray) {
        val encodedToken = Base64.encodeToString(encryptedToken, Base64.NO_WRAP)
        val encodedIv = Base64.encodeToString(iv, Base64.NO_WRAP)
        context.dataStore.edit { preferences ->
            preferences[ENCRYPTED_AUTH_TOKEN_KEY] = encodedToken
            preferences[AUTH_TOKEN_IV_KEY] = encodedIv
        }
    }
    suspend fun getEncryptedUserInfo(): EncryptedUserInfo? {
        val prefs = context.dataStore.data.first()
        val json = prefs[stringPreferencesKey("encrypted_user_info")] ?: return null
        return try {
            Gson().fromJson(json, EncryptedUserInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }
    data class EncryptedUserInfo(val userId: Long, val username: String, val role: String)
    private val ENCRYPTED_USER_INFO_KEY = stringPreferencesKey("encrypted_user_info")
    suspend fun saveEncryptedUserData(
        encryptedToken: ByteArray,
        iv: ByteArray,
        userId: Long,
        username: String,
        role: String
    ) {
        val encodedToken = Base64.encodeToString(encryptedToken, Base64.NO_WRAP)
        val encodedIv = Base64.encodeToString(iv, Base64.NO_WRAP)
        val userInfoJson = Gson().toJson(EncryptedUserInfo(userId, username, role))

        context.dataStore.edit { preferences ->
            preferences[ENCRYPTED_AUTH_TOKEN_KEY] = encodedToken
            preferences[AUTH_TOKEN_IV_KEY] = encodedIv
            preferences[ENCRYPTED_USER_INFO_KEY] = userInfoJson
        }
    }

    suspend fun getEncryptedTokenAndIv(): Pair<String?, String?> {
        val prefs = context.dataStore.data.first()
        val encodedToken = prefs[ENCRYPTED_AUTH_TOKEN_KEY]
        val encodedIv = prefs[AUTH_TOKEN_IV_KEY]
        return Pair(encodedToken, encodedIv)
    }

    suspend fun getEncryptedTokenCiphertext(): ByteArray? {
        val encodedToken = context.dataStore.data.first()[ENCRYPTED_AUTH_TOKEN_KEY] ?: return null
        return try {
            Base64.decode(encodedToken, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    suspend fun getEncryptedTokenIv(): ByteArray? {
        val encodedIv = context.dataStore.data.first()[AUTH_TOKEN_IV_KEY] ?: return null
        return try {
            Base64.decode(encodedIv, Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    suspend fun clearEncryptedToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(ENCRYPTED_AUTH_TOKEN_KEY)
            preferences.remove(AUTH_TOKEN_IV_KEY)
        }
    }

    // Save and retrieve current project by its ID
    suspend fun saveCurrentProjectId(projectId: Long) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_PROJECT_ID_KEY] = projectId
        }
    }

    suspend fun getSelectedProjectId(): Long? {
        return context.dataStore.data.first()[CURRENT_PROJECT_ID_KEY]
    }

    suspend fun saveCurrentProject(project: Project) {
        // Save full project as JSON
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

    // Simplified authState: just checks if the session *was* active recently based on expiry.
    // Actual token validity requires decryption attempt.
    val authState = context.dataStore.data.map { preferences ->
        val expiresAt = preferences[EXPIRES_AT] ?: 0
        expiresAt > System.currentTimeMillis()
    }

    // Clears most session data but preserves biometric preference and encrypted token/IV
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            val biometricEnabled = preferences[BIOMETRIC_ENABLED_KEY]
            val encryptedToken = preferences[ENCRYPTED_AUTH_TOKEN_KEY]
            val tokenIv = preferences[AUTH_TOKEN_IV_KEY]
            val encryptedUserInfo = preferences[ENCRYPTED_USER_INFO_KEY]

            preferences.clear()

            if (biometricEnabled != null) preferences[BIOMETRIC_ENABLED_KEY] = biometricEnabled
            if (encryptedToken != null) preferences[ENCRYPTED_AUTH_TOKEN_KEY] = encryptedToken
            if (tokenIv != null) preferences[AUTH_TOKEN_IV_KEY] = tokenIv
            if (encryptedUserInfo != null) preferences[ENCRYPTED_USER_INFO_KEY] = encryptedUserInfo  // <-- ADD THIS
        }
    }
    // Clears only the raw (unencrypted) token
    suspend fun clearToken() {
        context.dataStore.edit {
            it.remove(TOKEN_KEY)
        }
    }

    // Save the raw token (used for standard login)
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }
}
