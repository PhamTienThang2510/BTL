package com.example.btl.Model

/**
 * Data class representing a Mockup Template
 * Maps to mockup_templates table from backend schema
 */
data class MockupTemplate(
    val template_id: Int,
    val mockup_uuid: String,
    val thumbnail_url: String,
    val smart_objects: String? = null,  // JSON string of smart objects
    val variant_id: Int? = null,
    val is_active: Boolean = true,
    val created_at: String? = null,
    val updated_at: String? = null
)

