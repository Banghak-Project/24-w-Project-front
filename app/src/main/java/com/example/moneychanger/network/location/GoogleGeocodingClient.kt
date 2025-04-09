package com.example.moneychanger.network.location

import com.example.moneychanger.BuildConfig
import com.example.moneychanger.network.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GoogleGeocodingClient {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService.GoogleGeocodingApi by lazy {
        retrofit.create(ApiService.GoogleGeocodingApi::class.java)
    }

    val apiKey: String
        get() = BuildConfig.GOOGLE_MAPS_API_KEY
}
