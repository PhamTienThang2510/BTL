package com.example.btl.Api

import android.content.Context
import com.example.btl.DataStore.DataStoreManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            DataStoreManager.getToken(context)
        }

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }
}

