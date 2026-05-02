package com.example.btl.Api

import com.example.btl.Model.LoginRequest
import com.example.btl.Model.LoginResponse
import com.example.btl.Model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<LoginResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<Map<String, Any>>

    /**
     * Chức năng quên mật khẩu: Gửi email yêu cầu reset
     */
    @POST("user/forgotPassword")
    suspend fun forgotPassword(@Body body: Map<String, String>): Response<Unit>
}
