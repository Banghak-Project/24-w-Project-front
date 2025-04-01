package com.example.moneychanger.location


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.moneychanger.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class LocationActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        val locationText: TextView = findViewById(R.id.locationText)
        val locationButton: Button = findViewById(R.id.locationButton)
        locationButton.setOnClickListener {
            getLocation(locationText)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!RequestPermissionsUtil(this).isLocationPermitted()) {
            RequestPermissionsUtil(this).requestLocation()
        }
    }

    // 위도 경도 값 나오는 getLocation
//    @SuppressLint("MissingPermission")
//    private fun getLocation(textView: TextView) {
//        val fusedLocationProviderClient =
//            LocationServices.getFusedLocationProviderClient(this)
//
//        fusedLocationProviderClient.lastLocation
//            .addOnSuccessListener { success: Location? ->
//                success?.let { location ->
//                    textView.text =
//                        "${location.latitude}, ${location.longitude}"
//                }
//            }
//            .addOnFailureListener { fail ->
//                textView.text = fail.localizedMessage
//            }
//    }
    // 주소가 출력되도록 해둔 getLocation
    @SuppressLint("MissingPermission")
    private fun getLocation(textView: TextView) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val lat = it.latitude
                    val lng = it.longitude

                    lifecycleScope.launch(Dispatchers.IO) {
                        val address = getAddress(lat, lng)?.getOrNull(0)

                        withContext(Dispatchers.Main) {
                            val locationInfo = "위도: $lat\n경도: $lng"
                            val addressInfo = if (address != null) {
                                "\n주소: ${address.adminArea ?: ""} ${address.locality ?: ""} ${address.thoroughfare ?: ""}".trim()
                            } else {
                                "\n주소를 찾을 수 없습니다."
                            }

                            textView.text = locationInfo + addressInfo
                        }
                    }
                } ?: run {
                    textView.text = "위치 정보를 가져올 수 없습니다."
                }
            }
            .addOnFailureListener {
                textView.text = "위치 정보를 가져오지 못했습니다: ${it.localizedMessage}"
            }
    }



    private suspend fun getAddress(lat: Double, lng: Double): List<Address>? {
        return try {
            val geocoder = Geocoder(this@LocationActivity, Locale.getDefault())
            geocoder.getFromLocation(lat, lng, 1)
        } catch (e: IOException) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@LocationActivity, "주소를 가져 올 수 없습니다", Toast.LENGTH_SHORT).show()
            }
            null
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (granted) {
                Toast.makeText(this, "위치 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "위치 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}