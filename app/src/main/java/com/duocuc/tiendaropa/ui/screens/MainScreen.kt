package com.duocuc.tiendaropa.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.duocuc.tiendaropa.ui.components.CartBadgeIcon
import coil.compose.rememberAsyncImagePainter
import com.duocuc.tiendaropa.viewmodel.TiendaViewModel
import com.duocuc.tiendaropa.utils.formatChileanPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: TiendaViewModel) {
    val products = viewModel.products
    val isLoggedIn = viewModel.isLoggedIn
    val currentUser = viewModel.currentUser
    
    Scaffold(
        floatingActionButton = {
            if (isLoggedIn) {
                FloatingActionButton(
                    onClick = { navController.navigate("agregar_producto") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("productos") },
                    icon = { Icon(Icons.Default.List, contentDescription = "Productos") },
                    label = { Text("Productos") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("carrito") },
                    icon = { CartBadgeIcon(itemCount = viewModel.getCartItemsCount(), contentDescription = "Carrito") },
                    label = { Text("Carrito") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("quienes_somos") },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Info") },
                    label = { Text("Info") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { 
                        if (isLoggedIn) {
                            navController.navigate("perfil")
                        } else {
                            navController.navigate("login")
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Usuario") },
                    label = { Text(if (isLoggedIn) "Perfil" else "Login") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Barra de búsqueda
            Spacer(modifier = Modifier.height(16.dp))
            var searchText by remember { mutableStateOf("") }
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Buscar productos...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )
            
            // Carrusel
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(3) { index ->
                    CarouselItem(index = index)
                }
            }
            
            // Categorías
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Categorías",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(listOf("Poleras", "Camisas", "Bermudas y Shorts", "Jeans", "Polerones", "Chaquetas y Parkas", "Zapatillas", "Accesorios")) { category ->
                    CategoryChip(category = category)
                }
            }
            
            // Productos destacados
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Productos Destacados",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(products.reversed().take(2)) { product ->
                    FeaturedProductCard(
                        product = product,
                        isOwnProduct = isLoggedIn && product.sellerName == currentUser?.name,
                        onClick = { navController.navigate("product_detail/${product.id}") }
                    )
                }
            }
            
            // Listado resumido de productos
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Todos los Productos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { navController.navigate("productos") }) {
                    Text("Ver todos")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(products.reversed().take(4)) { product ->
                    CompactProductCard(
                        product = product,
                        isOwnProduct = isLoggedIn && product.sellerName == currentUser?.name,
                        onClick = { navController.navigate("product_detail/${product.id}") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CarouselItem(index: Int) {
    val offers = listOf(
        "50% descuento\nen Poleras",
        "Hasta 40%\nen Shorts y Bermudas",
        "20% descuento\nen Zapatillas"
    )
    
    val colors = listOf(
        Color(0xFFE53935), // Rojo
        Color(0xFF1E88E5), // Azul
        Color(0xFFFB8C00)  // Naranja
    )
    
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors[index % 3])
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = offers[index % 3],
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
        }
    }
}

@Composable
fun CategoryChip(category: String) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun FeaturedProductCard(
    product: com.duocuc.tiendaropa.data.Product,
    isOwnProduct: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .background(Color(0xFF2D2D2D)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (!product.imageUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(product.imageUrl),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF3D3D3D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            if (isOwnProduct) {
                Text(
                    text = "Tu producto",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            } else {
                Text(
                    text = formatChileanPrice(product.price),
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun CompactProductCard(
    product: com.duocuc.tiendaropa.data.Product,
    isOwnProduct: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
            .background(Color(0xFF2D2D2D)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (!product.imageUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(product.imageUrl),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF3D3D3D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.name.take(10),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = product.name.take(15),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1
            )
            if (isOwnProduct) {
                Text(
                    text = "Tu producto",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            } else {
                Text(
                    text = formatChileanPrice(product.price),
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

