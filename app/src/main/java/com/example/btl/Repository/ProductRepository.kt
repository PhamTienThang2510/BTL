package com.example.btl.Repository

import android.util.Log
import com.example.btl.Api.CategoryApi
import com.example.btl.Api.ProductApi
import com.example.btl.Api.RetrofitClient
import com.example.btl.Model.Category
import com.example.btl.Model.Product

class ProductRepository {
    private val categoryApi: CategoryApi = RetrofitClient.categoryApi
    private val productApi: ProductApi = RetrofitClient.productApi
    private val TAG = "ProductRepository"

    suspend fun getAllCategories(token: String): Result<List<Category>> = try {
        Log.d(TAG, "getAllCategories: Calling API with token=${token.take(20)}...")
        val response = categoryApi.getAllCategories("Bearer $token")
        Log.d(TAG, "getAllCategories: Response code=${response.code()}")
        if (response.isSuccessful) {
            val categories = response.body() ?: emptyList()
            Log.d(TAG, "getAllCategories: ✅ Success - ${categories.size} categories")
            Result.success(categories)
        } else {
            Log.e(TAG, "getAllCategories: ❌ Error ${response.code()}")
            Result.failure(Exception("Error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "getAllCategories: ❌ Exception - ${e.message}", e)
        Result.failure(e)
    }

    suspend fun getAllProducts(token: String): Result<List<Product>> = try {
        Log.d(TAG, "getAllProducts: Calling API")
        val response = productApi.getAllProducts("Bearer $token")
        Log.d(TAG, "getAllProducts: Response code=${response.code()}")
        if (response.isSuccessful) {
            val products = response.body() ?: emptyList()
            Log.d(TAG, "getAllProducts: ✅ Success - ${products.size} products")
            Result.success(products)
        } else {
            Log.e(TAG, "getAllProducts: ❌ Error ${response.code()}")
            Result.failure(Exception("Error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "getAllProducts: ❌ Exception - ${e.message}", e)
        Result.failure(e)
    }

    suspend fun getProductsByCategoryId(categoryId: Int, token: String): Result<List<Product>> = try {
        Log.d(TAG, "")
        Log.d(TAG, "════════════════════════════════════════════════════════════")
        Log.d(TAG, "getProductsByCategoryId: START")
        Log.d(TAG, "  categoryId=$categoryId")
        Log.d(TAG, "  token=${token.take(30)}...${token.takeLast(10)}")
        Log.d(TAG, "════════════════════════════════════════════════════════════")

        // Try endpoint 1: GET /categories/{id}
        Log.d(TAG, "🔄 Try Endpoint 1: GET /categories/$categoryId")
        val response = categoryApi.getCategoryWithProducts(categoryId, "Bearer $token")
        Log.d(TAG, "  Response code: ${response.code()}")
        Log.d(TAG, "  Response message: ${response.message()}")

        if (response.isSuccessful) {
            Log.d(TAG, "✅ Response is successful (200)")

            // Extract products từ CategoryWithProductsResponse
            val categoryWithProducts = response.body()
            Log.d(TAG, "  categoryWithProducts object: ${categoryWithProducts != null}")

            if (categoryWithProducts != null) {
                Log.d(TAG, "  category_id: ${categoryWithProducts.category_id}")
                Log.d(TAG, "  name: ${categoryWithProducts.name}")
                Log.d(TAG, "  description: ${categoryWithProducts.description}")
                Log.d(TAG, "  products count: ${categoryWithProducts.products.size}")
            }

            val products = categoryWithProducts?.products ?: emptyList()

            Log.d(TAG, "")
            Log.d(TAG, "✅ getProductsByCategoryId SUCCESS - ${products.size} products")
            products.forEachIndexed { index, product ->
                Log.d(TAG, "  [$index] id=${product.product_id}, name=${product.name}")
                Log.d(TAG, "       price=${product.price}, category=${product.category_id}")
                Log.d(TAG, "       image_url=${product.image_url}")

                if (product.image_url.isNullOrEmpty()) {
                    Log.w(TAG, "       ⚠️ IMAGE_URL NULL/EMPTY!")
                } else if (product.image_url.startsWith("http")) {
                    Log.d(TAG, "       ✅ image_url valid (http)")
                } else {
                    Log.w(TAG, "       ⚠️ image_url invalid (not http): ${product.image_url}")
                }
            }
            Log.d(TAG, "════════════════════════════════════════════════════════════")
            Log.d(TAG, "")
            Result.success(products)

        } else if (response.code() == 404) {
            Log.d(TAG, "")
            Log.w(TAG, "⚠️ Response 404 - Endpoint /categories/{id} not found")
            Log.w(TAG, "🔄 Try Endpoint 2: GET /products?category=$categoryId")
            Log.d(TAG, "════════════════════════════════════════════════════════════")

            val fallbackResponse = productApi.getProductsByCategory(categoryId, "Bearer $token")
            Log.d(TAG, "  Fallback response code: ${fallbackResponse.code()}")
            Log.d(TAG, "  Fallback response message: ${fallbackResponse.message()}")

            if (fallbackResponse.isSuccessful) {
                val products = fallbackResponse.body() ?: emptyList()
                Log.d(TAG, "")
                Log.d(TAG, "✅ Fallback SUCCESS - ${products.size} products")
                products.forEachIndexed { index, product ->
                    Log.d(TAG, "  [$index] id=${product.product_id}, name=${product.name}")
                    Log.d(TAG, "       price=${product.price}, category=${product.category_id}")
                    Log.d(TAG, "       image_url=${product.image_url}")
                }
                Log.d(TAG, "════════════════════════════════════════════════════════════")
                Log.d(TAG, "")
                Result.success(products)
            } else {
                Log.e(TAG, "")
                Log.e(TAG, "❌ Fallback FAILED - Response code: ${fallbackResponse.code()}")
                Log.e(TAG, "❌ Response message: ${fallbackResponse.message()}")
                Log.e(TAG, "❌ Error body: ${fallbackResponse.errorBody()?.string()}")
                Log.e(TAG, "════════════════════════════════════════════════════════════")
                Log.e(TAG, "")
                Result.failure(Exception("Error: ${fallbackResponse.code()}"))
            }
        } else {
            Log.e(TAG, "")
            Log.e(TAG, "❌ Response FAILED - Code: ${response.code()}")
            Log.e(TAG, "❌ Message: ${response.message()}")
            Log.e(TAG, "❌ Error body: ${response.errorBody()?.string()}")
            Log.e(TAG, "════════════════════════════════════════════════════════════")
            Log.e(TAG, "")
            Result.failure(Exception("Error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "")
        Log.e(TAG, "❌ EXCEPTION in getProductsByCategoryId")
        Log.e(TAG, "  Exception type: ${e::class.simpleName}")
        Log.e(TAG, "  Message: ${e.message}")
        Log.e(TAG, "  Cause: ${e.cause}")
        Log.e(TAG, "════════════════════════════════════════════════════════════")
        Log.e(TAG, "", e)
        Result.failure(e)
    }

    suspend fun getProductsByCategory(categoryId: Int, token: String): Result<List<Product>> = try {
        Log.d(TAG, "getProductsByCategory: categoryId=$categoryId, token=${token.take(20)}...")
        val response = productApi.getProductsByCategory(categoryId, "Bearer $token")
        Log.d(TAG, "getProductsByCategory: Response code=${response.code()}")
        if (response.isSuccessful) {
            val products = response.body() ?: emptyList()
            Log.d(TAG, "getProductsByCategory: ✅ Success - ${products.size} products for category=$categoryId")
            products.forEach { product ->
                Log.d(TAG, "  Product: id=${product.product_id}, name=${product.name}, category=${product.category_id}")
            }
            Result.success(products)
        } else {
            Log.e(TAG, "getProductsByCategory: ❌ Error ${response.code()}")
            Result.failure(Exception("Error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "getProductsByCategory: ❌ Exception - ${e.message}", e)
        Result.failure(e)
    }

    private fun filterByQuery(products: List<Product>, query: String): List<Product> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return products.filter { product ->
            product.name.contains(q, ignoreCase = true) ||
                product.description.contains(q, ignoreCase = true)
        }
    }

    suspend fun searchProducts(query: String, categoryId: Int, token: String): Result<List<Product>> = try {
        Log.d(TAG, "searchProducts: query=$query, categoryId=$categoryId")
        val response = if (categoryId > 0) {
            Log.d(TAG, "searchProducts: Using category filter")
            productApi.searchProductsWithCategory(query, categoryId, "Bearer $token")
        } else {
            Log.d(TAG, "searchProducts: No category filter, using basic search")
            productApi.searchProducts(query, "Bearer $token")
        }
        Log.d(TAG, "searchProducts: Response code=${response.code()}")
        if (response.isSuccessful) {
            val products = response.body() ?: emptyList()
            Log.d(TAG, "searchProducts: ✅ Success - ${products.size} products")
            Result.success(products)
        } else if (response.code() == 400) {
            Log.w(TAG, "searchProducts: ⚠️ 400 from API, fallback to local search")
            getAllProducts(token).map { products ->
                val filtered = filterByQuery(products, query)
                if (categoryId > 0) {
                    filtered.filter { it.category_id == categoryId }
                } else {
                    filtered
                }
            }
        } else {
            Log.e(TAG, "searchProducts: ❌ Error ${response.code()}")
            Result.failure(Exception("Error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "searchProducts: ❌ Exception - ${e.message}", e)
        Result.failure(e)
    }

    suspend fun getProductById(productId: Int, token: String): Result<Product> = try {
        Log.d(TAG, "getProductById: productId=$productId, token=${token.take(20)}...")
        val response = productApi.getProductById(productId, "Bearer $token")
        Log.d(TAG, "getProductById: Response code=${response.code()}")
        if (response.isSuccessful) {
            val product = response.body()
            if (product != null) {
                Log.d(TAG, "getProductById: ✅ Success - ${product.name}")
                Log.d(TAG, "  Variants: ${product.variants.size}")
                Log.d(TAG, "  Media: ${product.media.size}")
                Result.success(product)
            } else {
                Log.e(TAG, "getProductById: ❌ Response body is null")
                Result.failure(Exception("Product not found"))
            }
        } else {
            Log.e(TAG, "getProductById: ❌ Error ${response.code()}")
            Result.failure(Exception("Error: ${response.code()}"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "getProductById: ❌ Exception - ${e.message}", e)
        Result.failure(e)
    }
}
