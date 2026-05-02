package com.example.btl.Api

import com.example.btl.Model.Category
import com.example.btl.Model.CategoryWithProductsResponse
import com.example.btl.Model.Product
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface CategoryApi {
    @GET("categories")
    suspend fun getAllCategories(
        @Header("Authorization") token: String
    ): Response<List<Category>>

    /**
     * GET /categories/{id} trả về Category object có chứa nested products array
     * Response type: CategoryWithProductsResponse (chứa products list)
     */
    @GET("categories/{id}")
    suspend fun getCategoryWithProducts(
        @Path("id") categoryId: Int,
        @Header("Authorization") token: String
    ): Response<CategoryWithProductsResponse>
}

