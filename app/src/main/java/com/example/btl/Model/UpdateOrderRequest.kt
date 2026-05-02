package com.example.btl.Model

import com.google.gson.annotations.SerializedName

data class UpdateOrderRequest(
    @SerializedName("order_status")
    val order_status: String
)

