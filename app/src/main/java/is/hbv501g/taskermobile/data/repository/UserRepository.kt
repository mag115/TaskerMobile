package `is`.hbv501g.taskermobile.data.repository

import `is`.hbv501g.taskermobile.data.api.UserService
import `is`.hbv501g.taskermobile.data.model.User
import retrofit2.HttpException

class UserRepository(   private val api: UserService) {
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val response = api.getAllUsers()
            if (response.isSuccessful) {
                response.body()?.let {
                    return Result.success(it)
                }
            }
            Result.failure(HttpException(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}