package com.example.btl.Model

/**
 * DTO for creating mockup render request
 * Corresponds to backend create-mockup-render.dto.ts
 */
data class CreateMockupRenderDto(
    val template_id: Int,
    val design_image_url: String,  // Can be local file path or remote URL
    val product_id: Int? = null,
    val render_config: Map<String, Any>? = null  // Optional rendering options
)

/**
 * DTO for render response from backend
 */
data class MockupRenderResponseDto(
    val render_id: Int,
    val template_id: Int,
    val status: String,
    val design_image_url: String,
    val rendered_image_url: String? = null,
    val error_message: String? = null
)


