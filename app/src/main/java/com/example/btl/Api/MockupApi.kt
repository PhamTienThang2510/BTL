package com.example.btl.Api

import com.example.btl.Model.CreateMockupRenderDto
import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * API Interface for Mockup rendering
 * Handles image upload and mockup creation
 */
interface MockupApi {

    @GET
    suspend fun getTemplates(
        @Url endpoint: String,
        @Query("variant_id") variantId: Int,
        @Query("active") active: Boolean = true
    ): Response<JsonElement>

    @POST
    suspend fun createRender(
        @Url endpoint: String,
        @Body request: CreateMockupRenderDto
    ): Response<JsonElement>

    @GET("mockups/templates")
    suspend fun getTemplatesFixed(
        @Query("variant_id") variantId: Int? = null
    ): Response<JsonElement>

    @POST("mockups/render")
    suspend fun createRenderFixed(
        @Body request: CreateMockupRenderDto
    ): Response<JsonElement>
}
