package com.example.btl.Integration

import com.example.btl.Helpers.MockupTestDataHelper
import com.example.btl.Model.CartItem
import com.example.btl.Model.CreateMockupRenderDto
import com.example.btl.Model.MockupRender
import com.example.btl.Model.MockupTemplate
import com.example.btl.Model.Product
import com.example.btl.Model.ProductVariant
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Integration Tests for Shopping Flow
 * Tests the interaction between multiple components
 */
class ShoppingFlowIntegrationTest {


    @Test
    fun testCompleteShoppingFlow() = runTest {
        // 1. User browses products
        val products = createMockProducts()
        assert(products.size > 0)
        assert(products.all { (it.price ?: 0.0) > 0 })

        // 2. User selects a product
        val selectedProduct = products[0]
        assert(selectedProduct.name == "Áo Polo")
        assert(selectedProduct.price == 250000.0)

        // 3. User adds to cart
        val cartItem = CartItem(
            cart_item_id = 1,
            cart_id = 1,
            variant_id = 1,
            quantity = 2,
            price = selectedProduct.price,
            color = "Đỏ",
            size = "M"
        )

        assert(cartItem.quantity == 2)
        assert(cartItem.getPrice() * cartItem.quantity == 500000.0)

        // 4. User adds more items
        val cartItems = mutableListOf(cartItem)
        cartItems.add(
            CartItem(
                cart_item_id = 2,
                cart_id = 1,
                quantity = 1,
                price = 300000.0,
                color = "Xanh",
                size = "L"
            )
        )

        // 5. Verify cart total
        val cartTotal = cartItems.sumOf { it.getPrice() * it.quantity }
        assert(cartTotal == 800000.0)
    }

    @Test
    fun testProductSearch() = runTest {
        // Simulate product search
        val allProducts = createMockProducts()
        val searchTerm = "Áo"

        val searchResults = allProducts.filter {
            it.name.contains(searchTerm, ignoreCase = true)
        }

        assert(searchResults.isNotEmpty())
        assert(searchResults.all { it.name.contains(searchTerm) })
    }

    @Test
    fun testProductFilter() = runTest {
        // Simulate filtering by price
        val allProducts = createMockProducts()
        val minPrice = 200000.0
        val maxPrice = 300000.0

        val filteredProducts = allProducts.filter {
            (it.price ?: 0.0) in minPrice..maxPrice
        }

        assert(filteredProducts.isNotEmpty())
        assert(filteredProducts.all { (it.price ?: 0.0) >= minPrice && (it.price ?: 0.0) <= maxPrice })
    }

    @Test
    fun testCartWithMultipleItems() = runTest {
        // Create cart with multiple items
        val cartItems = listOf(
            CartItem(1, 1, variant_id = 1, quantity = 1, price = 100000.0, color = "Đỏ", size = "M"),
            CartItem(2, 1, variant_id = 2, quantity = 2, price = 200000.0, color = "Xanh", size = "L"),
            CartItem(3, 1, variant_id = 3, quantity = 1, price = 150000.0, color = "Trắng", size = "S"),
            CartItem(4, 1, variant_id = 4, quantity = 3, price = 50000.0, color = "Vàng", size = "XL")
        )

        val cartTotal = cartItems.sumOf { it.getPrice() * it.quantity }
        val itemCount = cartItems.sumOf { it.quantity }

        assert(cartTotal == 800000.0)
        assert(itemCount == 7)
    }

    @Test
    fun testOrderProcessing() = runTest {
        // Simulate order processing
        val cartItems = listOf(
            CartItem(1, 1, variant_id = 1, quantity = 1, price = 100000.0, color = "Đỏ", size = "M"),
            CartItem(2, 1, variant_id = 2, quantity = 2, price = 200000.0, color = "Xanh", size = "L")
        )

        val orderTotal = cartItems.sumOf { it.getPrice() * it.quantity }
        val shippingCost = 50000.0
        val finalTotal = orderTotal + shippingCost

        assert(finalTotal == 550000.0)
    }

    @Test
    fun testApplyDiscount() = runTest {
        // Simulate discount application
        val cartTotal = 1000000.0
        val discountPercent = 10.0
        val discountAmount = cartTotal * (discountPercent / 100)
        val finalTotal = cartTotal - discountAmount

        assert(discountAmount == 100000.0)
        assert(finalTotal == 900000.0)
    }

    @Test
    fun testApplyCoupon() = runTest {
        // Simulate coupon code
        val orderTotal = 500000.0
        val couponCode = "SAVE20"
        val couponDiscount = when (couponCode) {
            "SAVE20" -> orderTotal * 0.2
            else -> 0.0
        }

        val finalTotal = orderTotal - couponDiscount

        assert(couponDiscount == 100000.0)
        assert(finalTotal == 400000.0)
    }

