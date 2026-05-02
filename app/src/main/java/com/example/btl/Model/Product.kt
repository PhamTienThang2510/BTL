package com.example.btl.Model

import com.google.gson.annotations.SerializedName

/**
 * Product Media - Cloudinary image URL
 */
data class ProductMedia(
    val media_id: Int,
    val product_id: Int,
    val media_url: String,  // Cloudinary URL (https://res.cloudinary.com/...)
    val media_type: String,
    val is_primary: Boolean = false
)

/**
 * Product Variant - Size/Color options with price
 */
data class ProductVariant(
    val variant_id: Int,
    val product_id: Int,
    val sku: String,
    val color: String,
    val size: String,
    val price: String,  // As string from backend
    val stock_quantity: Int,
    val image_url: String  // Local path: /uploads/...
)

/**
 * Product - Full structure with media & variants
 * Backend returns: product_name, product_media[], product_variants[]
 */
data class Product(
    val product_id: Int,
    @SerializedName("product_name")
    val name: String,  // Renamed from product_name for easier use
    val description: String,
    val seller_id: Int,
    val category_id: Int,
    val status: String = "active",
    @SerializedName("product_media")
    val media: List<ProductMedia> = emptyList(),
    @SerializedName("product_variants")
    val variants: List<ProductVariant> = emptyList(),

    // Legacy fields (for backward compatibility if needed)
    val price: Double? = null,
    val image_url: String? = null,
    
    // Optional fields for sorting and filtering
    val rating: Double? = null,
    val reviewCount: Int? = null,
    val salesCount: Int? = 0,
    val viewCount: Int? = 0
) {
    /**
     * Get primary image from media or variants
     * Priority: product_media.is_primary → product_variants.image_url → fallback ""
     */
    fun getPrimaryImageUrl(): String {
        // 1. First priority: primary media from Cloudinary
        val primaryMedia = media.firstOrNull { it.is_primary }
        if (primaryMedia != null) {
            return primaryMedia.media_url
        }

        // 2. Second priority: first media (if any)
        if (media.isNotEmpty()) {
            return media[0].media_url
        }

        // 3. Third priority: first variant image
        if (variants.isNotEmpty()) {
            return variants[0].image_url
        }

        // 4. Fallback: legacy image_url
        return image_url ?: ""
    }

    /**
     * Get primary price from variants or legacy field
     * Priority: product_variants[0].price → price field
     */
    fun getPrimaryPrice(): Double {
        // 1. First priority: first variant price (convert String to Double)
        if (variants.isNotEmpty()) {
            return try {
                variants[0].price.toDouble()
            } catch (_: Exception) {
                price ?: 0.0
            }
        }

        // 2. Fallback: legacy price field
        return price ?: 0.0
    }
}

data class Category(
    val category_id: Int,
    val name: String,
    val description: String,
    val thumbnail: String = "",
    val status: String = "active"
)

/**
 * Response từ GET /categories/{id} - Backend trả Category object có chứa nested products array
 * Dùng để parse response từ CategoryApi.getCategoryWithProducts()
 */
data class CategoryWithProductsResponse(
    val category_id: Int,
    val name: String,
    val description: String,
    val thumbnail: String = "",
    val status: String = "active",
    val products: List<Product> = emptyList()
)

