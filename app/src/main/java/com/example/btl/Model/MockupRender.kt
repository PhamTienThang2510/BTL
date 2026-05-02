package com.example.btl.Model

/**
 * Data class representing a Mockup Render Result
 * Maps to mockup_renders table from backend schema
 */
data class MockupRender(
    val render_id: Int,
    val template_id: Int,
    val user_id: Int,
    val design_image_url: String,  // Local or remote URL
    val rendered_image_url: String? = null,  // Result after rendering
    val status: String = "pending",  // pending | processing | completed | failed
    val error_message: String? = null,
    val product_id: Int? = null,
    val created_at: String? = null,
    val updated_at: String? = null
) {
    fun isProcessing() = status == "processing"
    fun isCompleted() = status == "completed"
    fun isFailed() = status == "failed"
    fun isPending() = status == "pending"
}

