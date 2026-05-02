package com.example.btl.Model

import com.google.gson.annotations.SerializedName

/**
 * Review model (fields are nullable to handle inconsistent backend payloads)
 */
data class Review(
    val review_id: Int? = null,
    val product_id: Int? = null,
    val customer_id: Int? = null,
    val rating: Double? = null,
    val comment: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("customer_name")
    val customerName: String? = null,
    @SerializedName("user_name")
    val userName: String? = null
)

