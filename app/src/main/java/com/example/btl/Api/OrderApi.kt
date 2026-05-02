package com.example.btl.Api

import com.example.btl.Model.CreateOrderRequest
import com.example.btl.Model.Order
import com.example.btl.Model.UpdateOrderRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface OrderApi {
    @GET("orders")
    suspend fun getOrders(): Response<List<Order>>

    @GET("orders/{id}")
    suspend fun getOrder(
        @Path("id") orderId: Int
    ): Response<Order>

    @POST("orders")
    suspend fun createOrder(
        @Body request: CreateOrderRequest
    ): Response<Order>

    @DELETE("orders/{id}")
    suspend fun cancelOrder(
        @Path("id") orderId: Int
    ): Response<Order>

    @PUT("orders/{id}")
    suspend fun updateOrder(
        @Path("id") orderId: Int,
        @Body request: UpdateOrderRequest
    ): Response<Order>
}

