package com.example.btl.Integration

import com.example.btl.Api.AuthApi
import com.example.btl.Api.ProductApi
import com.example.btl.Api.CartApi
import com.example.btl.Model.LoginRequest
import com.example.btl.Model.User
import com.example.btl.Model.Product
import com.example.btl.Model.Cart
import com.example.btl.Model.CartItem
import com.example.btl.Repository.AuthRepository
import com.example.btl.Repository.ProductRepository
import com.example.btl.Repository.CartRepository
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Integration Tests with Real Network Layer
 * Uses MockWebServer to simulate backend API
 */
class AuthenticationIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var authApi: AuthApi
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(AuthApi::class.java)
        authRepository = AuthRepository(authApi, null!!)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testLoginWithValidCredentialsFlow() = runTest {
        // Arrange - Mock server response
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "user_id": 1,
                    "email": "user@example.com",
                    "username": "testuser",
                    "phone": "0123456789",
                    "token": "fake_jwt_token_123"
                }
            """.trimIndent())

        mockWebServer.enqueue(mockResponse)

        // Act - Make login request
        val loginRequest = LoginRequest("user@example.com", "password123")
        val response = authApi.login(loginRequest)

        // Assert - Verify response
        assert(response.isSuccessful)
        assert(response.body()?.email == "user@example.com")
        assert(response.body()?.token?.isNotEmpty() == true)

        // Verify request
        val request = mockWebServer.takeRequest()
        assert(request.path?.contains("/api/") == true)
    }

    @Test
    fun testLoginErrorResponse() = runTest {
        // Arrange - Mock error response
        val errorResponse = MockResponse()
            .setResponseCode(401)
            .setBody("""{"error": "Invalid credentials"}""")

        mockWebServer.enqueue(errorResponse)

        // Act
        val loginRequest = LoginRequest("invalid@example.com", "wrongpass")
        val response = authApi.login(loginRequest)

        // Assert
        assert(!response.isSuccessful)
        assert(response.code() == 401)
    }

    @Test
    fun testRegistrationFlow() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(201)
            .setBody("""
                {
                    "user_id": 2,
                    "email": "newuser@example.com",
                    "username": "newuser",
                    "phone": "0987654321",
                    "token": "new_token_456"
                }
            """.trimIndent())

        mockWebServer.enqueue(mockResponse)

        // Act
        val newUser = User(
            email = "newuser@example.com",
            username = "newuser",
            password = "SecurePass123",
            phone = "0987654321"
        )
        val response = authApi.register(newUser)

        // Assert
        assert(response.isSuccessful)
        assert(response.code() == 201)
        assert(response.body()?.username == "newuser")
    }
}

/**
 * Product API Integration Test
 */
class ProductApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var productApi: ProductApi
    private lateinit var productRepository: ProductRepository

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        productApi = retrofit.create(ProductApi::class.java)
        productRepository = ProductRepository(productApi)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testGetProductsFlow() = runTest {
        // Arrange - Mock server response
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                [
                    {
                        "product_id": 1,
                        "name": "Áo Polo",
                        "price": 250000,
                        "description": "Áo polo chất lượng cao",
                        "image_url": "https://example.com/image1.jpg",
                        "variants": []
                    },
                    {
                        "product_id": 2,
                        "name": "Giày Sneaker",
                        "price": 450000,
                        "description": "Giày thể thao",
                        "image_url": "https://example.com/image2.jpg",
                        "variants": []
                    }
                ]
            """.trimIndent())

        mockWebServer.enqueue(mockResponse)

        // Act
        val response = productApi.getProducts("bearer_token")

        // Assert
        assert(response.isSuccessful)
        assert(response.body()?.size == 2)
        assert(response.body()?.get(0)?.name == "Áo Polo")
        assert(response.body()?.get(1)?.price == 450000.0)
    }

    @Test
    fun testGetProductByIdFlow() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "product_id": 1,
                    "name": "Áo Polo",
                    "price": 250000,
                    "description": "Áo polo chất lượng cao",
                    "image_url": "https://example.com/image1.jpg",
                    "variants": [
                        {
                            "variant_id": 1,
                            "product_id": 1,
                            "color": "Đỏ",
                            "size": "M",
                            "quantity": 10
                        }
                    ]
                }
            """.trimIndent())

        mockWebServer.enqueue(mockResponse)

        // Act
        val response = productApi.getProductById(1, "bearer_token")

        // Assert
        assert(response.isSuccessful)
        assert(response.body()?.product_id == 1)
        assert(response.body()?.variants?.size == 1)
    }
}

/**
 * Cart API Integration Test
 */
class CartApiIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var cartApi: CartApi
    private lateinit var cartRepository: CartRepository

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        cartApi = retrofit.create(CartApi::class.java)
        cartRepository = CartRepository(cartApi)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testGetCartFlow() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                    "cart_id": 1,
                    "user_id": 1,
                    "cart_items": [
                        {
                            "cart_item_id": 1,
                            "cart_id": 1,
                            "variant_id": 1,
                            "quantity": 2,
                            "unit_price": 250000,
                            "color": "Đỏ",
                            "size": "M"
                        }
                    ],
                    "created_at": "2026-04-17"
                }
            """.trimIndent())

        mockWebServer.enqueue(mockResponse)

        // Act
        val response = cartApi.getCart("bearer_token")

        // Assert
        assert(response.isSuccessful)
        assert(response.body()?.cart_id == 1)
        assert(response.body()?.cart_items?.size == 1)
        assert(response.body()?.cart_items?.get(0)?.quantity == 2)
    }

    @Test
    fun testAddToCartFlow() = runTest {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(201)
            .setBody("""
                {
                    "cart_item_id": 1,
                    "cart_id": 1,
                    "variant_id": 1,
                    "quantity": 1,
                    "unit_price": 250000,
                    "color": "Đỏ",
                    "size": "M"
                }
            """.trimIndent())

        mockWebServer.enqueue(mockResponse)

        // Act
        val cartItem = CartItem(
            cart_item_id = 0,
            cart_id = 1,
            variant_id = 1,
            quantity = 1,
            unit_price = 250000.0,
            color = "Đỏ",
            size = "M"
        )

        val response = cartApi.addToCart(cartItem, "bearer_token")

        // Assert
        assert(response.isSuccessful)
        assert(response.code() == 201)
    }
}

