package com.duocuc.tiendaropa.data

data class Sale(
    val id: Int? = null,
    val user_id: Int,
    val product_id: Int,
    val quantity: Int,
    val total: Double,
    val created_at: String? = null
)

data class SaleRequest(
    val user_id: Int,
    val product_id: Int,
    val quantity: Int,
    val total: Double
)

data class SaleResponse(
    val message: String,
    val id: Int,
    val sale: Sale
)

