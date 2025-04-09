package com.example.moneychanger.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.widget.Toast
import com.example.moneychanger.network.location.GoogleGeocodingClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocationUtil {
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        context: Context,
        onSuccess: (Location) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = Priority.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation
                    if (location != null) {
                        onSuccess(location)
                    } else {
                        onError?.invoke("위치를 가져올 수 없습니다.")
                        Toast.makeText(context, "위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            Looper.getMainLooper()
        )
    }
}
suspend fun getAddressFromLatLng(context: Context, lat: Double, lng: Double): String? {
    return withContext(Dispatchers.IO) {
        try {
            val response = GoogleGeocodingClient.api.reverseGeocode(
                latlng = "$lat,$lng",
                apiKey = GoogleGeocodingClient.apiKey
            )
            if (response.status == "OK" && response.results.isNotEmpty()) {
                response.results[0].formatted_address
            } else {
                null
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "주소 가져오기 실패", Toast.LENGTH_SHORT).show()
            }
            null
        }
    }
}
