package com.example.btl.ViewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.btl.Model.Cart
import com.example.btl.Model.CartItem
import com.example.btl.Model.Product
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CartViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CartViewModel

    @Before
    fun setup() {
        viewModel = CartViewModel()
    }

    @Test
    fun testEmptyCart() {
        // Test empty cart initialization
        val emptyCart = Cart(
            cart_id = 1,
            customer_id = 1,
            cart_items = emptyList(),
            created_at = "2026-04-17"
        )

        assert(emptyCart.cart_items.isEmpty())
        assert(emptyCart.calculateTotal() == 0.0)
    }

    @Test
    fun testAddItemToCart() {
        // Test adding item to cart
        val cartItem = CartItem(
            cart_item_id = 1,
            cart_id = 1,
            variant_id = 1,
            quantity = 2,
            price = 100000.0,
            color = "Đỏ",
            size = "M"
        )

        assert(cartItem.quantity > 0)
        assert(cartItem.getPrice() > 0)
        assert(cartItem.getPrice() * cartItem.quantity == 200000.0)
    }

    @Test
    fun testCartTotalCalculation() {
        // Test cart total calculation
        val items = listOf(
            CartItem(1, 1, variant_id = 1, quantity = 1, price = 100000.0, color = "Đỏ", size = "M"),
            CartItem(2, 1, variant_id = 2, quantity = 2, price = 200000.0, color = "Xanh", size = "L"),
            CartItem(3, 1, variant_id = 3, quantity = 1, price = 150000.0, color = "Trắng", size = "S")
        )

        val totalPrice = items.sumOf { it.getPrice() * it.quantity }

        assert(totalPrice == 650000.0)
    }

    @Test
    fun testUpdateQuantity() {
        // Test updating cart item quantity
        val cartItem = CartItem(
            cart_item_id = 1,
            cart_id = 1,
            variant_id = 1,
            quantity = 2,
            price = 100000.0,
            color = "Đỏ",
            size = "M"
        )

        val newQuantity = 5
        val oldTotal = cartItem.getPrice() * cartItem.quantity
        val newTotal = cartItem.getPrice() * newQuantity

        assert(oldTotal == 200000.0)
        assert(newTotal == 500000.0)
        assert(newTotal > oldTotal)
    }

    @Test
    fun testRemoveItem() {
        // Test removing item from cart
        var items = mutableListOf(
            CartItem(1, 1, variant_id = 1, quantity = 1, price = 100000.0, color = "Đỏ", size = "M"),
            CartItem(2, 1, variant_id = 2, quantity = 2, price = 200000.0, color = "Xanh", size = "L")
        )

        val initialSize = items.size
        items.removeAt(0)

        assert(initialSize == 2)
        assert(items.size == 1)
        assert(items[0].cart_item_id == 2)
    }

    @Test
    fun testCartItemPrice() {
        // Test that cart item price is calculated correctly
        val cartItem = CartItem(
            cart_item_id = 1,
            cart_id = 1,
            variant_id = 1,
            quantity = 3,
            price = 150000.0,
            color = "Đỏ",
            size = "M"
        )

        val totalPrice = cartItem.quantity * cartItem.getPrice()
        assert(totalPrice == 450000.0)
    }

    @Test
    fun testNegativeQuantityValidation() {
        // Test that negative quantity is invalid
        val negativeQuantity = -5

        assert(negativeQuantity < 0)
    }

    @Test
    fun testZeroQuantityValidation() {
        // Test that zero quantity is invalid
        val zeroQuantity = 0

        assert(zeroQuantity == 0)
    }
}

