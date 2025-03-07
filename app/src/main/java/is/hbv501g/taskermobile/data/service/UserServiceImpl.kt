package `is`.hbv501g.taskermobile.data.service

import `is`.hbv501g.taskermobile.data.api.UserService
import `is`.hbv501g.taskermobile.data.model.User
import retrofit2.HttpException

class UserServiceImpl(private val userService: UserService) {
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val response = userService.getAllUsers()
            if (response.isSuccessful) {
                response.body()?.let { users ->
                    Result.success(users)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add getUserById for fetching a single user.
    suspend fun getUserById(userId: Long): Result<User> {
        return try {
            val response = userService.getUserById(userId)
            if (response.isSuccessful) {
                response.body()?.let { user ->
                    Result.success(user)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}