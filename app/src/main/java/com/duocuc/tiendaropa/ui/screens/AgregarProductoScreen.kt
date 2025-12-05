package com.duocuc.tiendaropa.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.duocuc.tiendaropa.data.Product
import com.duocuc.tiendaropa.ui.components.CartBadgeIcon
import java.io.ByteArrayOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarProductoScreen(navController: NavController, viewModel: com.duocuc.tiendaropa.viewmodel.TiendaViewModel) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageBase64 by remember { mutableStateOf<String>("") }
    
    val categories = listOf("Poleras", "Camisas", "Bermudas y Shorts", "Jeans", "Polerones", "Chaquetas y Parkas", "Zapatillas", "Accesorios")
    var expanded by remember { mutableStateOf(false) }
    val isLoggedIn = viewModel.isLoggedIn
    val context = LocalContext.current
    
    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Convertir URI a Bitmap y comprimir
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (bitmap != null) {
                    selectedImageBitmap = bitmap
                    
                    android.util.Log.d("AgregarProducto", "Original image: ${bitmap.width}x${bitmap.height}")
                    
                    // Redimensionar imagen a máximo 600px (más pequeño para reducir tamaño)
                    val maxDimension = 600
                    val resizedBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                        val scale = minOf(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
                        Bitmap.createScaledBitmap(
                            bitmap,
                            (bitmap.width * scale).toInt(),
                            (bitmap.height * scale).toInt(),
                            true
                        )
                    } else {
                        bitmap
                    }
                    
                    android.util.Log.d("AgregarProducto", "Resized image: ${resizedBitmap.width}x${resizedBitmap.height}")
                    
                    // Comprimir con calidad 60 (más compresión)
                    val outputStream = ByteArrayOutputStream()
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                    val imageBytes = outputStream.toByteArray()
                    
                    android.util.Log.d("AgregarProducto", "Compressed size: ${imageBytes.size / 1024}KB")
                    
                    imageBase64 = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                    
                    android.util.Log.d("AgregarProducto", "Base64 length: ${imageBase64.length} chars")
                    
                    // Limpiar recursos
                    if (resizedBitmap != bitmap) {
                        resizedBitmap.recycle()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AgregarProducto", "Error processing image: ${e.message}", e)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("main") },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Default.ShoppingCart, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Stock") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Dropdown de categoría
            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // URL de imagen
            OutlinedTextField(
                value = imageBase64,
                onValueChange = { imageBase64 = it },
                label = { Text("URL de Imagen (opcional)") },
                placeholder = { Text("https://ejemplo.com/imagen.jpg") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            )
            
            // Vista previa de imagen
            if (imageBase64.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imageBase64),
                            contentDescription = "Vista previa",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
            
            if (showError) {
                Text(
                    text = "Por favor, complete todos los campos correctamente",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones Guardar y Cancelar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text("Cancelar", fontSize = 16.sp)
                }
                
                Button(
                    onClick = {
                        // Validar que el usuario esté logueado
                        if (viewModel.currentUser == null) {
                            showError = true
                            return@Button
                        }
                        
                        if (name.isNotEmpty() && description.isNotEmpty() && 
                            price.isNotEmpty() && stock.isNotEmpty() && category.isNotEmpty()) {
                            try {
                                val sellerName = viewModel.currentUser?.username
                                android.util.Log.d("AgregarProducto", "Creating product with seller: '$sellerName'")
                                
                                if (sellerName.isNullOrEmpty()) {
                                    android.util.Log.e("AgregarProducto", "ERROR: seller_name is null or empty!")
                                    showError = true
                                    return@Button
                                }
                                
                                val newProduct = Product(
                                    name = name,
                                    description = description,
                                    price = price.toDouble(),
                                    stock = stock.toInt(),
                                    category = category,
                                    imageUrl = if (imageBase64.isNotEmpty()) imageBase64 else "",
                                    sellerName = sellerName
                                )
                                
                                android.util.Log.d("AgregarProducto", "Product object created: $newProduct")
                                
                                viewModel.createProduct(newProduct,
                                    onSuccess = {
                                        android.util.Log.d("AgregarProducto", "Product created successfully")
                                        navController.popBackStack()
                                    },
                                    onError = { error ->
                                        android.util.Log.e("AgregarProducto", "Error creating product: $error")
                                        showError = true
                                    }
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("AgregarProducto", "Exception: ${e.message}", e)
                                showError = true
                            }
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Guardar", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

