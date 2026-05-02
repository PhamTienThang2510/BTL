package com.example.btl.Api

import com.example.btl.Model.Product
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    @GET("products")
    suspend fun getAllProducts(
        @Header("Authorization") token: String
    ): Response<List<Product>>

    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") productId: Int,
        @Header("Authorization") token: String
    ): Response<Product>

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Header("Authorization") token: String
    ): Response<List<Product>>

    @GET("products/search")
    suspend fun searchProductsWithCategory(
        @Query("q") query: String,
        @Query("category") categoryId: Int,
        @Header("Authorization") token: String
    ): Response<List<Product>>

    @GET("products")
    suspend fun getProductsByCategory(
        @Query("category") categoryId: Int,
        @Header("Authorization") token: String
    ): Response<List<Product>>
}

