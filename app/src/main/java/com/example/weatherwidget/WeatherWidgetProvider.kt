package com.example.weatherwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.concurrent.thread

class WeatherWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_MANUAL_REFRESH = "com.example.weatherwidget.ACTION_MANUAL_REFRESH"

        /** Chiamato da MainActivity dopo che il permesso è stato concesso */
        fun requestImmediateUpdate(context: Context) {
            val intent = Intent(context, WeatherWidgetProvider::class.java)
            intent.action = ACTION_MANUAL_REFRESH
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateSingleWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_MANUAL_REFRESH || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, WeatherWidgetProvider::class.java)
            )
            for (id in ids) updateSingleWidget(context, manager, id)
        }
    }

    private fun updateSingleWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget)

        val hasPermission = ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            views.setTextViewText(R.id.city_text, "Apri l'app per dare il permesso")
            manager.updateAppWidget(widgetId, views)
            return
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        // getCurrentLocation richiede una posizione fresca, invece di lastLocation
        // che è la cache dell'ultima posizione richiesta da QUALSIASI app: se nessuna
        // l'ha mai chiesta (es. dopo un'installazione pulita), lastLocation è null.
        fusedClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location == null) {
                views.setTextViewText(R.id.city_text, "Posizione non disponibile")
                manager.updateAppWidget(widgetId, views)
                return@addOnSuccessListener
            }

            // La chiamata di rete NON può stare sul thread principale
            thread {
                val result = WeatherApi.fetchWeather(location.latitude, location.longitude)
                if (result == null) {
                    views.setTextViewText(R.id.city_text, "Errore di rete")
                    manager.updateAppWidget(widgetId, views)
                    return@thread
                }

                views.setTextViewText(
                    R.id.city_text,
                    "${"%.2f".format(location.latitude)}°, ${"%.2f".format(location.longitude)}°"
                )
                views.setTextViewText(R.id.icon_text, result.currentIcon)
                views.setTextViewText(R.id.temp_text, "${result.currentTemp}°C")
                views.setTextViewText(R.id.desc_text, result.currentDesc)

                val forecastLine = result.forecast.joinToString("   ") {
                    "${it.label} ${it.icon} ${it.tempMax}°"
                }
                views.setTextViewText(R.id.forecast_text, forecastLine)

                manager.updateAppWidget(widgetId, views)
            }
        }
    }
}
