package com.example.moneychanger.network.location

import retrofit2.http.GET
import retrofit2.http.Query
//지우지 마세용 - 지은
interface GoogleGeocodingApi {
    @GET("geocode/json")
    suspend fun reverseGeocode(
        @Query("latlng") latlng: String,
        @Query("key") apiKey: String
    ): GeocodeResponse
}
