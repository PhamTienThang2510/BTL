package com.example.btl.Repository

import android.content.Context
import com.example.btl.Api.CreateRefundRequest
import com.example.btl.Api.RetrofitClient

class RefundRepository(private val context: Context) {

    suspend fun createRefund(orderId: Int, reason: String): Result<String> = try {
        val response = RetrofitClient.refundApi.createRefund(
            CreateRefundRequest(order_id = orderId, reason = reason)
        )

        if (response.isSuccessful) {
            Result.success(response.body()?.message ?: "Yêu cầu hoàn hàng đã được gửi")
        } else {
            Result.failure(Exception("Tạo yêu cầu hoàn hàng thất bại: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
