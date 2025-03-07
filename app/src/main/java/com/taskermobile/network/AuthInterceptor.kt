package com.taskermobile.network

import okhttp3.Interceptor
import okhttp3.Response
import com.taskermobile.data.session.SessionManager
import kotlinx.coroutines.runBlocking

class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        if (response.code == 401) {
            // Token is expired/invalid
            runBlocking {
                sessionManager.clearSession()
            }
        }
        
        return response
    }
} 