package com.duocuc.tiendaropa.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)  // Aumentado para manejar imágenes grandes
        .readTimeout(60, TimeUnit.SECONDS)     // Aumentado para manejar imágenes grandes
        .writeTimeout(60, TimeUnit.SECONDS)    // Aumentado para manejar imágenes grandes
        .build()
    
    private val usersRetrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL_USERS)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val productsRetrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL_PRODUCTS)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val salesRetrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL_SALES)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val usersApi: UsersApi = usersRetrofit.create(UsersApi::class.java)
    val productsApi: ProductsApi = productsRetrofit.create(ProductsApi::class.java)
    val salesApi: SalesApi = salesRetrofit.create(SalesApi::class.java)
}

