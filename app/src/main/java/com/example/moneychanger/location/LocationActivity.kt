package com.example.moneychanger.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.moneychanger.R
import com.example.moneychanger.network.ApiService
import com.example.moneychanger.network.location.GeocodeResponse
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private val GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com/maps/api/"
    private val API_KEY = "AIzaSyCRdYSXhCVr4Keg7wm80XeICwvg0spuHa4" // 🔑 반드시 교체

    private lateinit var googleApi: ApiService.GoogleGeocodingApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl(GOOGLE_MAPS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        googleApi = retrofit.create(ApiService.GoogleGeocodingApi::class.java)

        val locationText: TextView = findViewById(R.id.locationText)
        val locationButton: Button = findViewById(R.id.locationButton)

        locationButton.setOnClickListener {
            getLocation(locationText)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!RequestPermissionsUtil(this).isLocationPermitted()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (!RequestPermissionsUtil(this).isLocationPermitted()) {
            RequestPermissionsUtil(this).requestLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(textView: TextView) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = Priority.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    val location = result.lastLocation ?: return
                    val lat = location.latitude
                    val lng = location.longitude

                    lifecycleScope.launch {
                        val address = getAddressFromGoogleApi(lat, lng)
                        val locationInfo = "위도: $lat\n경도: $lng\n"
                        val addressInfo = address ?: "주소를 찾을 수 없습니다."
                        textView.text = locationInfo + addressInfo
                    }
                }
            },
            mainLooper
        )
    }

    private suspend fun getAddressFromGoogleApi(lat: Double, lng: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response: GeocodeResponse = googleApi.reverseGeocode(
                    latlng = "$lat,$lng",
                    apiKey = API_KEY
                )
                if (response.status == "OK" && response.results.isNotEmpty()) {
                    response.results[0].formatted_address
                } else {
                    null
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LocationActivity, "주소 가져오기 실패", Toast.LENGTH_SHORT).show()
                }
                null
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            val message = if (granted) "위치 권한이 허용되었습니다." else "위치 권한이 거부되었습니다."
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
