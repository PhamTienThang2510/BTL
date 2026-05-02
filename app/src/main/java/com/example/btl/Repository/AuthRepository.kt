package com.example.btl.Repository

import android.content.Context
import android.util.Log
import com.example.btl.Api.RetrofitClient
import com.example.btl.DataStore.DataStoreManager
import com.example.btl.Model.LoginRequest
import com.example.btl.Model.LoginResponse
import com.example.btl.Model.RegisterRequest
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val context: Context) {
    companion object {
        private const val TAG = "AuthRepository"
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.authApi.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                DataStoreManager.saveToken(context, loginResponse.access_token.trim())
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Đăng nhập thất bại: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.authApi.register(RegisterRequest(username, email, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                DataStoreManager.saveToken(context, loginResponse.access_token.trim())
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Đăng ký thất bại: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gửi yêu cầu đặt lại mật khẩu
     */
    suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            Log.d(TAG, "forgotPassword: Sending request for $email")
            val body = mapOf("email" to email)
            val response = RetrofitClient.authApi.forgotPassword(body)
            Log.d(TAG, "forgotPassword: Response code=${response.code()} isSuccessful=${response.isSuccessful}")
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "forgotPassword: Error body=${errorBody ?: "<empty>"}")
            }
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Gửi yêu cầu thất bại: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "forgotPassword: Exception ${e.message}", e)
            Result.failure(e)
        }
    }

    fun getToken(): Flow<String?> = DataStoreManager.getTokenFlow(context)
    suspend fun logout() = DataStoreManager.clearToken(context)
    suspend fun getStoredToken(): String? = DataStoreManager.getToken(context)
}