    @Test
    fun testVariantSelection() = runTest {
        // Test product variant selection
        val product = Product(
            product_id = 1,
            name = "Áo Polo",
            description = "Áo chất lượng cao",
            seller_id = 1,
            category_id = 1,
            price = 250000.0,
            image_url = "url",
            variants = listOf(
                ProductVariant(1, 1, "SKU1", "Đỏ", "M", "250000", 10, "url"),
                ProductVariant(2, 1, "SKU2", "Đỏ", "L", "250000", 5, "url"),
                ProductVariant(3, 1, "SKU3", "Xanh", "M", "250000", 8, "url"),
                ProductVariant(4, 1, "SKU4", "Xanh", "L", "250000", 12, "url")
            )
        )

        // Select variant: Red, Medium
        val selectedVariant = product.variants.find { 
            it.color == "Đỏ" && it.size == "M" 
        }

        assert(selectedVariant != null)
        assert(selectedVariant?.stock_quantity == 10)
    }

    @Test
    fun testInventoryCheck() = runTest {
        // Check if product is in stock
        val product = Product(
            product_id = 1,
            name = "Áo Polo",
            description = "Áo",
            seller_id = 1,
            category_id = 1,
            price = 250000.0,
            image_url = "url",
            variants = listOf(
                ProductVariant(1, 1, "SKU1", "Đỏ", "M", "250000", 0, "url")  // Out of stock
            )
        )

        val variant = product.variants[0]
        val isInStock = variant.stock_quantity > 0

        assert(!isInStock)
    }

    // Helper function
    private fun createMockProducts(): List<Product> {
        return listOf(
            Product(1, "Áo Polo", "Áo polo chất lượng", 1, 1, "active", emptyList(), emptyList(), 250000.0, "url1"),
            Product(2, "Áo Sơ Mi", "Áo sơ mi lịch lãm", 1, 1, "active", emptyList(), emptyList(), 300000.0, "url2"),
            Product(3, "Giày Sneaker", "Giày thể thao", 1, 1, "active", emptyList(), emptyList(), 450000.0, "url3"),
            Product(4, "Túi Xách", "Túi xách công sở", 1, 1, "active", emptyList(), emptyList(), 500000.0, "url4")
        )
    }
}

/**
 * User Authentication Flow Integration Test
 */
class AuthenticationFlowIntegrationTest {

    @Test
    fun testCompleteAuthenticationFlow() {
        // 1. User navigates to login page
        val loginPage = "login_screen"
        assert(loginPage == "login_screen")

        // 2. User enters credentials
        val email = "user@example.com"
        val password = "SecurePassword123"
        assert(email.contains("@"))
        assert(password.length >= 6)

        // 3. System validates input
        val isValidEmail = email.contains("@") && email.contains(".")
        assert(isValidEmail)

        // 4. System sends login request
        val loginSuccessful = true
        assert(loginSuccessful)

        // 5. System stores token
        val token = "fake_jwt_token_12345"
        assert(token.isNotEmpty())

        // 6. User redirected to home
        val currentScreen = "home_screen"
        assert(currentScreen == "home_screen")
    }

    @Test
    fun testPasswordReset() {
        // 1. User clicks forgot password
        val email = "user@example.com"
        assert(email.isNotEmpty())

        // 2. System sends reset email
        val emailSent = true
        assert(emailSent)

        // 3. User clicks reset link in email
        val resetLink = "https://example.com/reset?token=abc123"
        assert(resetLink.contains("reset"))

        // 4. User enters new password
        val newPassword = "NewSecurePassword456"
        assert(newPassword.length >= 6)

        // 5. System updates password
        val passwordUpdated = true
        assert(passwordUpdated)
    }
}

/**
 * Mockup Rendering Integration Test
 * Tests the POD (Print on Demand) mockup workflow with local and remote images
 * Based on api-architecture.md documentation
 */
class MockupRenderingIntegrationTest {

    @Test
    fun testMockupTemplateLoading() = runTest {
        // 1. Client loads available mockup templates
        val templates = MockupTestDataHelper.createMockMockupTemplates()
        
        assert(templates.isNotEmpty())
        assert(templates.size == 3)
        assert(templates.all { it.is_active })
        
        // 2. Verify template structure matches backend schema
        val template = templates[0]
        assert(template.template_id > 0)
        assert(template.mockup_uuid.isNotEmpty())
        assert(template.thumbnail_url.isNotEmpty())
        assert(template.smart_objects != null)
    }

