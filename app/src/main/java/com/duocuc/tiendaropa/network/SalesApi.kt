package com.duocuc.tiendaropa.network

import com.duocuc.tiendaropa.data.*
import retrofit2.Response
import retrofit2.http.*

interface SalesApi {
    // Carrito
    @GET("cart/{user_id}")
    suspend fun getCart(@Path("user_id") userId: Int): Response<List<CartItemResponse>>
    
    @POST("cart")
    suspend fun addToCart(@Body request: CartRequest): Response<Map<String, Any>>
    
    @PUT("cart/{item_id}")
    suspend fun updateCartItem(@Path("item_id") itemId: Int, @Body request: CartUpdateRequest): Response<Map<String, Any>>
    
    @DELETE("cart/{item_id}")
    suspend fun deleteCartItem(@Path("item_id") itemId: Int): Response<Map<String, String>>
    
    @DELETE("cart/user/{user_id}")
    suspend fun clearCart(@Path("user_id") userId: Int): Response<Map<String, String>>
    
    // Órdenes
    @POST("orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<OrderResponse>
    
    @GET("orders/user/{user_id}")
    suspend fun getUserOrders(@Path("user_id") userId: Int): Response<List<Order>>
    
    @GET("orders/{order_id}/items")
    suspend fun getOrderItems(@Path("order_id") orderId: Int): Response<List<OrderItem>>
    
    // Ventas (compatibilidad)
    @POST("sales")
    suspend fun createSale(@Body sale: SaleRequest): Response<SaleResponse>
    
    @GET("sales")
    suspend fun getSales(): Response<List<Sale>>
    
    @GET("sales/user/{user_id}")
    suspend fun getUserSales(@Path("user_id") userId: Int): Response<List<UserSale>>
}

data class CartRequest(
    val user_id: Int,
    val product_id: Int,
    val quantity: Int
)

data class CartUpdateRequest(
    val quantity: Int
)

data class CartItemResponse(
    val id: Int,
    val user_id: Int,
    val product_id: Int,
    val quantity: Int,
    val product_name: String,
    val product_price: Double,
    val product_image_url: String?
)

data class OrderRequest(
    val user_id: Int
)

data class OrderResponse(
    val message: String,
    val order_id: Int,
    val total: Double
)

data class Order(
    val id: Int,
    val user_id: Int,
    val total: Double,
    val status: String,
    val items_count: Int,
    val created_at: String
)

data class OrderItem(
    val id: Int,
    val order_id: Int,
    val product_id: Int,
    val product_name: String,
    val product_image_url: String?,
    val quantity: Int,
    val price: Double,
    val subtotal: Double
)

data class UserSale(
    val sale_id: Int,
    val product_id: Int,
    val quantity: Int,
    val total: Double,
    val created_at: String,
    val product_name: String,
    val product_price: Double,
    val product_image: String?,
    val seller_name: String,
    val product_stock: Int
)

