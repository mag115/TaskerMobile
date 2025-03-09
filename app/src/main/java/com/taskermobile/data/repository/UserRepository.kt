package com.taskermobile.data.repository

import com.taskermobile.data.service.UserService
import com.taskermobile.data.model.User
import com.taskermobile.data.local.dao.UserDao
import com.taskermobile.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import android.util.Log

class UserRepository(
    private val userService: UserService,
    private val userDao: UserDao
) {
    fun getLocalUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toUser() }
        }
    }

    suspend fun refreshUsers(): Result<List<User>> {
        return try {
            val response = userService.getAllUsers()
            if (response.isSuccessful) {
                response.body()?.let { users ->
                    val entities = users.map { UserEntity.fromUser(it).copy(isSynced = true) }
                    userDao.insertUsers(entities)
                    Result.success(users)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // suspend fun createUser(user: User): Result<User> {
    //     return try {
    //         val response = userService.createUser(user)
    //         if (response.isSuccessful) {
    //             response.body()?.let { createdUser ->
    //                 // Save to local database with isSynced = true
    //                 userDao.insertUser(UserEntity.fromUser(createdUser).copy(isSynced = true))
    //                 Result.success(createdUser)
    //             } ?: Result.failure(Exception("Response body is null"))
    //         } else {
    //             // Save to local database with isSynced = false
    //             userDao.insertUser(UserEntity.fromUser(user).copy(isSynced = false))
    //             Result.failure(HttpException(response))
    //         }
    //     } catch (e: Exception) {
    //         // Save to local database with isSynced = false
    //         userDao.insertUser(UserEntity.fromUser(user).copy(isSynced = false))
    //         Result.failure(e)
    //     }
    // }

    // Sync operations
    // suspend fun syncUnsyncedUsers() {
    //     val unsyncedUsers = userDao.getUnsyncedUsers()
    //     for (userEntity in unsyncedUsers) {
    //         try {
    //             val response = userService.createUser(userEntity.toUser())
    //             if (response.isSuccessful) {
    //                 response.body()?.let { user ->
    //                     userEntity.id?.let { id ->
    //                         userDao.markUserAsSynced(id)
    //                     }
    //                 }
    //             }
    //         } catch (e: Exception) {
    //             // Handle exception (retry later, notify user, etc.)
    //             Log.e("UserRepository", "Error syncing user: ${e.message}")
    //         }
    //     }
    // }
}