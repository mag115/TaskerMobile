package com.taskermobile.network

import android.app.Application
import android.util.Log
import com.taskermobile.TaskerApplication
import com.taskermobile.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val app: Application, private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val taskerApp = app as TaskerApplication
        val token = taskerApp.currentDecryptedAuthToken

        var request = chain.request()

        if (!token.isNullOrBlank()) {
            Log.d("AuthInterceptor", "Adding Authorization header from TaskerApplication.")
            request = request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            Log.d("AuthInterceptor", "No decrypted token found. Proceeding without Authorization header.")
        }

        // Proceed with the request
        val response = chain.proceed(request)

        if (response.code == 401) {
             Log.w("AuthInterceptor", "Received 401 Unauthorized. Clearing session.")
        }

        return response
    }
} 