/**
 * Complete Shopping Flow Integration Test
 */
class CompleteShoppingFlowIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var productApi: ProductApi
    private lateinit var cartApi: CartApi
    private lateinit var authApi: AuthApi

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApi = retrofit.create(AuthApi::class.java)
        productApi = retrofit.create(ProductApi::class.java)
        cartApi = retrofit.create(CartApi::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testCompleteShoppingFlowWithRealApiCalls() = runTest {
        // 1. User logs in
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                {"user_id": 1, "email": "user@example.com", "token": "token123"}
            """)
        )

        // 2. User browses products
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                [{"product_id": 1, "name": "Áo Polo", "price": 250000, "description": "Áo", "image_url": "url", "variants": []}]
            """)
        )

        // 3. User gets product details
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                {"product_id": 1, "name": "Áo Polo", "price": 250000, "description": "Áo", "image_url": "url", "variants": [{"variant_id": 1, "color": "Đỏ", "size": "M", "quantity": 10}]}
            """)
        )

        // 4. User gets cart
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody("""
                {"cart_id": 1, "user_id": 1, "cart_items": [], "created_at": "2026-04-17"}
            """)
        )

        // 5. User adds to cart
        mockWebServer.enqueue(
            MockResponse().setResponseCode(201).setBody("""
                {"cart_item_id": 1, "cart_id": 1, "variant_id": 1, "quantity": 1, "unit_price": 250000, "color": "Đỏ", "size": "M"}
            """)
        )

        // Execute all API calls
        val loginResp = authApi.login(LoginRequest("user@example.com", "pass"))
        assert(loginResp.isSuccessful)

        val productsResp = productApi.getProducts("token123")
        assert(productsResp.isSuccessful)
        assert(productsResp.body()?.size == 1)

        val productResp = productApi.getProductById(1, "token123")
        assert(productResp.isSuccessful)

        val cartResp = cartApi.getCart("token123")
        assert(cartResp.isSuccessful)

        val addResp = cartApi.addToCart(
            CartItem(0, 1, 1, 1, 250000.0, "Đỏ", "M"),
            "token123"
        )
        assert(addResp.isSuccessful)
    }
}

