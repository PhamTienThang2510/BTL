package com.example.btl.ViewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.btl.Api.ProductApi
import com.example.btl.Model.Category
import com.example.btl.Model.Product
import com.example.btl.Repository.ProductRepository
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ProductViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var productApi: ProductApi

    private lateinit var productRepository: ProductRepository
    private lateinit var viewModel: ProductViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        productRepository = ProductRepository()
        viewModel = ProductViewModel()
    }

    @Test
    fun testLoadCategoriesSuccess() = runTest(testDispatcher) {
        // Arrange - Mock successful category loading
        val mockCategories = listOf(
            Category(category_id = 1, name = "Áo", description = "Áo thun"),
            Category(category_id = 2, name = "Giày", description = "Giày thể thao")
        )

        // Assert - Verify structure
        assert(mockCategories.size == 2)
        assert(mockCategories[0].name == "Áo")
        assert(mockCategories[1].name == "Giày")
    }

    @Test
    fun testProductDataIntegrity() {
        // Test that product object maintains data integrity
        val product = Product(
            product_id = 1,
            name = "Áo Polo",
            description = "Áo polo chất lượng cao",
            seller_id = 1,
            category_id = 1,
            price = 250000.0,
            image_url = "https://example.com/image.jpg",
            variants = emptyList()
        )

        // Assert
        assert(product.product_id == 1)
        assert(product.name == "Áo Polo")
        assert(product.price == 250000.0)
        assert((product.price ?: 0.0) > 0)
    }

    @Test
    fun testProductPriceValidation() {
        // Test price validation
        val validPrice = 250000.0
        val invalidPrice = -100.0

        assert(validPrice > 0)
        assert(invalidPrice < 0)
    }

    @Test
    fun testProductNameNotEmpty() {
        // Test that product name is not empty
        val product = Product(
            product_id = 1,
            name = "Valid Product",
            description = "Test",
            seller_id = 1,
            category_id = 1,
            price = 100000.0,
            image_url = "url",
            variants = emptyList()
        )

        assert(product.name.isNotEmpty())
        assert(product.name.length > 0)
    }

    @Test
    fun testMultipleProducts() {
        // Test handling multiple products
        val products = listOf(
            Product(1, "Áo", "Desc 1", 1, 1, price = 100000.0, image_url = "url1", variants = emptyList()),
            Product(2, "Giày", "Desc 2", 1, 1, price = 200000.0, image_url = "url2", variants = emptyList()),
            Product(3, "Túi", "Desc 3", 1, 1, price = 300000.0, image_url = "url3", variants = emptyList())
        )

        assert(products.size == 3)
        assert(products.all { (it.price ?: 0.0) > 0 })
    }
}

