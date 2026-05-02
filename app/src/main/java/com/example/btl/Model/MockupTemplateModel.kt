package com.example.btl.Model

data class MockupTemplateModel(
    val template_id: Int,
    val name: String,
    val description: String,
    val image_url: String,  // Thumbnail image
    val is_selected: Boolean = false
)

