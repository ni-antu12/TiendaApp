package com.duocuc.tiendaropa.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duocuc.tiendaropa.data.*
import com.duocuc.tiendaropa.repository.ApiRepository
import com.duocuc.tiendaropa.utils.TiendaApp
import kotlinx.coroutines.launch

class TiendaViewModel : ViewModel() {
    
    private val repository = ApiRepository()
    private val localStorage = LocalStorage(TiendaApp.instance)
    
    // Estados de UI
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    // Lista de productos disponibles
    var products by mutableStateOf<List<Product>>(emptyList())
        private set
    
    // Carrito de compras
    var cartItems by mutableStateOf<List<CartItem>>(emptyList())
        private set
    
    // Usuario logueado
    var isLoggedIn by mutableStateOf(false)
        private set
    
    var currentUser by mutableStateOf<User?>(null)
        private set
    
    // Ventas
    var sales by mutableStateOf<List<Sale>>(emptyList())
        private set
    
    // Órdenes del usuario
    var userOrders by mutableStateOf<List<com.duocuc.tiendaropa.network.Order>>(emptyList())
        private set
    
    // Productos vendidos por el usuario
    var userSoldProducts by mutableStateOf<List<Product>>(emptyList())
        private set
    
    init {
        loadProducts()
        // Restaurar sesión si existe
        if (localStorage.isLoggedIn()) {
            val savedUser = localStorage.getUser()
            if (savedUser != null) {
                isLoggedIn = true
                currentUser = savedUser
                // Cargar carrito desde BD
                loadCartFromDB()
            }
        }
    }
    
    // Cargar productos desde la API
    fun loadProducts() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            android.util.Log.d("TiendaViewModel", "loadProducts - Starting to load products")
            
