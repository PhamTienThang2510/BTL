package com.example.btl.Api

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface LocationApi {
    @GET
    suspend fun getByUrl(
        @Url url: String,
        @Header("Authorization") token: String? = null
    ): Response<JsonElement>
}

