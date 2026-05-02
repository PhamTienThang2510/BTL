package com.example.btl.Api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class CreateRefundRequest(
    val order_id: Int,
    val reason: String
)

data class CreateRefundResponse(
    val message: String? = null
)

interface RefundApi {
    @POST("refunds")
    suspend fun createRefund(
        @Body request: CreateRefundRequest
    ): Response<CreateRefundResponse>
}
