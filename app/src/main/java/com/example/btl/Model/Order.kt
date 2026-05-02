package com.example.btl.Model

import com.google.gson.annotations.SerializedName

data class Order(
    val order_id: Int? = null,
    val customer_id: Int? = null,
    val address_id: Int? = null,
    val order_status: String = "pending",
    val payment_status: String = "pending",
    @SerializedName("total_amount")
    val totalAmount: Double = 0.0,
    @SerializedName("order_items")
    val orderItems: List<OrderItem> = emptyList(),
    val created_at: String? = null,
    val updated_at: String? = null
)

data class OrderItem(
    val order_item_id: Int? = null,
    val order_id: Int? = null,
    val variant_id: Int,
    val quantity: Int = 1,
    @SerializedName("unit_price")
    val unitPrice: Double = 0.0,
    val created_at: String? = null,
    val updated_at: String? = null,
    val product_id: Int? = null,
    val product_name: String? = null,
    val image_url: String? = null,
    @SerializedName("render_id")
    val renderId: Int? = null,
    @SerializedName("product_variants")
    val productVariants: ProductVariantSummary? = null
)

data class CreateOrderRequest(
    val customer_id: Int? = null,
    val address_id: Int? = null,
    val items: List<OrderItemRequest> = emptyList(),
    @SerializedName("payment_method")
    val paymentMethod: String? = null,
    @SerializedName("total_amount")
    val totalAmount: Double? = null,
    @SerializedName("payment_status")
    val paymentStatus: String? = null,
    @SerializedName("order_status")
    val orderStatus: String? = null
)

data class OrderItemRequest(
    val variant_id: Int,
    val quantity: Int = 1,
    val unit_price: Double = 0.0,
    @SerializedName("render_id")
    val renderId: Int? = null
)

data class ProductVariantSummary(
    val variant_id: Int? = null,
    val product_id: Int? = null,
    val image_url: String? = null,
    @SerializedName("products")
    val product: ProductSummary? = null
) {
    data class ProductSummary(
        val product_id: Int? = null,
        @SerializedName("product_name")
        val product_name: String? = null
    )
}
