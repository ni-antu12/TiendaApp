package com.duocuc.tiendaropa.data

data class User(
    val id: Int? = null,
    val username: String,
    val name: String,
    val lastname: String,
    val email: String,
    val password: String? = null
)

data class LoginRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val name: String,
    val lastname: String,
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val id: Int,
    val user: User
)

data class RegisterResponse(
    val message: String,
    val user: User
)