            repository.getProducts().fold(
                onSuccess = { productList ->
                    android.util.Log.d("TiendaViewModel", "loadProducts - SUCCESS: Loaded ${productList.size} products")
                    productList.forEachIndexed { index, product ->
                        android.util.Log.d("TiendaViewModel", "  Product $index: ${product.name} (seller: ${product.sellerName})")
                    }
                    products = productList
                    isLoading = false
                },
                onFailure = { error ->
                    android.util.Log.e("TiendaViewModel", "loadProducts - FAILED: ${error.message}", error)
                    errorMessage = error.message ?: "Error al cargar productos"
                    isLoading = false
                }
            )
        }
    }
    
    // Registro de usuario
    fun register(request: RegisterRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.register(request).fold(
                onSuccess = {
                    isLoading = false
                    onSuccess()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Error al registrar usuario"
                    isLoading = false
                    onError(error.message ?: "Error al registrar usuario")
                }
            )
        }
    }
    
    // Login de usuario
    fun login(request: LoginRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.login(request).fold(
                onSuccess = { response ->
                    isLoggedIn = true
                    currentUser = response.user
                    // Guardar sesión en SharedPreferences
                    localStorage.saveUser(response.user)
                    isLoading = false
                    // Cargar carrito desde BD
                    loadCartFromDB()
                    onSuccess()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Error al iniciar sesión"
                    isLoading = false
                    onError(error.message ?: "Error al iniciar sesión")
                }
            )
        }
    }
    
    // Logout
    fun logout() {
        // Guardar userId antes de limpiar currentUser
        val userId = currentUser?.id
        
        isLoggedIn = false
        currentUser = null
        cartItems = emptyList()
        
        // Limpiar sesión y carrito local
        localStorage.clearSession()
        localStorage.clearCart()
        
        // Limpiar carrito de BD
        userId?.let {
            viewModelScope.launch {
                repository.clearCart(it)
            }
        }
    }
    
    // Cargar carrito desde BD
    private fun loadCartFromDB() {
        currentUser?.id?.let { userId ->
            viewModelScope.launch {
                repository.getCart(userId).fold(
                    onSuccess = { cartItemsResponse ->
                        cartItems = cartItemsResponse.map { item ->
                            CartItem(
                                cartItemId = item.id,  // Guardar el ID del cart_item
                                product = Product(
                                    id = item.product_id,
                                    name = item.product_name,
                                    description = "",
                                    price = item.product_price,
                                    stock = 0,
                                    category = "",
                                    imageUrl = item.product_image_url ?: ""
                                ),
                                quantity = item.quantity
                            )
                        }
                        // Guardar también en SharedPreferences como backup
                        localStorage.saveCart(cartItems)
                    },
                    onFailure = { }
                )
            }
        }
    }
    
    // Crear producto
    fun createProduct(product: Product, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            android.util.Log.d("TiendaViewModel", "createProduct - Starting creation")
            
            repository.createProduct(product).fold(
                onSuccess = { newProduct ->
                    android.util.Log.d("TiendaViewModel", "createProduct - Product created: ${newProduct.id}")
                    
                    // Recargar toda la lista de productos desde el backend
                    repository.getProducts().fold(
                        onSuccess = { productsList ->
                            android.util.Log.d("TiendaViewModel", "createProduct - Products reloaded: ${productsList.size} products")
                            products = productsList
                            isLoading = false
                            onSuccess()
                        },
                        onFailure = { error ->
                            android.util.Log.e("TiendaViewModel", "createProduct - Reload failed: ${error.message}")
                            // Si falla la recarga, al menos agregamos el producto localmente
                            products = products + newProduct
                            isLoading = false
                            onSuccess()
                        }
                    )
                },
                onFailure = { error ->
                    android.util.Log.e("TiendaViewModel", "createProduct - Creation failed: ${error.message}")
                    errorMessage = error.message ?: "Error al crear producto"
                    isLoading = false
                    onError(error.message ?: "Error al crear producto")
                }
            )
        }
    }
    
    // Actualizar producto
    fun updateProduct(id: Int, product: Product, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.updateProduct(id, product).fold(
                onSuccess = { updatedProduct ->
                    products = products.map { if (it.id == id) updatedProduct else it }
                    isLoading = false
                    onSuccess()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Error al actualizar producto"
                    isLoading = false
                    onError(error.message ?: "Error al actualizar producto")
                }
            )
        }
    }
    
    // Eliminar producto
    fun deleteProduct(id: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.deleteProduct(id).fold(
                onSuccess = {
                    products = products.filter { it.id != id }
                    isLoading = false
                    onSuccess()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Error al eliminar producto"
                    isLoading = false
                    onError(error.message ?: "Error al eliminar producto")
                }
            )
        }
    }
    
    // Agregar al carrito (guarda en BD)
    fun addToCart(product: Product) {
        if (currentUser == null) {
            errorMessage = "Debes iniciar sesión para agregar al carrito"
            return
        }
        
        // DEBUG: Log para verificar seller_name
        android.util.Log.d("TiendaViewModel", "addToCart - Product: ${product.name}")
        android.util.Log.d("TiendaViewModel", "addToCart - Seller: '${product.sellerName}'")
        android.util.Log.d("TiendaViewModel", "addToCart - Current User: '${currentUser?.username}'")
        android.util.Log.d("TiendaViewModel", "addToCart - Is same? ${product.sellerName == currentUser?.username}")
        
        // Validar que el usuario no esté comprando su propio producto
        if (!product.sellerName.isNullOrEmpty() && product.sellerName == currentUser?.username) {
            errorMessage = "No puedes comprar tus propios productos"
            android.util.Log.d("TiendaViewModel", "addToCart - BLOCKED: Self-purchase attempt")
            return
        }
        
        val userId = currentUser!!.id ?: return
        val productId = product.id ?: return
        
        viewModelScope.launch {
            repository.addToCart(userId, productId, 1).fold(
                onSuccess = {
                    // Recargar carrito desde BD
                    loadCartFromDB()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Error al agregar al carrito"
                }
            )
        }
    }
    
    // Remover del carrito (elimina de BD)
    fun removeFromCart(cartItemId: Int) {
        viewModelScope.launch {
            repository.deleteCartItem(cartItemId).fold(
                onSuccess = {
                    // Recargar carrito desde BD
                    loadCartFromDB()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Error al eliminar del carrito"
                }
            )
        }
    }
    
    // Actualizar cantidad en carrito (actualiza en BD)
    fun updateQuantity(cartItemId: Int, quantity: Int) {
        if (currentUser == null) return
        
        if (quantity <= 0) {
            removeFromCart(cartItemId)
        } else {
            viewModelScope.launch {
                repository.updateCartItem(cartItemId, quantity).fold(
                    onSuccess = {
                        // Recargar carrito desde BD
                        loadCartFromDB()
                    },
                    onFailure = { error ->
                        errorMessage = error.message ?: "Error al actualizar cantidad"
                    }
                )
            }
        }
    }
    
    // Calcular total del carrito
    fun getCartTotal(): Double {
        return cartItems.sumOf { it.product.price * it.quantity }
    }
    
    // Obtener cantidad total de items en el carrito
    fun getCartItemsCount(): Int {
        return cartItems.sumOf { it.quantity }
    }
    
    // Procesar compra (crea orden en BD)
    fun processPurchase(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (currentUser == null || cartItems.isEmpty()) {
            onError("Debes estar logueado y tener productos en el carrito")
            return
        }
        
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val userId = currentUser!!.id ?: return@launch
            
            // Crear orden (el microservicio limpia el carrito de BD automáticamente)
            repository.createOrder(userId).fold(
                onSuccess = { _ ->
                    // Limpiar carrito local (el microservicio ya lo limpió de BD)
                    cartItems = emptyList()
                    localStorage.clearCart()
                    isLoading = false
                    onSuccess()
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Error al procesar compra"
                    isLoading = false
                    onError(error.message ?: "Error al procesar compra")
                }
            )
        }
    }
    
    // Cargar ventas
    fun loadSales() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.getSales().fold(
                onSuccess = { salesList ->
                    sales = salesList
                    isLoading = false
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Error al cargar ventas"
                    isLoading = false
                }
            )
        }
    }
    
    // Cargar órdenes del usuario
    fun loadUserOrders() {
        if (currentUser == null) return
        
        val userId = currentUser!!.id ?: return
        
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.getUserOrders(userId).fold(
                onSuccess = { ordersList ->
                    userOrders = ordersList
                    isLoading = false
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Error al cargar órdenes"
                    isLoading = false
                }
            )
        }
    }
    
    // Cargar productos VENDIDOS por el usuario (ventas reales)
    fun loadUserSoldProducts() {
        val userId = currentUser?.id ?: return
        
        viewModelScope.launch {
            isLoading = true
            android.util.Log.d("TiendaViewModel", "loadUserSoldProducts - Loading sales for user $userId")
            
            repository.getUserSales(userId).fold(
                onSuccess = { sales ->
                    android.util.Log.d("TiendaViewModel", "loadUserSoldProducts - Loaded ${sales.size} sales")
                    
                    // Convertir UserSale a Product para mostrar en la UI
                    userSoldProducts = sales.map { sale ->
                        Product(
                            id = sale.product_id,
                            name = sale.product_name,
                            description = "Vendido: ${sale.quantity} unidades",
                            price = sale.product_price,
                            stock = sale.product_stock, // Stock actual del producto
                            category = "",
                            imageUrl = sale.product_image,
                            sellerName = sale.seller_name
                        )
                    }
                    isLoading = false
                },
                onFailure = { error ->
                    android.util.Log.e("TiendaViewModel", "loadUserSoldProducts - Error: ${error.message}")
                    userSoldProducts = emptyList()
                    isLoading = false
                }
            )
        }
    }

    // Limpiar mensaje de error
    fun clearError() {
        errorMessage = null
    }
}
