package com.example.btl.Repository

import android.content.Context
import com.example.btl.Api.RetrofitClient
import com.example.btl.Api.UpdateUserRequest
import com.example.btl.Api.ChangePasswordRequest
import com.example.btl.Api.ChangePasswordResponse
import com.example.btl.DataStore.DataStoreManager
import com.example.btl.Model.User

class UserRepository(private val context: Context) {

    suspend fun getCurrentUser(): Result<User> = try {
        val token = DataStoreManager.getToken(context)
        if (token == null) {
            Result.failure(Exception("No token found"))
        } else {
            val response = RetrofitClient.userApi.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch user profile: ${response.code()}"))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUserProfile(username: String?, phone: String?): Result<User> = try {
        val token = DataStoreManager.getToken(context)
        if (token == null) {
            Result.failure(Exception("No token found"))
        } else {
            val updateRequest = UpdateUserRequest(username = username, phone = phone)
            val response = RetrofitClient.userApi.updateUserProfile(updateRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update user profile: ${response.code()}"))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<String> = try {
        val token = DataStoreManager.getToken(context)
        if (token == null) {
            Result.failure(Exception("No token found"))
        } else {
            val changePasswordRequest = ChangePasswordRequest(oldPassword, newPassword)
            val response = RetrofitClient.userApi.changePassword(changePasswordRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception("Failed to change password: ${response.code()}"))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

