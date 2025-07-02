package com.example.melisamobil

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var btnKonum: Button
    private lateinit var txtAddress: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnKonum = findViewById(R.id.btnKonum)
        txtAddress = findViewById(R.id.txtAddress)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        txtAddress.text = ""

        btnKonum.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            } else {
                getLocationAndShowAddress()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocationAndShowAddress()
        } else {
            txtAddress.text = "Konum izni verilmedi."
        }
    }

    private fun getLocationAndShowAddress() {
        txtAddress.text = "Konum alınıyor..."
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    getAddressFromGeocoder(location.latitude, location.longitude)
                } else {
                    txtAddress.text = "Konum alınamadı. Lütfen konumu açın."
                }
            }.addOnFailureListener {
                txtAddress.text = "Konum alınırken hata oluştu."
            }
        } catch (e: SecurityException) {
            txtAddress.text = "Konum izni verilmedi."
        }
    }

    private fun getAddressFromGeocoder(lat: Double, lon: Double) {
        Thread {
            try {
                val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    // Adresin tüm kısımlarını birleştir
                    val fullAddress = buildString {
                        append("Şu anda siz; ")
                        append(address.getAddressLine(0)) // Tam adres satırı
                        append(" civarındasınız.")
                    }
                    runOnUiThread {
                        txtAddress.text = fullAddress
                    }
                } else {
                    runOnUiThread {
                        txtAddress.text = "Adres alınamadı."
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    txtAddress.text = "Adres alınırken hata oluştu: ${e.localizedMessage}"
                }
            }
        }.start()
    }
}
