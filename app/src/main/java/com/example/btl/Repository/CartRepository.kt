package com.example.btl.Repository

import android.util.Log
import com.example.btl.Api.CartApi
import com.example.btl.Api.RetrofitClient
import com.example.btl.Model.AddToCartRequest
import com.example.btl.Model.Cart
import com.example.btl.Model.CartItemRequest
import com.example.btl.Model.CreateCartRequest
import com.example.btl.Model.UpdateCartItemRequest
import com.example.btl.Model.UpdateCartItemsRequest
import com.example.btl.Model.UpdateCartRequest

class CartRepository {
    private val cartApi: CartApi = RetrofitClient.cartApi
    private val TAG = "CartRepository"

    /**
     * Lấy cart của customer hiện tại
     */
    suspend fun getCart(customerId: Int, token: String): Result<Cart> = try {
        Log.d(TAG, "getCart: Calling API for customer $customerId")
        val response = cartApi.getCart(customerId, "Bearer $token")
        
        if (response.isSuccessful) {
            val cart = response.body() ?: Cart()
            Result.success(cart)
        } else {
            Result.failure(Exception("Error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Add to cart - Thêm 1 sản phẩm vào cart
     */
    suspend fun addToCart(
        customerId: Int?,
        variantId: Int,
        token: String,
        renderId: Int? = null
    ): Result<Cart> {
        return try {
            Log.d(TAG, "addToCart: Adding item - customer=$customerId, variant=$variantId")
            
            val request = AddToCartRequest(
                customer_id = customerId,
                variant_id = variantId,
                render_id = renderId
            )

            val response = cartApi.addToCart(request, "Bearer $token")
            if (response.isSuccessful) {
                if (customerId != null) {
                    val getResponse = cartApi.getCart(customerId, "Bearer $token")
                    if (getResponse.isSuccessful) {
                        Result.success(getResponse.body() ?: Cart())
                    } else {
                        Result.success(Cart(customer_id = customerId))
                    }
                } else {
                    Result.success(Cart(customer_id = null))
                }
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Tạo cart mới
     */
    suspend fun createCart(
        customerId: Int?,
        items: List<CartItemRequest>?,
        token: String
    ): Result<Cart> = try {
        val request = CreateCartRequest(customer_id = customerId, items = items ?: emptyList())
        val response = cartApi.createCart(request, "Bearer $token")
        if (response.isSuccessful) {
            Result.success(response.body() ?: Cart())
        } else {
            Result.failure(Exception("Error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Update quantity của 1 cart item
     */
    suspend fun updateCartItemQuantity(
        cartItemId: Int,
        quantity: Int,
        customerId: Int,
        token: String
    ): Result<Cart> = try {
        val request = UpdateCartRequest(cart_item_id = cartItemId, quantity = quantity)
        val response = cartApi.updateCartItem(cartItemId, request, "Bearer $token")
        
        if (response.isSuccessful) {
            val getResponse = cartApi.getCart(customerId, "Bearer $token")
            if (getResponse.isSuccessful) {
                Result.success(getResponse.body() ?: Cart())
            } else {
                Result.success(Cart(customer_id = customerId))
            }
        } else {
            Result.failure(Exception("Error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Xóa 1 item từ cart
     */
    suspend fun deleteCartItem(
        cartItemId: Int,
        customerId: Int,
        token: String
    ): Result<Cart> = try {
        val response = cartApi.deleteCartItem(cartItemId, "Bearer $token")
        if (response.isSuccessful) {
            val getResponse = cartApi.getCart(customerId, "Bearer $token")
            if (getResponse.isSuccessful) {
                Result.success(getResponse.body() ?: Cart())
            } else {
                Result.success(Cart(customer_id = customerId))
            }
        } else {
            Result.failure(Exception("Error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Xóa toàn bộ item khỏi cart
     */
    suspend fun clearCart(cartId: Int, customerId: Int?, token: String): Result<Cart> = try {
        val response = cartApi.clearCart(cartId, "Bearer $token")
        if (response.isSuccessful) {
            if (customerId != null) {
                val getResponse = cartApi.getCart(customerId, "Bearer $token")
                if (getResponse.isSuccessful) {
                    Result.success(getResponse.body() ?: Cart())
                } else {
                    Result.success(Cart(customer_id = customerId))
                }
            } else {
                Result.success(response.body() ?: Cart())
            }
        } else {
            Result.failure(Exception("Error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
