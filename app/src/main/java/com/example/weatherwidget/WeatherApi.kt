package com.example.weatherwidget

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class DayForecast(val label: String, val icon: Int, val tempMin: Int, val tempMax: Int)

data class HourForecast(val label: String, val icon: Int, val temp: Int)

data class WeatherResult(
    val currentTemp: Int,
    val currentIcon: Int,
    val currentDesc: String,
    val currentHumidity: Int,
    val rainProbability: Int,
    val hourlyForecast: List<HourForecast>,
    val forecast: List<DayForecast>
)

object WeatherApi {

    // Mappa i weather code di Open-Meteo (WMO) a icona (Weather Icons, MIT/OFL) + descrizione
    private fun codeToIconDesc(code: Int): Pair<Int, String> = when (code) {
        0 -> R.drawable.ic_weather_sunny to "Sereno"
        1, 2 -> R.drawable.ic_weather_partly_cloudy to "Poco nuvoloso"
        3 -> R.drawable.ic_weather_cloudy to "Nuvoloso"
        45, 48 -> R.drawable.ic_weather_fog to "Nebbia"
        51, 53, 55, 56, 57 -> R.drawable.ic_weather_drizzle to "Pioviggine"
        61, 63, 65, 66, 67 -> R.drawable.ic_weather_rain to "Pioggia"
        71, 73, 75, 77 -> R.drawable.ic_weather_snow to "Neve"
        80, 81, 82 -> R.drawable.ic_weather_showers to "Rovesci"
        95, 96, 99 -> R.drawable.ic_weather_thunderstorm to "Temporali"
        else -> R.drawable.ic_weather_cloudy to "Variabile"
    }

    private val giorni = arrayOf("Dom", "Lun", "Mar", "Mer", "Gio", "Ven", "Sab")

    /**
     * Chiama l'API gratuita Open-Meteo (nessuna API key richiesta).
     * Deve essere eseguita fuori dal thread principale (vedi WeatherWidgetProvider).
     */
    fun fetchWeather(lat: Double, lon: Double): WeatherResult? {
        return try {
            val url = URL(
                "https://api.open-meteo.com/v1/forecast" +
                        "?latitude=$lat&longitude=$lon" +
                        "&current=temperature_2m,weather_code,relative_humidity_2m" +
                        "&hourly=temperature_2m,weather_code,precipitation_probability" +
                        "&forecast_hours=24" +
                        "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                        "&forecast_days=7&timezone=auto"
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)

            val current = json.getJSONObject("current")
            val currentTemp = current.getDouble("temperature_2m").toInt()
            val currentCode = current.getInt("weather_code")
            val currentHumidity = current.getDouble("relative_humidity_2m").toInt()
            val (icon, desc) = codeToIconDesc(currentCode)

            val hourly = json.getJSONObject("hourly")
            val hourTimes = hourly.getJSONArray("time")
            val hourCodes = hourly.getJSONArray("weather_code")
            val hourTemps = hourly.getJSONArray("temperature_2m")
            val hourRainProb = hourly.getJSONArray("precipitation_probability")

            val rainProbability = if (hourRainProb.length() > 0) hourRainProb.getInt(0) else 0

            val hourlyList = mutableListOf<HourForecast>()
            for (i in 0 until hourTimes.length()) {
                val timeStr = hourTimes.getString(i) // formato YYYY-MM-DDTHH:MM
                val hourLabel = timeStr.substringAfter("T").substringBefore(":")
                val (hIcon, _) = codeToIconDesc(hourCodes.getInt(i))
                val hTemp = hourTemps.getDouble(i).toInt()
                hourlyList.add(HourForecast(hourLabel, hIcon, hTemp))
            }

            val daily = json.getJSONObject("daily")
            val times = daily.getJSONArray("time")
            val codes = daily.getJSONArray("weather_code")
            val maxTemps = daily.getJSONArray("temperature_2m_max")
            val minTemps = daily.getJSONArray("temperature_2m_min")

            val forecastList = mutableListOf<DayForecast>()
            // Parte da indice 1 per saltare "oggi" e mostrare i 6 giorni successivi
            for (i in 1 until times.length()) {
                val dateStr = times.getString(i) // formato YYYY-MM-DD
                val parts = dateStr.split("-")
                val cal = java.util.GregorianCalendar(
                    parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt()
                )
                val label = giorni[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1]
                val (dIcon, _) = codeToIconDesc(codes.getInt(i))
                val tMin = minTemps.getDouble(i).toInt()
                val tMax = maxTemps.getDouble(i).toInt()
                forecastList.add(DayForecast(label, dIcon, tMin, tMax))
            }

            WeatherResult(
                currentTemp, icon, desc, currentHumidity, rainProbability, hourlyList, forecastList
            )
        } catch (e: Exception) {
            null
        }
    }
}