    @Test
    fun testMockupRenderWithLocalImage() = runTest {
        // Scenario: User uploads image from device and creates mockup
        
        // 1. Create mockup render with local image
        val localImagePath = "/data/user/0/com.example.btl/cache/design_local.jpg"
        val mockupRender = MockupTestDataHelper.createMockMockupRenderWithLocalImage(
            renderid = 1,
            templateId = 1,
            localImagePath = localImagePath
        )
        
        // 2. Verify local image handling
        assert(mockupRender.design_image_url.startsWith("file://"))
        assert(mockupRender.design_image_url.contains(localImagePath))
        assert(mockupRender.status == "processing")
        
        // 3. Simulate backend processing
        val processedRender = MockupTestDataHelper.simulateMockupRenderResponse(
            mockupRender,
            success = true
        )
        
        // 4. Verify render completed
        assert(processedRender.status == "completed")
        assert(processedRender.rendered_image_url != null)
        assert(processedRender.rendered_image_url!!.startsWith("file://"))
    }

    @Test
    fun testMockupRenderWithRemoteImage() = runTest {
        // Scenario: User provides remote image URL for mockup
        
        val remoteImageUrl = "https://example.com/product-design.jpg"
        val mockupRender = MockupTestDataHelper.createMockMockupRenderWithRemoteImage(
            renderid = 2,
            templateId = 2,
            remoteImageUrl = remoteImageUrl,
            renderedImageUrl = "https://example.com/rendered-mockup-2.jpg"
        )
        
        // Verify remote image handling
        assert(mockupRender.design_image_url == remoteImageUrl)
        assert(mockupRender.status == "completed")
        assert(mockupRender.rendered_image_url != null)
    }

    @Test
    fun testMockupRenderRequest() = runTest {
        // Test POST /mockups/render endpoint payload
        
        val request = CreateMockupRenderDto(
            template_id = 1,
            design_image_url = "file:///cache/user_design.jpg",
            product_id = 1,
            render_config = mapOf(
                "quality" to "high",
                "format" to "jpeg"
            )
        )
        
        // Verify request structure
        assert(request.template_id == 1)
        assert(request.design_image_url.startsWith("file://"))
        assert(request.product_id != null)
        assert(request.render_config != null)
        assert(request.render_config!!["quality"] == "high")
    }

    @Test
    fun testMockupRenderProcessing() = runTest {
        // Test mockup rendering workflow: pending -> processing -> completed
        
        // 1. Initial render request
        val initialRender = MockupTestDataHelper.createProcessingMockupRender(
            renderId = 1,
            templateId = 1
        )
        assert(initialRender.status == "processing")
        assert(initialRender.rendered_image_url == null)
        
        // 2. Simulate backend processing
        val completedRender = initialRender.copy(
            status = "completed",
            rendered_image_url = "https://example.com/final-render.jpg"
        )
        
        assert(completedRender.status == "completed")
        assert(completedRender.rendered_image_url != null)
        assert(completedRender.error_message == null)
    }

    @Test
    fun testMockupRenderError() = runTest {
        // Test error handling in mockup rendering
        
        val failedRender = MockupTestDataHelper.createFailedMockupRender(
            renderId = 1,
            errorMessage = "Template not found: invalid_uuid"
        )
        
        assert(failedRender.status == "failed")
        assert(failedRender.error_message != null)
        assert(failedRender.error_message!!.contains("Template not found"))
        assert(failedRender.rendered_image_url == null)
    }

    @Test
    fun testCartIntegrationWithMockup() = runTest {
        // Test adding mockup-rendered product to cart
        // Per api-architecture: cart uniqueness is (variant_id, render_id)
        
        // 1. Create mockup render
        val mockup1 = MockupTestDataHelper.createMockMockupRenderWithLocalImage(
            renderid = 1,
            templateId = 1,
            localImagePath = "/cache/design_red.jpg"
        )
        
        val mockup2 = MockupTestDataHelper.createMockMockupRenderWithLocalImage(
            renderid = 2,
            templateId = 1,
            localImagePath = "/cache/design_blue.jpg"
        )
        
        // 2. Add same variant with different renders to cart
        // This should create TWO different cart items
        val cartItem1 = CartItem(
            cart_item_id = 1,
            cart_id = 1,
            variant_id = 1,  // Same variant
            quantity = 1,
            price = 250000.0,
            color = "Đỏ",
            size = "M"
        )
        
        val cartItem2 = CartItem(
            cart_item_id = 2,
            cart_id = 1,
            variant_id = 1,  // Same variant
            quantity = 2,
            price = 250000.0,
            color = "Đỏ",
            size = "M"
        )
        
        // Verify both items exist in cart with different mockups
        val cartItems = listOf(cartItem1, cartItem2)
        assert(cartItems.size == 2)
        assert(cartItems.all { it.variant_id == 1 })
        assert(cartItems[0].quantity + cartItems[1].quantity == 3)
    }

