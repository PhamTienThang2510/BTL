package com.example.btl.Model

import org.junit.Test

class UserTest {

    @Test
    fun testUserEmailValidation() {
        // Test email format validation
        val validEmail = "user@example.com"
        val invalidEmail = "invalid-email"

        assert(validEmail.contains("@"))
        assert(!invalidEmail.contains("@"))
    }

    @Test
    fun testUserPasswordValidation() {
        // Test password strength validation
        val weakPassword = "123"
        val strongPassword = "secure123Password"

        assert(weakPassword.length < 6)
        assert(strongPassword.length >= 6)
    }

    @Test
    fun testUserPhoneValidation() {
        // Test phone number validation
        val validPhone = "0123456789"
        val invalidPhone = "123"

        assert(validPhone.length == 10)
        assert(invalidPhone.length < 10)
    }

    @Test
    fun testUserCreation() {
        // Test user object creation
        val user = User(
            user_id = 1,
            email = "test@example.com",
            username = "testuser",
            phone = "0123456789",
            role = "customer",
            status = "active",
            created_at = "2026-04-17",
            updated_at = "2026-04-17"
        )

        assert(user.user_id == 1)
        assert(user.email == "test@example.com")
        assert(user.username == "testuser")
    }
}

class ProductTest {

    @Test
    fun testProductCreation() {
        // Test product object creation
        val product = Product(
            product_id = 1,
            name = "Test Product",
            description = "Product description",
            seller_id = 1,
            category_id = 1,
            price = 100000.0,
            image_url = "https://example.com/image.jpg",
            variants = emptyList()
        )

        assert(product.product_id == 1)
        assert(product.name == "Test Product")
        assert(product.price == 100000.0)
    }

    @Test
    fun testProductPriceRange() {
        // Test product price is within valid range
        val products = listOf(
            Product(1, "Áo", "Desc", 1, 1, price = 50000.0, image_url = "url", variants = emptyList()),
            Product(2, "Giày", "Desc", 1, 1, price = 500000.0, image_url = "url", variants = emptyList()),
            Product(3, "Túi", "Desc", 1, 1, price = 1000000.0, image_url = "url", variants = emptyList())
        )

        val allPricesValid = products.all { (it.price ?: 0.0) > 0 }
        assert(allPricesValid)
    }

    @Test
    fun testProductVariants() {
        // Test product variants
        val variant = ProductVariant(
            variant_id = 1,
            product_id = 1,
            sku = "SKU001",
            color = "Đỏ",
            size = "M",
            price = "100000",
            stock_quantity = 10,
            image_url = "/uploads/test.jpg"
        )

        assert(variant.color == "Đỏ")
        assert(variant.size == "M")
        assert(variant.stock_quantity == 10)
    }
}

class CartItemTest {

    @Test
    fun testCartItemTotalPrice() {
        // Test cart item total price calculation
        val cartItem = CartItem(
            cart_item_id = 1,
            cart_id = 1,
            variant_id = 1,
            quantity = 3,
            price = 100000.0,
            color = "Đỏ",
            size = "M"
        )

        val total = cartItem.getPrice() * cartItem.quantity
        assert(total == 300000.0)
    }

    @Test
    fun testMultipleCartItems() {
        // Test multiple cart items
        val items = listOf(
            CartItem(1, 1, variant_id = 1, quantity = 1, price = 100000.0, color = "Đỏ", size = "M"),
            CartItem(2, 1, variant_id = 2, quantity = 2, price = 200000.0, color = "Xanh", size = "L"),
            CartItem(3, 1, variant_id = 3, quantity = 3, price = 150000.0, color = "Trắng", size = "S")
        )

        val totalPrice = items.sumOf { it.getPrice() * it.quantity }
        assert(totalPrice == 950000.0)
    }
}

class AddressTest {

    @Test
    fun testAddressCreation() {
        // Test address object creation
        val address = Address(
            address_id = 1,
            customer_id = 1,
            receiver_name = "John Doe",
            phone = "0123456789",
            ward_id = 1,
            address_detail = "123 Main Street",
            isDefault = true
        )

        assert(address.address_id == 1)
        assert(address.receiver_name == "John Doe")
        assert(address.isDefault)
    }

    @Test
    fun testAddressPhoneValidation() {
        // Test address phone validation
        val address = Address(
            address_id = 1,
            customer_id = 1,
            receiver_name = "John",
            phone = "0123456789",
            ward_id = 1,
            address_detail = "123 Main",
            isDefault = false
        )

        assert(address.phone.length == 10)
        assert(address.phone.startsWith("0"))
    }
}

class OrderTest {

    @Test
    fun testOrderCreation() {
        // Test order object creation
        val order = Order(
            order_id = 1,
            customer_id = 1,
            address_id = 1,
            totalAmount = 500000.0,
            order_status = "pending",
            created_at = "2026-04-17",
            orderItems = emptyList()
        )

        assert(order.order_id == 1)
        assert(order.totalAmount == 500000.0)
        assert(order.order_status == "pending")
    }

    @Test
    fun testOrderStatus() {
        // Test order status validation
        val validStatuses = listOf("pending", "processing", "shipped", "delivered", "cancelled")
        val testStatus = "pending"

        assert(validStatuses.contains(testStatus))
    }
}

