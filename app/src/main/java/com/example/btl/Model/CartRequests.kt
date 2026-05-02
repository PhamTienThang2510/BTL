package com.example.btl.Model

import com.google.gson.annotations.SerializedName

// ===== Add To Cart Request (flat format for backend) =====
data class AddToCartRequest(
    @SerializedName("customer_id")
    val customer_id: Int? = null,  // ✅ Allow null for guest users
    
    @SerializedName("variant_id")
    val variant_id: Int,
    
    @SerializedName("render_id")
    val render_id: Int? = null  // ✅ Support mockup renders
)

// ===== Cart Item Request/Response =====
data class CartItemRequest(
    @SerializedName("variant_id")
    val variant_id: Int,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("color")
    val color: String? = null,
    
    @SerializedName("size")
    val size: String? = null
)

// ===== Create Cart Request =====
data class CreateCartRequest(
    @SerializedName("customer_id")
    val customer_id: Int? = null,
    
    @SerializedName("items")
    val items: List<CartItemRequest> = emptyList()
)

// ===== Update Cart Request =====
data class UpdateCartRequest(
    @SerializedName("cart_item_id")
    val cart_item_id: Int? = null,
    
    @SerializedName("quantity")
    val quantity: Int
)

// ===== Update Cart Items Request =====
data class UpdateCartItemsRequest(
    @SerializedName("items")
    val items: List<UpdateCartItemRequest>
)

data class UpdateCartItemRequest(
    @SerializedName("cart_item_id")
    val cart_item_id: Int,
    
    @SerializedName("quantity")
    val quantity: Int
)

