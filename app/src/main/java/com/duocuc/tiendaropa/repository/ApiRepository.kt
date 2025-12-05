package com.duocuc.tiendaropa.repository

import com.duocuc.tiendaropa.data.*
import com.duocuc.tiendaropa.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiRepository {
    private val usersApi = RetrofitClient.usersApi
    private val productsApi = RetrofitClient.productsApi
    private val salesApi = RetrofitClient.salesApi
    
    // Users
    suspend fun register(request: RegisterRequest): Result<RegisterResponse> = withContext(Dispatchers.IO) {
        try {
            val response = usersApi.register(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception("Error: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(request: LoginRequest): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = usersApi.login(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Credenciales inválidas"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Products
    suspend fun getProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val response = productsApi.getProducts()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar productos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProduct(id: Int): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val response = productsApi.getProduct(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Producto no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createProduct(product: Product): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val response = productsApi.createProduct(product)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error al crear producto"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProduct(id: Int, product: Product): Result<Product> = withContext(Dispatchers.IO) {
        try {
            val response = productsApi.updateProduct(id, product)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error al actualizar producto"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteProduct(id: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = productsApi.deleteProduct(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar producto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Sales
    suspend fun createSale(sale: SaleRequest): Result<SaleResponse> = withContext(Dispatchers.IO) {
        try {
            val response = salesApi.createSale(sale)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error al registrar venta"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSales(): Result<List<Sale>> = withContext(Dispatchers.IO) {
        try {
            val response = salesApi.getSales()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar ventas: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Carrito
    suspend fun getCart(userId: Int): Result<List<com.duocuc.tiendaropa.network.CartItemResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = salesApi.getCart(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar carrito"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addToCart(userId: Int, productId: Int, quantity: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = com.duocuc.tiendaropa.network.CartRequest(userId, productId, quantity)
            val response = salesApi.addToCart(request)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al agregar al carrito"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateCartItem(itemId: Int, quantity: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = com.duocuc.tiendaropa.network.CartUpdateRequest(quantity)
            val response = salesApi.updateCartItem(itemId, request)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al actualizar carrito"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCartItem(itemId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = salesApi.deleteCartItem(itemId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar del carrito"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun clearCart(userId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = salesApi.clearCart(userId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al limpiar carrito"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Órdenes
    suspend fun createOrder(userId: Int): Result<com.duocuc.tiendaropa.network.OrderResponse> = withContext(Dispatchers.IO) {
        try {
            val request = com.duocuc.tiendaropa.network.OrderRequest(userId)
            val response = salesApi.createOrder(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear orden"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserOrders(userId: Int): Result<List<com.duocuc.tiendaropa.network.Order>> = withContext(Dispatchers.IO) {
        try {
            val response = salesApi.getUserOrders(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar órdenes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getOrderItems(orderId: Int): Result<List<com.duocuc.tiendaropa.network.OrderItem>> = withContext(Dispatchers.IO) {
        try {
            val response = salesApi.getOrderItems(orderId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar items de la orden"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserSales(userId: Int): Result<List<com.duocuc.tiendaropa.network.UserSale>> = withContext(Dispatchers.IO) {
        try {
            val response = salesApi.getUserSales(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cargar ventas del usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}

