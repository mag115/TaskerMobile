package `is`.hbv501g.taskermobile.data.api

import android.util.Log
import `is`.hbv501g.taskermobile.data.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun <T> createService(serviceClass: Class<T>, sessionManager: SessionManager): T {
        val authInterceptor = Interceptor { chain ->
            val token = runBlocking { sessionManager.authToken.first() }
            val request = chain.request().newBuilder()
            token?.let { request.addHeader("Authorization", "Bearer $it") }
            chain.proceed(request.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(serviceClass)
    }
}