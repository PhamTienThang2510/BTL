package com.example.btl.Model

import com.google.gson.annotations.SerializedName

data class Cart(
    val cart_id: Int? = null,
    val customer_id: Int? = null,
    @SerializedName("total_price")
    val total_price: Double = 0.0,
    @SerializedName("cart_items")
    val cart_items: List<CartItem> = emptyList(),
    val created_at: String? = null,
    val updated_at: String? = null
) {
    fun calculateTotal(): Double {
        return cart_items.sumOf { item ->
            val price = item.getPrice()
            (price * item.quantity).toDouble()
        }
    }

    fun getTotalQuantity(): Int {
        return cart_items.sumOf { it.quantity }
    }
}

/**
 * CartItem - Supports both old format (direct fields) and new format (nested product_variants)
 * Old format: cart_item_id, variant_id, quantity, price, product_name, color, size, image_url
 * New format: cart_item_id, quantity, product_variants { variant_id, price, color, size, image_url, products { product_name } }
 */
data class CartItem(
    val cart_item_id: Int? = null,
    val cart_id: Int? = null,
    val quantity: Int = 1,

    // Old format fields (flat structure)
    val product_id: Int? = null,
    val variant_id: Int? = null,
    val price: Double? = null,
    val product_name: String? = null,
    val color: String? = null,
    val size: String? = null,
    val image_url: String? = null,
    @SerializedName("render_id")
    val render_id: Int? = null,

    // New format field (nested structure from backend)
    @SerializedName("product_variants")
    val product_variants: ProductVariantWithProduct? = null,

    val created_at: String? = null,
    val updated_at: String? = null
) {
    /**
     * Get price - supports both formats
     * Try nested product_variants.price first, fallback to flat price field
     */
    fun getPrice(): Double {
        return product_variants?.price?.toDoubleOrNull() ?: price ?: 0.0
    }

    /**
     * Get variant ID - supports both formats
     */
    fun getVariantId(): Int {
        return product_variants?.variant_id ?: variant_id ?: 0
    }

    /**
     * Get product name - supports both formats
     */
    fun getProductName(): String {
        return product_variants?.products?.product_name ?: product_name ?: "Product"
    }

    /**
     * Get color - supports both formats
     */
    fun getColorValue(): String {
        return product_variants?.color ?: color ?: "N/A"
    }

    /**
     * Get size - supports both formats
     */
    fun getSizeValue(): String {
        return product_variants?.size ?: size ?: "N/A"
    }

    /**
     * Get image URL - supports both formats
     */
    fun getImageUrl(): String {
        return product_variants?.image_url ?: image_url ?: ""
    }
}

/**
 * ProductVariantWithProduct - Nested structure from backend
 * Part of cart_items when backend returns full Cart object
 */
data class ProductVariantWithProduct(
    val variant_id: Int,
    val product_id: Int? = null,
    @SerializedName("SKU")
    val sku: String? = null,
    val color: String,
    val size: String,
    val price: String,  // Can be String or need conversion
    val stock_quantity: Int? = null,
    val image_url: String,
    @SerializedName("products")
    val products: Product? = null
) {
    /**
     * Product nested in variant (minimal fields for cart)
     */
    data class Product(
        val product_id: Int? = null,
        @SerializedName("product_name")
        val product_name: String = "Product"
    )
}
