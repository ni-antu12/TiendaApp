package com.duocuc.tiendaropa.network

import com.duocuc.tiendaropa.data.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UsersApi {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}

