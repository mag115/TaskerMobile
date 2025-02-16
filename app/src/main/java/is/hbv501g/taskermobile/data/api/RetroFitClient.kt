package `is`.hbv501g.taskermobile.data.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Replace with your actual backend URL (for emulator use 10.0.2.2)
    private const val BASE_URL = "http://10.0.2.2:8080"

    // Optional: Use logging interceptor for debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Expose the API interface
    val authApiService: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    // **Backend health check**
    fun checkBackendConnection() {
        val request = Request.Builder()
            .url("$BASE_URL/auth") // Update to your health check endpoint
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    Log.d("RetrofitClient", "✅ Backend is connected! Response: ${response.body?.string()}")
                } else {
                    Log.e("RetrofitClient", "❌ Backend unreachable! Status code: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("RetrofitClient", "❌ Error connecting to backend: ${e.message}", e)
            }
        }.start()
    }
}
