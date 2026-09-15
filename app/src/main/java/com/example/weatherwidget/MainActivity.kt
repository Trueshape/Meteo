package com.example.weatherwidget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Questa activity si apre solo se l'utente tocca l'icona dell'app
 * (non serve per il widget in sé). Il suo unico scopo è chiedere
 * il permesso di posizione, che il widget da solo non può richiedere.
 */
class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val info = TextView(this)
        info.text = "Widget Meteo installato.\n\n" +
                "Tieni premuto sulla schermata Home, scegli 'Widget', " +
                "cerca 'Weather Widget' e trascinalo sulla home.\n\n" +
                "Concedi il permesso di posizione qui sotto per far funzionare il widget."
        info.setPadding(48, 96, 48, 48)
        info.textSize = 16f
        setContentView(info)

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            WeatherWidgetProvider.requestImmediateUpdate(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            WeatherWidgetProvider.requestImmediateUpdate(this)
        }
    }
}
