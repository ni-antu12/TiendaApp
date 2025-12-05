package com.duocuc.tiendaropa

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.duocuc.tiendaropa.ui.screens.*
import com.duocuc.tiendaropa.viewmodel.TiendaViewModel

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Login : Screen("login")
    object Register : Screen("register")
    object Productos : Screen("productos")
    object ProductDetail : Screen("product_detail/{id}") {
        fun createRoute(id: Int) = "product_detail/$id"
    }
    object AgregarProducto : Screen("agregar_producto")
    object EditarProducto : Screen("editar_producto/{id}") {
        fun createRoute(id: Int) = "editar_producto/$id"
    }
    object Carrito : Screen("carrito")
    object ResumenCompra : Screen("resumen_compra")
    object Ventas : Screen("ventas")
    object QuienesSomos : Screen("quienes_somos")
    object Perfil : Screen("perfil")
    object FormularioEnvio : Screen("formulario_envio")
}

@Composable
fun TiendaRopaApp() {
    val navController = rememberNavController()
    val viewModel: TiendaViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(navController = navController, viewModel = viewModel)
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, viewModel = viewModel)
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController, viewModel = viewModel)
        }
        
        composable(Screen.Productos.route) {
            ProductosScreen(navController = navController, viewModel = viewModel)
        }
        
        composable(Screen.ProductDetail.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            if (productId != null) {
                ProductDetailScreen(
                    navController = navController,
                    viewModel = viewModel,
                    productId = productId
                )
            }
        }
        
        composable(Screen.AgregarProducto.route) {
            AgregarProductoScreen(navController = navController, viewModel = viewModel)
        }
        
        composable(Screen.EditarProducto.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            if (productId != null) {
                EditarProductoScreen(
                    navController = navController,
                    viewModel = viewModel,
                    productId = productId
                )
            }
        }
        
        composable(Screen.Carrito.route) {
            CarritoScreen(navController = navController, viewModel = viewModel)
        }
        
        composable(Screen.ResumenCompra.route) {
            ResumenCompraScreen(navController = navController, viewModel = viewModel)
        }
        
        composable(Screen.Ventas.route) {
            VentasScreen(navController = navController, viewModel = viewModel)
        }
        
        composable(Screen.QuienesSomos.route) {
            QuienesSomosScreen(navController = navController, viewModel = viewModel)
        }
        
        composable(Screen.Perfil.route) {
            PerfilScreen(navController = navController, viewModel = viewModel)
        }
        
        composable(Screen.FormularioEnvio.route) {
            FormularioEnvioScreen(
                navController = navController,
                viewModel = viewModel,
                onConfirm = { shippingInfo ->
                    // Procesar compra como invitado
                    viewModel.processPurchase(
                        onSuccess = {
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Main.route) { inclusive = false }
                            }
                        },
                        onError = { }
                    )
                }
            )
        }
    }
}

