package com.example.btl.ViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.btl.DataStore.DataStoreManager
import com.example.btl.Model.Cart
import com.example.btl.Model.CartItemRequest
import com.example.btl.Repository.CartRepository
import com.example.btl.Repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {
    private val cartRepository = CartRepository()
    private var userRepository: UserRepository? = null
    private val TAG = "CartViewModel"

    private val _cart = MutableStateFlow<Cart>(Cart())
    val cart: StateFlow<Cart> = _cart

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentCustomerId: Int? = null
    private var currentToken: String? = null

    /**
     * Initialize CartViewModel with context
     */
    fun init(context: Context) {
        userRepository = UserRepository(context)
        Log.d(TAG, "init: CartViewModel initialized with context")
    }

    /**
     * Load cart của customer hiện tại
     * Lấy token từ DataStore và user_id từ user profile
     */
    fun loadCart(context: Context) {
        viewModelScope.launch {
            Log.d(TAG, "loadCart: Starting to load cart")
            _isLoading.value = true
            _error.value = null

            try {
                // Lấy token từ DataStore
                currentToken = DataStoreManager.getToken(context)
                if (currentToken == null) {
                    Log.e(TAG, "loadCart: No token found")
                    _error.value = "No token found. Please login again."
                    _isLoading.value = false
                    return@launch
                }

                // Initialize UserRepository nếu chưa
                if (userRepository == null) {
                    userRepository = UserRepository(context)
                }

                // Lấy user profile để có customer_id
                userRepository?.getCurrentUser()
                    ?.onSuccess { user ->
                        currentCustomerId = user.user_id
                        Log.d(TAG, "loadCart: Got customer_id = $currentCustomerId")

                        // Bây giờ load cart
                        cartRepository.getCart(currentCustomerId!!, currentToken!!)
                            .onSuccess { cart ->
                                Log.d(TAG, "loadCart: ✅ Success - ${cart.cart_items.size} items")
                                _cart.value = cart
                            }
                            .onFailure { exception ->
                                Log.e(TAG, "loadCart: ❌ Error loading cart - ${exception.message}")
                                _error.value = exception.message ?: "Failed to load cart"
                            }
                    }
                    ?.onFailure { exception ->
                        Log.e(TAG, "loadCart: ❌ Error getting user profile - ${exception.message}")
                        _error.value = exception.message ?: "Failed to get user profile"
                    }
            } catch (e: Exception) {
                Log.e(TAG, "loadCart: ❌ Exception - ${e.message}", e)
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Tạo cart mới với items (POST)
     * Sau đó tự động GET cart để lấy dữ liệu mới nhất
     */
    fun createCart(
        customerId: Int? = null,
        items: List<CartItemRequest>? = null
    ) {
        viewModelScope.launch {
            Log.d(TAG, "createCart: Creating new cart (POST)")
            if (currentToken == null) {
                _error.value = "No token found"
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            cartRepository.createCart(customerId, items, currentToken!!)
                .onSuccess { cart ->
                    Log.d(TAG, "createCart: ✅ POST Success, Cart now has ${cart.cart_items.size} items")
                    _cart.value = cart
                    
                    // Auto load cart to ensure we have latest data
                    if (customerId != null) {
                        Log.d(TAG, "createCart: Auto-loading cart to sync data")
                        cartRepository.getCart(customerId, currentToken!!)
                            .onSuccess { updatedCart ->
                                Log.d(TAG, "createCart: ✅ GET Success - Synced ${updatedCart.cart_items.size} items")
                                _cart.value = updatedCart
                            }
                            .onFailure { exception ->
                                Log.w(TAG, "createCart: ⚠️ GET failed but POST succeeded - ${exception.message}")
                            }
                    }
                }
                .onFailure { exception ->
                    Log.e(TAG, "createCart: ❌ Error - ${exception.message}")
                    _error.value = exception.message ?: "Failed to create cart"
                }

            _isLoading.value = false
        }
    }

    /**
     * Update quantity của item (PUT /carts/item/{id})
     */
    fun updateItemQuantity(cartItemId: Int, newQuantity: Int) {
        viewModelScope.launch {
            Log.d(TAG, "updateItemQuantity: Updating item $cartItemId to quantity $newQuantity")
            if (currentToken == null || currentCustomerId == null) {
                _error.value = "No token or customer ID found"
                return@launch
            }

            _error.value = null

            if (newQuantity <= 0) {
                deleteItem(cartItemId)
                return@launch
            }

            cartRepository.updateCartItemQuantity(
                cartItemId = cartItemId,
                quantity = newQuantity,
                customerId = currentCustomerId!!,
                token = currentToken!!
            ).onSuccess { cart ->
                Log.d(TAG, "updateItemQuantity: ✅ Success - Cart updated with ${cart.cart_items.size} items")
                _cart.value = cart
            }.onFailure { exception ->
                Log.e(TAG, "updateItemQuantity: ❌ Error - ${exception.message}")
                _error.value = exception.message ?: "Failed to update quantity"
            }
        }
    }

    /**
     * Xóa item khỏi cart (DELETE /carts/item/{id})
     */
    fun deleteItem(cartItemId: Int) {
        viewModelScope.launch {
            Log.d(TAG, "deleteItem: Deleting item $cartItemId")
            if (currentToken == null || currentCustomerId == null) {
                _error.value = "No token or customer ID found"
                return@launch
            }

            _error.value = null

            cartRepository.deleteCartItem(
                cartItemId = cartItemId,
                customerId = currentCustomerId!!,
                token = currentToken!!
            ).onSuccess { updatedCart ->
                Log.d(TAG, "deleteItem: ✅ Success - Cart has ${updatedCart.cart_items.size} items")
                _cart.value = updatedCart
            }.onFailure { exception ->
                Log.e(TAG, "deleteItem: ❌ Error - ${exception.message}")
                _error.value = exception.message ?: "Failed to delete item"
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Get total price
     */
    fun getTotalPrice(): Double {
        return _cart.value.calculateTotal()
    }

    /**
     * Get total items
     */
    fun getTotalItems(): Int {
        return _cart.value.getTotalQuantity()
    }

    /**
     * Check if cart is empty
     */
    fun isCartEmpty(): Boolean {
        return _cart.value.cart_items.isEmpty()
    }
}