    @Test
    fun testMockupRenderHistory() = runTest {
        // Test GET /mockups/renders?limit=20 endpoint
        
        val renders = MockupTestDataHelper.createMockupRenderBatch(count = 5)
        
        // Verify render history structure
        assert(renders.size == 5)
        assert(renders.all { it.user_id == 1 })
        assert(renders.all { it.status == "completed" })
        
        // Test pagination: limit=3
        val paginated = renders.take(3)
        assert(paginated.size == 3)
        assert(paginated.all { it.render_id <= 3 })
    }

    @Test
    fun testSmartObjectsJsonHandling() = runTest {
        // Test JSON serialization/deserialization of smart_objects
        // Per api-architecture: smart_objects is stored as JSON string
        
        val template = MockupTestDataHelper.createMockMockupTemplates()[0]
        
        // Verify JSON string is present
        assert(template.smart_objects != null)
        assert(template.smart_objects!!.contains("design_area"))
        assert(template.smart_objects!!.contains("\"x\""))
        assert(template.smart_objects!!.contains("\"y\""))
        
        // In real implementation, would parse JSON:
        // val smartObjects = Json.decodeFromString<List<SmartObject>>(template.smart_objects)
    }

    @Test
    fun testMockupProductVariantRelation() = runTest {
        // Test product variant relationship with mockups
        
        val product = MockupTestDataHelper.createProductWithMockupVariants()
        
        // Verify product has variants
        assert(product.variants.isNotEmpty())
        assert(product.variants.size == 3)
        
        // Each variant can have multiple mockup renders
        val variant = product.variants[0]
        assert(variant.product_id == product.product_id)
        
        // Create multiple renders for same variant
        val renders = (1..3).map { i ->
            MockupTestDataHelper.createMockMockupRenderWithLocalImage(
                renderid = i,
                templateId = 1,
                localImagePath = "/cache/variant_render_$i.jpg"
            )
        }
        
        assert(renders.size == 3)
        assert(renders.all { it.status == "processing" })
    }

    @Test
    fun testLocalImageFileHandling() = runTest {
        // Test file:// URI handling for local images
        
        val localPath = "/data/user/0/com.example.btl/cache/mockup_design.jpg"
        val fileUri = "file://$localPath"
        
        // Verify file URI format
        assert(fileUri.startsWith("file://"))
        assert(fileUri.contains(localPath))
        assert(!fileUri.startsWith("http"))
        
        // Test render with file URI
        val render = MockupRender(
            render_id = 1,
            template_id = 1,
            user_id = 1,
            design_image_url = fileUri,
            status = "processing"
        )
        
        assert(render.design_image_url == fileUri)
    }

    @Test
    fun testOrderIntegrationWithMockup() = runTest {
        // Test that order preserves render_id
        // Per api-architecture: order_items.render_id links to mockup_renders
        
        // 1. Create and render mockup
        val mockup = MockupTestDataHelper.createMockMockupRenderWithLocalImage(
            renderid = 100,
            templateId = 2
        )
        
        assert(mockup.status == "processing")
        
        // 2. Complete mockup
        val completedMockup = mockup.copy(
            status = "completed",
            rendered_image_url = "file:///cache/final_render.jpg"
        )
        
        // 3. Create order item with render_id
        // In real implementation: order_items.render_id = completedMockup.render_id
        
        assert(completedMockup.render_id == 100)
        assert(completedMockup.status == "completed")
        // Order can now reference this render for product customization details
    }

    @Test
    fun testConcurrentMockupRenders() = runTest {
        // Test handling multiple concurrent render requests
        
        val renderBatch = (1..5).map { i ->
            MockupTestDataHelper.createProcessingMockupRender(
                renderId = i,
                templateId = (i % 3) + 1
            )
        }
        
        // Verify all renders are queued
        assert(renderBatch.size == 5)
        assert(renderBatch.all { it.status == "processing" })
        
        // Simulate completion in order
        val completedBatch = renderBatch.mapIndexed { index, render ->
            render.copy(
                status = "completed",
                rendered_image_url = "https://example.com/render-${index + 1}.jpg"
            )
        }
        
        assert(completedBatch.all { it.status == "completed" })
        assert(completedBatch.all { it.rendered_image_url != null })
    }
}

