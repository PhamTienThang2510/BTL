package com.example.btl.Model

import com.google.gson.annotations.SerializedName

data class Address(
    val address_id: Int? = null,
    val customer_id: Int? = null,
    val receiver_name: String = "",
    val phone: String = "",
    val ward_id: Int? = null,
    val address_detail: String = "",
    @SerializedName("is_default")
    val isDefault: Boolean = false,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class CreateAddressRequest(
    val receiver_name: String,
    val phone: String,
    val ward_id: Int,
    val address_detail: String,
    @SerializedName("is_default")
    val isDefault: Boolean = false
)

data class UpdateAddressRequest(
    val receiver_name: String? = null,
    val phone: String? = null,
    val ward_id: Int? = null,
    val address_detail: String? = null,
    @SerializedName("is_default")
    val isDefault: Boolean? = null
)

