package com.example.btl.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.btl.DataStore.DataStoreManager
import com.example.btl.Model.Product
import com.example.btl.Repository.ProductRepository
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private var productRepository: ProductRepository? = null
    private val TAG = "SearchViewModel"

    private val _originalSearchResults = MutableLiveData<List<Product>>(emptyList())
    
    private val _searchResults = MutableLiveData<List<Product>>(emptyList())
    val searchResults: LiveData<List<Product>> = _searchResults

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _lastSearchQueries = MutableLiveData<List<String>>(emptyList())
    val lastSearchQueries: LiveData<List<String>> = _lastSearchQueries

    private val _popularProducts = MutableLiveData<List<Product>>(emptyList())
    val popularProducts: LiveData<List<Product>> = _popularProducts

    private val _selectedCategoryId = MutableLiveData<Int?>(null)
    val selectedCategoryId: LiveData<Int?> = _selectedCategoryId

    private val _priceRange = MutableLiveData<Pair<Int, Int>?>(null)
    val priceRange: LiveData<Pair<Int, Int>?> = _priceRange

    private val _selectedColors = MutableLiveData<List<String>>(emptyList())
    val selectedColors: LiveData<List<String>> = _selectedColors

    private val _selectedLocations = MutableLiveData<List<String>>(emptyList())
    val selectedLocations: LiveData<List<String>> = _selectedLocations

    fun init() {
        if (productRepository == null) {
            productRepository = ProductRepository()
        }

        viewModelScope.launch {
            val history = DataStoreManager.getSearchHistory(getApplication())
            _lastSearchQueries.value = history
        }
    }

    /**
     * Xóa kết quả tìm kiếm hiện tại
     */
    fun clearSearchResults() {
        _originalSearchResults.value = emptyList()
        _searchResults.value = emptyList()
    }

    /**
     * Lọc theo địa điểm
     */
    fun setFilterLocations(locations: List<String>) {
        _selectedLocations.value = locations
        applyFilters()
    }

    fun searchProducts(query: String, token: String) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            clearSearchResults()
            return
        }

        // Save history immediately so failed searches still show up
        addToLastSearches(normalizedQuery)

        if (productRepository == null) {
            _errorMessage.value = "Search is not ready. Please try again."
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val categoryId = _selectedCategoryId.value ?: 0
                val result = productRepository?.searchProducts(normalizedQuery, categoryId, token)

                if (result != null) {
                    result.onSuccess { products ->
                        _originalSearchResults.value = products
                        applyFilters()
                        _isLoading.value = false
                    }
                    result.onFailure { exception ->
                        _errorMessage.value = exception.message
                        _isLoading.value = false
                    }
                } else {
                    _errorMessage.value = "Search failed. Please try again."
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun loadPopularProducts(token: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                productRepository?.getAllProducts(token)?.onSuccess { products ->
                    _popularProducts.value = products.take(10)
                    if (_originalSearchResults.value.isNullOrEmpty()) {
                        _originalSearchResults.value = products
                        applyFilters()
                    }
                    _isLoading.value = false
                }?.onFailure { _isLoading.value = false }
            } catch (e: Exception) { _isLoading.value = false }
        }
    }

    private fun addToLastSearches(query: String) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return

        val current = _lastSearchQueries.value?.toMutableList() ?: mutableListOf()
        val existingIndex = current.indexOfFirst { it.equals(normalizedQuery, ignoreCase = true) }
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        }

        current.add(0, normalizedQuery)
        if (current.size > 4) current.removeAt(current.size - 1)
        _lastSearchQueries.value = current
        viewModelScope.launch {
            DataStoreManager.saveSearchHistory(getApplication(), current)
        }
    }

    fun clearLastSearches() {
        _lastSearchQueries.value = emptyList()
        viewModelScope.launch {
            DataStoreManager.saveSearchHistory(getApplication(), emptyList())
        }
    }

    fun removeLastSearch(query: String) {
        val current = _lastSearchQueries.value?.toMutableList() ?: mutableListOf()
        current.remove(query)
        _lastSearchQueries.value = current
        viewModelScope.launch {
            DataStoreManager.saveSearchHistory(getApplication(), current)
        }
    }

    fun setFilterPriceRange(minPrice: Int, maxPrice: Int) {
        _priceRange.value = Pair(minPrice, maxPrice)
        applyFilters()
    }

    fun setFilterColors(colors: List<String>) {
        _selectedColors.value = colors
        applyFilters()
    }

    fun resetAllFilters() {
        _selectedCategoryId.value = null
        _priceRange.value = null
        _selectedColors.value = emptyList()
        _selectedLocations.value = emptyList()
        applyFilters()
    }

    private fun applyFilters() {
        var result = _originalSearchResults.value ?: emptyList()

        _selectedCategoryId.value?.let { id -> if (id > 0) result = result.filter { it.category_id == id } }
        _priceRange.value?.let { (min, max) ->
            result = result.filter { product ->
                val price = product.getPrimaryPrice()
                price >= min.toDouble() && price <= max.toDouble()
            }
        }
        _selectedColors.value?.let { colors ->
            if (colors.isNotEmpty()) {
                result = result.filter { product ->
                    product.variants.any { v -> colors.any { c -> c.equals(v.color, true) } }
                }
            }
        }

        _searchResults.value = result
    }
}
