package com.example.btl.Helpers

import com.example.btl.Model.CartItem
import com.example.btl.Model.MockupRender
import com.example.btl.Model.MockupTemplate
import com.example.btl.Model.Product
import com.example.btl.Model.ProductVariant

/**
 * Helper class for creating mock data for Mockup integration testing
 * Maps to backend mockup_templates and mockup_renders structures
 */
object MockupTestDataHelper {
    
    /**
     * Create mock mockup templates (simulating backend mockup_templates table)
     */
    fun createMockMockupTemplates(): List<MockupTemplate> {
        return listOf(
            MockupTemplate(
                template_id = 1,
                mockup_uuid = "uuid-template-1",
                thumbnail_url = "https://example.com/template1-thumb.jpg",
                smart_objects = """[{"name": "design_area", "x": 0, "y": 0, "width": 500, "height": 500}]""",
                variant_id = 1,
                is_active = true
            ),
            MockupTemplate(
                template_id = 2,
                mockup_uuid = "uuid-template-2",
                thumbnail_url = "https://example.com/template2-thumb.jpg",
                smart_objects = """[{"name": "front", "x": 50, "y": 50, "width": 450, "height": 450}]""",
                variant_id = 2,
                is_active = true
            ),
            MockupTemplate(
                template_id = 3,
                mockup_uuid = "uuid-template-3",
                thumbnail_url = "https://example.com/template3-thumb.jpg",
                smart_objects = """[{"name": "logo_area", "x": 10, "y": 10, "width": 100, "height": 100}]""",
                variant_id = 3,
                is_active = true
            )
        )
    }
    
    /**
     * Create mock mockup render with local image URL
     */
    fun createMockMockupRenderWithLocalImage(
        renderid: Int = 1,
        templateId: Int = 1,
        userId: Int = 1,
        localImagePath: String = "/cache/design_image.jpg",
        renderedImagePath: String? = null
    ): MockupRender {
        return MockupRender(
            render_id = renderid,
            template_id = templateId,
            user_id = userId,
            design_image_url = "file://$localImagePath",  // Local file URI
            rendered_image_url = renderedImagePath?.let { "file://$it" },
            status = if (renderedImagePath != null) "completed" else "processing",
            product_id = 1
        )
    }
    
    /**
     * Create mock mockup render with remote image URL
     */
    fun createMockMockupRenderWithRemoteImage(
        renderid: Int = 1,
        templateId: Int = 1,
        userId: Int = 1,
        remoteImageUrl: String = "https://example.com/design.jpg",
        renderedImageUrl: String? = null
    ): MockupRender {
        return MockupRender(
            render_id = renderid,
            template_id = templateId,
            user_id = userId,
            design_image_url = remoteImageUrl,
            rendered_image_url = renderedImageUrl ?: "https://example.com/rendered-$renderid.jpg",
            status = "completed",
            product_id = 1
        )
    }
    
    /**
     * Create mockup render in processing state
     */
    fun createProcessingMockupRender(
        renderId: Int = 1,
        templateId: Int = 1,
        imageUrl: String = "https://example.com/design.jpg"
    ): MockupRender {
        return MockupRender(
            render_id = renderId,
            template_id = templateId,
            user_id = 1,
            design_image_url = imageUrl,
            status = "processing"
        )
    }
    
    /**
     * Create failed mockup render
     */
    fun createFailedMockupRender(
        renderId: Int = 1,
        templateId: Int = 1,
        errorMessage: String = "Failed to connect to Sudomock API"
    ): MockupRender {
        return MockupRender(
            render_id = renderId,
            template_id = templateId,
            user_id = 1,
            design_image_url = "https://example.com/design.jpg",
            status = "failed",
            error_message = errorMessage
        )
    }
    
    /**
     * Create cart item with mockup render
     */
    fun createCartItemWithMockup(
        cartItemId: Int = 1,
        renderId: Int = 1,
        quantity: Int = 1,
        price: Double = 250000.0
    ): CartItem {
        return CartItem(
            cart_item_id = cartItemId,
            cart_id = 1,
            variant_id = 1,
            quantity = quantity,
            price = price,
            color = "Đỏ",
            size = "M"
            // render_id = renderId  // Add if CartItem supports render_id field
        )
    }
    
    /**
     * Create product with mockup-compatible variants
     */
    fun createProductWithMockupVariants(): Product {
        return Product(
            product_id = 1,
            name = "Áo Polo POD",
            description = "Áo polo có thể custom design",
            seller_id = 1,
            category_id = 1,
            status = "active",
            media = emptyList(),
            price = 250000.0,
            image_url = "https://example.com/polo.jpg",
            variants = listOf(
                ProductVariant(1, 1, "SKU1", "Đỏ", "M", "250000", 10, "https://example.com/red-m.jpg"),
                ProductVariant(2, 1, "SKU2", "Đỏ", "L", "250000", 5, "https://example.com/red-l.jpg"),
                ProductVariant(3, 1, "SKU3", "Xanh", "M", "250000", 8, "https://example.com/blue-m.jpg")
            )
        )
    }
    
    /**
     * Create batch of mockup renders for testing workflow
     */
    fun createMockupRenderBatch(count: Int = 3): List<MockupRender> {
        return (1..count).map { i ->
            MockupRender(
                render_id = i,
                template_id = (i % 3) + 1,
                user_id = 1,
                design_image_url = "file:///cache/design_$i.jpg",
                rendered_image_url = "file:///cache/rendered_$i.jpg",
                status = "completed",
                product_id = 1
            )
        }
    }
    
    /**
     * Simulate API response for mockup render
     */
    fun simulateMockupRenderResponse(
        inputRender: MockupRender,
        success: Boolean = true
    ): MockupRender {
        return if (success) {
            inputRender.copy(
                status = "completed",
                rendered_image_url = inputRender.design_image_url.replace("design", "rendered")
            )
        } else {
            inputRender.copy(
                status = "failed",
                error_message = "Sudomock API error: Invalid template UUID"
            )
        }
    }
}

