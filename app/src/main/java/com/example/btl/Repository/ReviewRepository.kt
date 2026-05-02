package com.example.btl.Repository

import android.util.Log
import com.example.btl.Api.ReviewApi
import com.example.btl.Api.RetrofitClient
import com.example.btl.Model.Review
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

class ReviewRepository {
    private val reviewApi: ReviewApi = RetrofitClient.reviewApi
    private val TAG = "ReviewRepository"

    suspend fun getReviewsByProduct(productId: Int): Result<List<Review>> {
        val primaryQuery = mapOf("productId" to productId.toString())
        val fallbackQuery = mapOf("product_id" to productId.toString())

        val primary = fetchReviews(primaryQuery).map { reviews -> filterByProduct(reviews, productId) }
        if (primary.isSuccess && primary.getOrNull()?.isNotEmpty() == true) return primary

        val fallback = fetchReviews(fallbackQuery).map { reviews -> filterByProduct(reviews, productId) }
        if (fallback.isSuccess) return fallback

        return primary
    }

    suspend fun submitReview(
        productId: Int,
        customerId: Int?,
        orderItemId: Int?,
        rating: Double,
        comment: String,
        mediaUrl: String?
    ): Result<Review> {
        return try {
            val body = com.example.btl.Model.ReviewRequest(
                product_id = productId,
                customer_id = customerId,
                order_item_id = orderItemId,
                rating = rating,
                comment = comment,
                content = comment,
                media_url = mediaUrl
            )
            Log.d(TAG, "submitReview: productId=$productId customerId=$customerId orderItemId=$orderItemId rating=$rating")
            val response = reviewApi.createReview(body)
            Log.d(TAG, "submitReview: Response code=${response.code()}")

            if (!response.isSuccessful || response.body() == null) {
                Log.e(TAG, "submitReview: Error ${response.code()} - ${response.message()}")
                Result.failure(Exception("Error: ${response.code()}"))
            } else {
                val review = parseReview(response.body()!!) ?: Review(
                    product_id = productId,
                    customer_id = customerId,
                    rating = rating,
                    comment = comment
                )
                Result.success(review)
            }
        } catch (e: Exception) {
            Log.e(TAG, "submitReview: Exception ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchReviews(query: Map<String, String>): Result<List<Review>> {
        return try {
            Log.d(TAG, "fetchReviews: query=$query")
            val response = reviewApi.getReviews(query)
            Log.d(TAG, "fetchReviews: Response code=${response.code()}")

            if (!response.isSuccessful || response.body() == null) {
                Log.e(TAG, "fetchReviews: Error ${response.code()} - ${response.message()}")
                Result.failure(Exception("Error: ${response.code()}"))
            } else {
                val reviews = parseReviews(response.body()!!)
                Log.d(TAG, "fetchReviews: Parsed ${reviews.size} reviews")
                Result.success(reviews)
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchReviews: Exception ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun parseReviews(json: JsonElement): List<Review> {
        val items = mutableListOf<JsonObject>()

        when {
            json.isJsonArray -> json.asJsonArray.forEach { if (it.isJsonObject) items.add(it.asJsonObject) }
            json.isJsonObject -> {
                val root = json.asJsonObject
                val keys = listOf("data", "items", "results", "reviews")
                var extracted = false
                for (key in keys) {
                    val value = root.get(key) ?: continue
                    when {
                        value.isJsonArray -> {
                            value.asJsonArray.forEach { if (it.isJsonObject) items.add(it.asJsonObject) }
                            extracted = true
                            break
                        }
                        value.isJsonObject -> {
                            val nested = value.asJsonObject
                            val nestedArray = listOf("reviews", "items", "results")
                                .firstNotNullOfOrNull { nested.get(it) }
                            if (nestedArray != null && nestedArray.isJsonArray) {
                                nestedArray.asJsonArray.forEach { if (it.isJsonObject) items.add(it.asJsonObject) }
                                extracted = true
                                break
                            }
                        }
                    }
                }
                if (!extracted && root.has("review_id")) {
                    items.add(root)
                }
            }
        }

        return items.map { obj ->
            Review(
                review_id = safeInt(obj.get("review_id")),
                product_id = safeInt(obj.get("product_id")),
                customer_id = safeInt(obj.get("customer_id")),
                rating = safeDouble(obj.get("rating")),
                comment = safeString(obj.get("comment")) ?: safeString(obj.get("content")),
                createdAt = safeString(obj.get("created_at")),
                customerName = safeString(obj.get("customer_name")),
                userName = safeString(obj.get("user_name"))
            )
        }
    }

    private fun parseReview(json: JsonElement): Review? {
        val obj = when {
            json.isJsonObject -> {
                val root = json.asJsonObject
                when {
                    root.has("review_id") -> root
                    root.has("data") && root.get("data").isJsonObject -> root.getAsJsonObject("data")
                    root.has("result") && root.get("result").isJsonObject -> root.getAsJsonObject("result")
                    else -> null
                }
            }
            else -> null
        } ?: return null

        return Review(
            review_id = safeInt(obj.get("review_id")),
            product_id = safeInt(obj.get("product_id")),
            customer_id = safeInt(obj.get("customer_id")),
            rating = safeDouble(obj.get("rating")),
            comment = safeString(obj.get("comment")) ?: safeString(obj.get("content")),
            createdAt = safeString(obj.get("created_at")),
            customerName = safeString(obj.get("customer_name")),
            userName = safeString(obj.get("user_name"))
        )
    }

    private fun filterByProduct(reviews: List<Review>, productId: Int): List<Review> {
        val filtered = reviews.filter { it.product_id == productId }
        if (filtered.size != reviews.size) {
            Log.w(TAG, "filterByProduct: Removed ${reviews.size - filtered.size} reviews not matching product_id=$productId")
        }
        return filtered
    }

    private fun safeString(value: JsonElement?): String? {
        if (value == null || value.isJsonNull) return null
        if (!value.isJsonPrimitive) return null
        val primitive = value.asJsonPrimitive
        return if (primitive.isString) primitive.asString else primitive.toString()
    }

    private fun safeInt(value: JsonElement?): Int? {
        if (value == null || value.isJsonNull) return null
        if (!value.isJsonPrimitive) return null
        val primitive = value.asJsonPrimitive
        return when {
            primitive.isNumber -> primitive.asInt
            primitive.isString -> primitive.asString.toIntOrNull()
            else -> null
        }
    }

    private fun safeDouble(value: JsonElement?): Double? {
        if (value == null || value.isJsonNull) return null
        if (!value.isJsonPrimitive) return null
        val primitive = value.asJsonPrimitive
        return when {
            primitive.isNumber -> primitive.asDouble
            primitive.isString -> primitive.asString.toDoubleOrNull()
            else -> null
        }
    }
}
