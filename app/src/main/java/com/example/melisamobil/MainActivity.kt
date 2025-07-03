package com.example.melisamobil

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
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
    private lateinit var backgroundImage: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnKonum = findViewById(R.id.btnKonum)
        txtAddress = findViewById(R.id.txtAddress)
        backgroundImage = findViewById(R.id.backgroundImage)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        txtAddress.text = ""
        backgroundImage.visibility = ImageView.GONE // İlk başta görünmesin

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
                    backgroundImage.visibility = ImageView.GONE
                }
            }.addOnFailureListener {
                txtAddress.text = "Konum alınırken hata oluştu."
                backgroundImage.visibility = ImageView.GONE
            }
        } catch (e: SecurityException) {
            txtAddress.text = "Konum izni verilmedi."
            backgroundImage.visibility = ImageView.GONE
        }
    }

    private fun getAddressFromGeocoder(lat: Double, lon: Double) {
        Thread {
            try {
                val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val fullAddress = buildString {
                        append("Şu anda siz; ")
                        append(address.getAddressLine(0))
                        append(" civarındasınız. 🚩")
                    }
                    val city = address.locality ?: address.adminArea ?: ""

                    runOnUiThread {
                        txtAddress.text = fullAddress
                        // Şehre göre arka plan değişimi
                        when {
                            city.contains("İstanbul", true) || city.contains("Istanbul", true) -> {
                                backgroundImage.setImageResource(R.drawable.background_istanbul)
                                backgroundImage.visibility = ImageView.VISIBLE
                            }
                            city.contains("Kocaeli", true) -> {
                                backgroundImage.setImageResource(R.drawable.background_kocaeli)
                                backgroundImage.visibility = ImageView.VISIBLE
                            }
                            else -> {
                                backgroundImage.visibility = ImageView.GONE
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        txtAddress.text = "Adres alınamadı."
                        backgroundImage.visibility = ImageView.GONE
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    txtAddress.text = "Adres alınırken hata oluştu: ${e.localizedMessage}"
                    backgroundImage.visibility = ImageView.GONE
                }
            }
        }.start()
    }
}
