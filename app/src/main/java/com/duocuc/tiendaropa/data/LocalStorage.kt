package com.duocuc.tiendaropa.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("TiendaApp", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Sesión de usuario
    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().putString("current_user", userJson).apply()
        prefs.edit().putBoolean("is_logged_in", true).apply()
        prefs.edit().putInt("user_id", user.id ?: 0).apply()
    }
    
    fun getUser(): User? {
        val userJson = prefs.getString("current_user", null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }
    
    fun getUserId(): Int {
        return prefs.getInt("user_id", 0)
    }
    
    fun clearSession() {
        prefs.edit().remove("current_user").apply()
        prefs.edit().remove("is_logged_in").apply()
        prefs.edit().remove("user_id").apply()
    }
    
    // Carrito (guardado localmente como backup)
    fun saveCart(cartItems: List<CartItem>) {
        val cartJson = gson.toJson(cartItems)
        prefs.edit().putString("cart_items", cartJson).apply()
    }
    
    fun getCart(): List<CartItem> {
        val cartJson = prefs.getString("cart_items", null)
        return if (cartJson != null) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            gson.fromJson(cartJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    fun clearCart() {
        prefs.edit().remove("cart_items").apply()
    }
}

