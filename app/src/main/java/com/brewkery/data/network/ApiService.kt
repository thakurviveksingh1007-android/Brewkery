package com.brewkery.data.network

import com.brewkery.data.model.BrewkeryMenuResponse
import com.brewkery.data.model.MenuItem
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface BrewkeryApi {

    @GET("data.json")
    suspend fun getMenu(): BrewkeryMenuResponse

    @GET("api/items/{id}.json")
    suspend fun getItemDetail(@Path("id") id: Int): MenuItem
}

object RetrofitClient {

    private const val BASE_URL = "https://raw.githubusercontent.com/VivekShah138/Brewkery/main/"

    val api: BrewkeryApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BrewkeryApi::class.java)
    }
}
