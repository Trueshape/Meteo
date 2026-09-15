package com.example.weatherwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
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

        // Colore di base del widget (blu cielo); l'opacità scelta dall'utente
        // viene applicata come canale alpha su questo stesso colore.
        private const val BASE_COLOR = 0xFF2C97FA.toInt()

        /** Chiamato da MainActivity dopo che il permesso è stato concesso */
        fun requestImmediateUpdate(context: Context) {
            val intent = Intent(context, WeatherWidgetProvider::class.java)
            intent.action = ACTION_MANUAL_REFRESH
            context.sendBroadcast(intent)
        }

        /** Chiamato dalla schermata di configurazione dopo il salvataggio. */
        fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            updateSingleWidget(context, manager, widgetId)
        }

        private fun updateSingleWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.weather_widget)

            val opacityPercent = WidgetPrefs.getOpacity(context, widgetId)
            val alpha = (opacityPercent * 255 / 100).coerceIn(0, 255)
            val backgroundColor = Color.argb(
                alpha, Color.red(BASE_COLOR), Color.green(BASE_COLOR), Color.blue(BASE_COLOR)
            )
            views.setInt(R.id.widget_root, "setBackgroundColor", backgroundColor)

            val configureIntent = Intent(context, WidgetConfigureActivity::class.java)
            configureIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            val configurePendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                configureIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.settings_icon, configurePendingIntent)

            val hasPermission = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                views.setTextViewText(R.id.city_text, "Apri l'app per dare il permesso")
                manager.updateAppWidget(widgetId, views)
                return
            }

            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                if (location == null) {
                    views.setTextViewText(R.id.city_text, "Posizione non disponibile")
                    manager.updateAppWidget(widgetId, views)
                    return@addOnSuccessListener
                }

                // La chiamata di rete e il geocoding NON possono stare sul thread principale
                thread {
                    val placeName = resolvePlaceName(context, location.latitude, location.longitude)
                    views.setTextViewText(R.id.city_text, placeName)

                    val result = WeatherApi.fetchWeather(location.latitude, location.longitude)
                    if (result == null) {
                        views.setTextViewText(R.id.desc_text, "Errore di rete")
                        manager.updateAppWidget(widgetId, views)
                        return@thread
                    }

                    views.setTextViewText(R.id.icon_text, result.currentIcon)
                    views.setTextViewText(R.id.temp_text, "${result.currentTemp}°C")
                    views.setTextViewText(R.id.desc_text, result.currentDesc)

                    views.removeAllViews(R.id.forecast_container)
                    for (day in result.forecast.take(6)) {
                        val dayView = RemoteViews(context.packageName, R.layout.day_column)
                        dayView.setTextViewText(R.id.day_label, day.label)
                        dayView.setTextViewText(R.id.day_icon, day.icon)
                        dayView.setTextViewText(R.id.day_temp, "${day.tempMin}°/${day.tempMax}°")
                        views.addView(R.id.forecast_container, dayView)
                    }

                    manager.updateAppWidget(widgetId, views)
                }
            }
        }

        /** Nome del luogo (es. "Milano") a partire dalle coordinate, con fallback su lat/lon. */
        private fun resolvePlaceName(context: Context, lat: Double, lon: Double): String {
            return try {
                if (!Geocoder.isPresent()) return formatCoordinates(lat, lon)
                val geocoder = Geocoder(context, java.util.Locale.ITALIAN)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                val address = addresses?.firstOrNull()
                address?.locality
                    ?: address?.subAdminArea
                    ?: address?.adminArea
                    ?: formatCoordinates(lat, lon)
            } catch (e: Exception) {
                formatCoordinates(lat, lon)
            }
        }

        private fun formatCoordinates(lat: Double, lon: Double): String =
            "${"%.2f".format(lat)}°, ${"%.2f".format(lon)}°"
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

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            WidgetPrefs.remove(context, id)
        }
    }
}
