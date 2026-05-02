package com.example.btl.Api

import com.example.btl.Model.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Body

interface UserApi {
    @GET("user/me")
    suspend fun getUserProfile(): Response<User>

    @PUT("user/me")
    suspend fun updateUserProfile(
        @Body updateRequest: UpdateUserRequest
    ): Response<User>

    @PUT("user/changePassword")
    suspend fun changePassword(
        @Body changePasswordRequest: ChangePasswordRequest
    ): Response<ChangePasswordResponse>
}

data class UpdateUserRequest(
    val username: String? = null,
    val phone: String? = null
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class ChangePasswordResponse(
    val message: String
)

