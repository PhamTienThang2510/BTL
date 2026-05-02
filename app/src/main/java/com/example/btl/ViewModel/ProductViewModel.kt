package com.example.btl.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.btl.Model.Category
import com.example.btl.Model.Product
import com.example.btl.Repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadCategories(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllCategories(token)
                .onSuccess { categoryList ->
                    _categories.value = categoryList
                    _error.value = null
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Unknown error"
                }
            _isLoading.value = false
        }
    }

    fun loadProductsByCategory(categoryId: Int, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getProductsByCategory(categoryId, token)
                .onSuccess { productList ->
                    _products.value = productList
                    _error.value = null
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Unknown error"
                }
            _isLoading.value = false
        }
    }

    fun loadAllProducts(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllProducts(token)
                .onSuccess { productList ->
                    _products.value = productList
                    _error.value = null
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Unknown error"
                }
            _isLoading.value = false
        }
    }
}

