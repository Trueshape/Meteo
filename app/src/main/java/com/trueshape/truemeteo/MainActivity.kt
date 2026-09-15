package com.trueshape.truemeteo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
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
    private lateinit var infoText: TextView
    private lateinit var permissionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(48, 96, 48, 48)

        infoText = TextView(this)
        infoText.text = "TrueMeteo installato.\n\n" +
                "Tieni premuto sulla schermata Home, scegli 'Widget', " +
                "cerca 'TrueMeteo' e trascinalo sulla home."
        infoText.textSize = 16f
        layout.addView(infoText)

        permissionButton = Button(this)
        permissionButton.text = "Concedi permesso di posizione"
        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        buttonParams.topMargin = 64
        buttonParams.gravity = Gravity.CENTER_HORIZONTAL
        permissionButton.layoutParams = buttonParams
        permissionButton.setOnClickListener { onPermissionButtonClicked() }
        layout.addView(permissionButton)

        setContentView(layout)

        refreshUiState()
    }

    override fun onResume() {
        super.onResume()
        refreshUiState()
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun refreshUiState() {
        if (hasLocationPermission()) {
            permissionButton.visibility = android.view.View.GONE
            WeatherWidgetProvider.requestImmediateUpdate(this)
        } else {
            permissionButton.visibility = android.view.View.VISIBLE
        }
    }

    private fun onPermissionButtonClicked() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            // Prima richiesta, oppure negato con "Non chiedere più": in questo
            // secondo caso il sistema non mostra più il popup, quindi si apre
            // direttamente la pagina permessi dell'app nelle Impostazioni.
            val alreadyAsked = getPreferences(0).getBoolean("asked_location", false)
            if (!alreadyAsked) {
                getPreferences(0).edit().putBoolean("asked_location", true).apply()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_CODE
                )
            } else {
                openAppSettings()
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            refreshUiState()
        }
    }
}
