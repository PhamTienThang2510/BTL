package com.example.btl.Api

import com.example.btl.Model.AddToCartRequest
import com.example.btl.Model.Cart
import com.example.btl.Model.CartItem
import com.example.btl.Model.CreateCartRequest
import com.example.btl.Model.UpdateCartItemsRequest
import com.example.btl.Model.UpdateCartRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CartApi {
    /**
     * GET /carts/:id - Lấy cart của customer theo customer_id
     */
    @GET("carts/{customerId}")
    suspend fun getCart(
        @Path("customerId") customerId: Int,
        @Header("Authorization") token: String
    ): Response<Cart>

     /**
      * POST /carts/add - Thêm item vào cart
      * ✅ FIX: Dùng Response<Unit> vì backend trả về body rỗng (empty body)
      */
     @POST("carts/add")
     suspend fun addToCart(
         @Body request: AddToCartRequest,
         @Header("Authorization") token: String
     ): Response<Unit>

    /**
     * POST /carts - Tạo cart mới
     */
    @POST("carts")
    suspend fun createCart(
        @Body request: CreateCartRequest,
        @Header("Authorization") token: String
    ): Response<Cart>

    /**
     * PUT /carts/:id - Update cart
     */
    @PUT("carts/{id}")
    suspend fun updateCart(
        @Path("id") cartId: Int,
        @Body request: UpdateCartItemsRequest,
        @Header("Authorization") token: String
    ): Response<Cart>

    /**
     * PUT /carts/item/:id - Update quantity của 1 cart item
     */
    @PUT("carts/item/{id}")
    suspend fun updateCartItem(
        @Path("id") cartItemId: Int,
        @Body request: UpdateCartRequest,
        @Header("Authorization") token: String
    ): Response<CartItem>

    /**
     * DELETE /carts/item/:id - Xóa 1 item từ cart
     */
    @DELETE("carts/item/{id}")
    suspend fun deleteCartItem(
        @Path("id") cartItemId: Int,
        @Header("Authorization") token: String
    ): Response<Cart>

    /**
     * DELETE /carts/item - Xóa toàn bộ item khỏi cart
     */
    @DELETE("carts/item")
    suspend fun clearCart(
        @Query("cart_id") cartId: Int,
        @Header("Authorization") token: String
    ): Response<Cart>
}
