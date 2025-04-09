package com.example.moneychanger.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.moneychanger.R
import kotlinx.coroutines.launch
class LocationActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

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

    private fun getLocation(textView: TextView) {
        LocationUtil.getCurrentLocation(
            context = this,
            onSuccess = { location ->
                val lat = location.latitude
                val lng = location.longitude

                lifecycleScope.launch {
                    val address = getAddressFromLatLng(this@LocationActivity, lat, lng)
                    val locationInfo = "위도: $lat\n경도: $lng\n"
                    val addressInfo = address ?: "주소를 찾을 수 없습니다."
                    textView.text = locationInfo + addressInfo
                }
            }
        )
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
