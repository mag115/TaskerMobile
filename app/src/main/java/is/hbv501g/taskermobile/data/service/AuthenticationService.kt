package `is`.hbv501g.taskermobile.data.service

import `is`.hbv501g.taskermobile.data.api.AuthApiService
import `is`.hbv501g.taskermobile.data.model.LoginRequest
import `is`.hbv501g.taskermobile.data.model.LoginResponse
import `is`.hbv501g.taskermobile.data.model.SignupRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthenticationService(
    private val authApiService: AuthApiService
) {
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                authApiService.login(LoginRequest(email, password))
            }
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // Ensure token is not empty
                    if (loginResponse.token.isNotEmpty()) {
                        Result.success(loginResponse)
                    } else {
                        Result.failure(Exception("Empty token"))
                    }
                } ?: Result.failure(Exception("Empty login response"))
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
                response.body()?.let { signupResponse ->
                    Result.success(signupResponse)
                } ?: Result.failure(Exception("Empty signup response"))
            } else {
                Result.failure(Exception("Signup failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.localizedMessage}"))
        }
    }
}