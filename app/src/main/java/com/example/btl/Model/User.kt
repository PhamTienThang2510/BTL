package com.example.btl.Model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token")
    val access_token: String,
    val user: User
)

data class User(
    val user_id: Int,
    val email: String,
    val username: String,
    val phone: String? = null,
    val role: String,
    val status: String,
    val created_at: String,
    val updated_at: String,
    val avatar: String? = null,
    @SerializedName("image_url")
    val image_url: String? = null
)

data class ApiErrorResponse(
    val statusCode: Int,
    val message: String,
    val error: String
)

