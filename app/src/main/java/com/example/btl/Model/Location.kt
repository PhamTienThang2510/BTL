package com.example.btl.Model

data class LocationOption(
    val id: Int,
    val name: String,
    val parentId: Int? = null
) {
    override fun toString(): String = name
}

