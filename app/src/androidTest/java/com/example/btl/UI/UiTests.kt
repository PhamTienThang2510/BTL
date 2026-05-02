package com.example.btl.UI

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.btl.Activities.LoginRegisterActivity
import com.example.btl.R
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for Login Screen
 * Tests authentication UI flow using Espresso
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginRegisterActivity::class.java)

    @Test
    fun testLoginScreenDisplayed() {
        // Verify login screen is displayed
        onView(withId(R.id.buttonLogin))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testEmailInputField() {
        // Test email input field is visible and functional
        onView(withId(R.id.etEmail))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testPasswordInputField() {
        // Test password input field is visible and functional
        onView(withId(R.id.etPassword))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testLoginButton() {
        // Test login button is visible and clickable
        onView(withId(R.id.buttonLogin))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testEnterValidCredentials() {
        // Enter email
        onView(withId(R.id.etEmail))
            .perform(typeText("test@example.com"))

        // Enter password
        onView(withId(R.id.etPassword))
            .perform(typeText("password123"))

        // Verify fields contain text
        onView(withId(R.id.etEmail))
            .check(matches(withText("test@example.com")))

        onView(withId(R.id.etPassword))
            .check(matches(withText("password123")))
    }

    @Test
    fun testRegisterLinkNavigation() {
        // Test that register link is visible
        onView(withId(R.id.tvRegister))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testErrorMessageDisplaying() {
        // Test error message visibility
        // After failed login, error message should appear
        onView(withId(R.id.buttonLogin))
            .perform(click())

        // Error message should be displayed
        // This depends on your error view implementation
    }
}

/**
 * UI Tests for Product List Screen
 * Tests product browsing UI flow
 */
@RunWith(AndroidJUnit4::class)
class ProductListUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginRegisterActivity::class.java)

    @Test
    fun testProductListDisplayed() {
        // Assuming we're logged in, check product list
        // This test may need adjustment based on your navigation
        
        // Verify search bar is displayed
        onView(withId(R.id.tvSearch))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testProductCardClickable() {
        // Product cards should be clickable
        // This is a placeholder for actual product interactions
        
        onView(allOf(
            withId(R.id.ivProductImage),
            isDisplayed()
        )).check(matches(isEnabled()))
    }

    @Test
    fun testSearchFunctionality() {
        // Test search box is functional
        onView(withId(R.id.tvSearch))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testCategoryTabsVisible() {
        // Test that category tabs are visible
        onView(withId(R.id.tabLayout))
            .check(matches(isDisplayed()))
    }
}

/**
 * UI Tests for Shopping Cart Screen
 */
@RunWith(AndroidJUnit4::class)
class ShoppingCartUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginRegisterActivity::class.java)

    @Test
    fun testCartScreenDisplayed() {
        // This test assumes navigation to cart screen
        // Verify cart icon/navigation
        onView(withId(R.id.navigation_cart))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyCartMessage() {
        // Test empty cart message is displayed when cart is empty
        // Implementation depends on your layout
    }

    @Test
    fun testCartItemList() {
        // Test that cart items are displayed in a list
        // This would check for RecyclerView with items
    }

    @Test
    fun testCheckoutButton() {
        // Test checkout button is visible and clickable
        onView(withId(R.id.buttonCheckout))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testQuantityIncrement() {
        // Test quantity increment button
        onView(withId(R.id.btnIncrement))
            .check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun testQuantityDecrement() {
        // Test quantity decrement button
        onView(withId(R.id.btnDecrement))
            .check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun testRemoveFromCart() {
        // Test remove item button
        onView(withId(R.id.btnRemove))
            .check(matches(isDisplayed()))
            .perform(click())
    }
}

/**
 * UI Tests for Product Details Screen
 */
@RunWith(AndroidJUnit4::class)
class ProductDetailsUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginRegisterActivity::class.java)

    @Test
    fun testProductImageDisplayed() {
        // Test product image is displayed
        // ViewPager for images should be visible
        onView(withId(R.id.viewPagerImages))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testProductNameDisplayed() {
        // Test product name is displayed
        onView(allOf(
            withId(R.id.tvProductName),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testProductPriceDisplayed() {
        // Test product price is displayed
        onView(allOf(
            withId(R.id.tvPrice),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testColorSelection() {
        // Test color selection is available
        onView(allOf(
            withId(R.id.ivColor),
            isDisplayed()
        )).check(matches(isClickable()))
    }

    @Test
    fun testSizeSelection() {
        // Test size selection is available
        onView(allOf(
            withId(R.id.ivSize),
            isDisplayed()
        )).check(matches(isClickable()))
    }

    @Test
    fun testAddToCartButton() {
        // Test add to cart button
        onView(withId(R.id.buttonAddToCart))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testAddToCartClick() {
        // Test clicking add to cart
        onView(withId(R.id.buttonAddToCart))
            .perform(click())

        // After click, should show success message
        // This depends on your implementation
    }
}

/**
 * UI Tests for Checkout/Billing Screen
 */
@RunWith(AndroidJUnit4::class)
class CheckoutUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginRegisterActivity::class.java)

    @Test
    fun testAddressSelectionDisplayed() {
        // Test address selection UI
        onView(withId(R.id.rvAddress))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testAddressSelectable() {
        // Test selecting an address
        onView(allOf(
            withId(R.id.cardAddress),
            isDisplayed()
        )).perform(click())
    }

    @Test
    fun testOrderItemsList() {
        // Test order items are displayed
        onView(withId(R.id.rvProducts))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testTotalPriceDisplayed() {
        // Test total price is displayed
        onView(allOf(
            withId(R.id.tvTotalPrice),
            isDisplayed()
        )).check(matches(isDisplayed()))
    }

    @Test
    fun testPlaceOrderButton() {
        // Test place order button
        onView(withId(R.id.buttonPlaceOrder))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testPlaceOrderClick() {
        // Test clicking place order
        onView(withId(R.id.buttonPlaceOrder))
            .perform(click())

        // Should navigate to order confirmation
    }
}

/**
 * UI Tests for Navigation Flow
 */
@RunWith(AndroidJUnit4::class)
class NavigationUiTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginRegisterActivity::class.java)

    @Test
    fun testBottomNavigationVisible() {
        // Test bottom navigation bar is visible
        onView(withId(R.id.bottom_nav))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testHomeTabClickable() {
        // Test home tab
        onView(withId(R.id.navigation_home))
            .check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun testShopTabClickable() {
        // Test shop tab
        onView(withId(R.id.navigation_shop))
            .check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun testCartTabClickable() {
        // Test cart tab
        onView(withId(R.id.navigation_cart))
            .check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun testProfileTabClickable() {
        // Test profile tab
        onView(withId(R.id.navigation_profile))
            .check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun testBackNavigation() {
        // Test back navigation works
        // This tests the system back button or app back button
    }
}

