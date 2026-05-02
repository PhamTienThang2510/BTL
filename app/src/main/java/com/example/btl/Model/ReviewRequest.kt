package com.example.btl.Model

data class ReviewRequest(
    val product_id: Int,
    val customer_id: Int? = null,
    val order_item_id: Int? = null,
    val rating: Double,
    val comment: String? = null,
    val content: String? = null,
    val media_url: String? = null
)
