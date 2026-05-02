package com.example.btl.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.btl.Api.RetrofitClient
import com.example.btl.Model.CartItemRequest
import com.example.btl.Model.Product
import com.example.btl.Repository.CartRepository
import com.example.btl.Repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for DetailProductFragment
 * Handles loading product details and managing variant data
 */
class DetailProductViewModel(private val productId: Int) : ViewModel() {
    private val repository = ProductRepository()
    private val cartRepository = CartRepository()
    private val TAG = "DetailProductViewModel"

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    private val _colors = MutableStateFlow<List<String>>(emptyList())
    val colors: StateFlow<List<String>> = _colors

    private val _sizes = MutableStateFlow<List<String>>(emptyList())
    val sizes: StateFlow<List<String>> = _sizes

    private val _imageUrls = MutableStateFlow<List<String>>(emptyList())
    val imageUrls: StateFlow<List<String>> = _imageUrls

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Track selected variant options
    private val _selectedColor = MutableStateFlow("")
    val selectedColor: StateFlow<String> = _selectedColor

    private val _selectedSize = MutableStateFlow("")
    val selectedSize: StateFlow<String> = _selectedSize

    // Track add to cart success
    private val _addToCartSuccess = MutableStateFlow(false)
    val addToCartSuccess: StateFlow<Boolean> = _addToCartSuccess

    // Store customerId for cart operations
    private var currentCustomerId: Int? = null

    init {
        Log.d(TAG, "ViewModel initialized with productId=$productId")
    }

    /**
     * Initialize ViewModel with user ID
     */
    fun init(customerId: Int?) {
        this.currentCustomerId = customerId
        Log.d(TAG, "init: ViewModel initialized with customerId=$customerId")
    }

    /**
     * Reset addToCartSuccess flag for next operation
     */
    fun resetAddToCartSuccess() {
        _addToCartSuccess.value = false
        Log.d(TAG, "resetAddToCartSuccess: Flag reset")
    }

    fun setSelectedColor(color: String) {
        Log.d(TAG, "setSelectedColor: $color")
        _selectedColor.value = color
    }

    fun setSelectedSize(size: String) {
        Log.d(TAG, "setSelectedSize: $size")
        _selectedSize.value = size
    }

    /**
     * Load product details from API
     * Extract colors, sizes, and images from variants
     */
    fun loadProductDetails(token: String) {
        Log.d(TAG, "loadProductDetails called with token=${token.take(20)}...")
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Log.d(TAG, "Calling API to get product with id=$productId")
                repository.getProductById(productId, token)
                    .onSuccess { product ->
                        Log.d(TAG, "✅ Product loaded: ${product.name}")
                        _product.value = product

                        // Extract colors from variants
                        val colorList = product.variants
                            .mapNotNull { it.color }
                            .distinct()
                        Log.d(TAG, "✅ Colors extracted: ${colorList.size} unique colors")
                        _colors.value = colorList

                        // Extract sizes from variants
                        val sizeList = product.variants
                            .mapNotNull { it.size }
                            .distinct()
                        Log.d(TAG, "✅ Sizes extracted: ${sizeList.size} unique sizes")
                        _sizes.value = sizeList

                        // Extract image URLs from variants
                        val imageList = product.variants
                            .mapNotNull { it.image_url }
                            .distinct()
                            .ifEmpty {
                                // If no variant images, use product media
                                product.media.map { it.media_url }
                            }
                        Log.d(TAG, "✅ Images extracted: ${imageList.size} images")
                        _imageUrls.value = imageList

                        Log.d(TAG, "Product details fully loaded")
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "❌ API Error: ${exception.message}", exception)
                        _error.value = exception.message ?: "Unknown error"
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception: ${e.message}", e)
                _error.value = e.message ?: "Unknown error"
            }

            _isLoading.value = false
        }
    }

    /**
     * Add item to cart with proper flat format
     * ✅ Supports guest users (customer_id = null)
     * ✅ Supports mockup renders (render_id)
     * Gọi cartRepository.addToCart() để gửi tới /carts/add endpoint
     */
    fun addToCart(
        variantId: Int,
        quantity: Int = 1,  // ✅ Default quantity
        color: String = "",
        size: String = "",
        token: String,
        renderId: Int? = null  // ✅ Support mockup renders
    ) {
        Log.d(TAG, "addToCart: Adding item - variantId=$variantId, qty=$quantity, customerId=$currentCustomerId, renderId=$renderId")
        
        // ✅ Allow guest users (customer_id can be null)
        Log.d(TAG, "addToCart: Processing add to cart (customer_id=$currentCustomerId, guest=${currentCustomerId == null})")
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Call cart repository - send to /carts/add endpoint
                cartRepository.addToCart(
                    customerId = currentCustomerId,  // ✅ Can be null
                    variantId = variantId,
                    token = token,
                    renderId = renderId              // ✅ Pass render_id
                ).onSuccess { cart ->
                    Log.d(TAG, "✅ addToCart: Item added successfully - cart has ${cart.cart_items.size} items")
                    _addToCartSuccess.value = true
                }.onFailure { exception ->
                    Log.e(TAG, "addToCart: ❌ Error - ${exception.message}")
                    _error.value = exception.message ?: "Failed to add to cart"
                }
            } catch (e: Exception) {
                Log.e(TAG, "addToCart: ❌ Exception - ${e.message}", e)
                _error.value = e.message ?: "Failed to add to cart"
            }

            _isLoading.value = false
        }
    }

    companion object {
        class Factory(private val productId: Int) : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                Log.d("DetailProductViewModel.Factory", "Creating ViewModel with productId=$productId")
                if (modelClass.isAssignableFrom(DetailProductViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return DetailProductViewModel(productId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

