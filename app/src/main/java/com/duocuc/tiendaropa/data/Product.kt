package com.duocuc.tiendaropa.data

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int? = null,
    val name: String,
    val description: String? = "",
    val price: Double,
    val stock: Int? = 0,
    val category: String? = "",
    @SerializedName("image_url")
    val imageUrl: String? = "",
    @SerializedName("seller_name")
    val sellerName: String? = null
)

data class CartItem(
    val cartItemId: Int? = null,  // ID del item en la BD
    val product: Product,
    var quantity: Int
)


