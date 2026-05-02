package com.example.btl.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.btl.Model.Product
import com.example.btl.Repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryProductsViewModel(
    private val categoryId: Int
) : ViewModel() {
    private val repository = ProductRepository()
    private val TAG = "CategoryProductsViewModel"

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        Log.d(TAG, "ViewModel initialized with categoryId=$categoryId")
        if (categoryId <= 0) {
            Log.e(TAG, "⚠️ ERROR: categoryId=$categoryId is INVALID! Should be >= 1")
        }
    }

    fun loadProductsByCategory(token: String) {
        Log.d(TAG, "loadProductsByCategory called: categoryId=$categoryId, token=${token.take(20)}...")
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "loadProductsByCategory: _isLoading=true")
            Log.d(TAG, "loadProductsByCategory: Calling repository.getProductsByCategoryId(categoryId=$categoryId)")

            try {
                repository.getProductsByCategoryId(categoryId, token)
                    .onSuccess { productList ->
                        Log.d(TAG, "✅ API Success: Received ${productList.size} products")
                        if (productList.isEmpty()) {
                            Log.w(TAG, "⚠️ WARNING: Received 0 products for categoryId=$categoryId")
                            Log.w(TAG, "⚠️ Check: Database có data không? Category ID=$categoryId có exist không?")
                        }
                        productList.forEach { product ->
                            Log.d(TAG, "  ✅ Product: id=${product.product_id}, name=${product.name}")
                            Log.d(TAG, "     price=${product.price}, category=${product.category_id}")
                            Log.d(TAG, "     image_url=${product.image_url}")

                            // Check image URL validity
                            if (product.image_url.isNullOrEmpty()) {
                                Log.w(TAG, "     ⚠️ WARNING: image_url is NULL/EMPTY!")
                            } else if (!product.image_url.startsWith("http")) {
                                Log.w(TAG, "     ⚠️ WARNING: image_url not valid (doesn't start with http): ${product.image_url}")
                            } else {
                                Log.d(TAG, "     ✅ image_url looks valid: ${product.image_url.take(60)}...")
                            }
                        }
                        _products.value = productList
                        Log.d(TAG, "✅ _products.value updated with ${productList.size} products")
                        _error.value = null
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "❌ API Error: ${exception.message}", exception)
                        Log.e(TAG, "❌ Exception class: ${exception::class.simpleName}")
                        Log.e(TAG, "❌ Full stacktrace:", exception)
                        _error.value = exception.message ?: "Unknown error"
                        _products.value = emptyList()
                        Log.e(TAG, "❌ _products.value set to empty list due to error")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception in loadProductsByCategory: ${e.message}", e)
                Log.e(TAG, "❌ Exception class: ${e::class.simpleName}")
                _error.value = e.message ?: "Unknown error"
            }

            _isLoading.value = false
            Log.d(TAG, "loadProductsByCategory: _isLoading=false")
        }
    }

    /**
     * Factory for creating CategoryProductsViewModel instances with categoryId parameter
     */
    class Factory(private val categoryId: Int) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Log.d("CategoryProductsViewModel.Factory", "Creating ViewModel with categoryId=$categoryId")
            if (modelClass.isAssignableFrom(CategoryProductsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CategoryProductsViewModel(categoryId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

