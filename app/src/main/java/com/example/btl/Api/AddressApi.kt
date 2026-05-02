package com.example.btl.Api

import com.example.btl.Model.Address
import com.example.btl.Model.CreateAddressRequest
import com.example.btl.Model.UpdateAddressRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AddressApi {
    @GET("addresses")
    suspend fun getAddresses(): Response<List<Address>>

    @GET("addresses/{id}")
    suspend fun getAddress(
        @Path("id") addressId: Int
    ): Response<Address>

    @POST("addresses")
    suspend fun createAddress(
        @Body request: CreateAddressRequest
    ): Response<Address>

    @PATCH("addresses/{id}")
    suspend fun updateAddress(
        @Path("id") addressId: Int,
        @Body request: UpdateAddressRequest
    ): Response<Address>

    @DELETE("addresses/{id}")
    suspend fun deleteAddress(
        @Path("id") addressId: Int
    ): Response<Any>

    @PATCH("addresses/{id}/default")
    suspend fun setDefaultAddress(
        @Path("id") addressId: Int
    ): Response<Address>
}

