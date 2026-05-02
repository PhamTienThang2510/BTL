package com.example.btl.Api

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface ReviewApi {
    @GET("reviews")
    suspend fun getReviews(
        @QueryMap query: Map<String, String>
    ): Response<JsonElement>

    @POST("reviews")
    suspend fun createReview(
        @Body body: com.example.btl.Model.ReviewRequest
    ): Response<JsonElement>
}
