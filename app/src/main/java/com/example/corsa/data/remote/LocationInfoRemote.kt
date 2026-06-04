package com.example.corsa.data.remote

import com.example.corsa.data.location.LocationProvider
import com.example.corsa.utils.WeatherCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.text.ifEmpty

data class LocationInfo(
    val cityName: String? = null,
    val weatherCode: WeatherCondition = WeatherCondition.UNKNOWN
)

sealed class ApiEndpoint {
    abstract val url: String

    data class ReverseGeocode(val lat: Double, val lon: Double) : ApiEndpoint() {
        override val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json"
    }

    data class WeatherForecast(val lat: Double, val lon: Double) : ApiEndpoint() {
        override val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=weather_code"
    }
}

class LocationInfoRemote(
    private val locationProvider: LocationProvider,

    ){

    var locationInfo: LocationInfo? = null
    private var timestamp: Long = 0L
    private val ttlMs = 10 * 60 * 1000L

    val isValid get() = locationInfo != null &&
            System.currentTimeMillis() - timestamp < ttlMs

    suspend fun getLocationInfo(): LocationInfo {
        if (!isValid) {
            val location = locationProvider.locationFlow(intervalMs = 0L).first()

            val cityName = try {
                getCityName(location.latitude, location.longitude)
            } catch (e: Exception) {
                null
            }

            val weatherCode = try {
                getWeather(location.latitude, location.longitude)
            } catch (e: Exception) {
                WeatherCondition.UNKNOWN
            }

            locationInfo = LocationInfo(
                cityName = cityName,
                weatherCode = weatherCode
            )
            timestamp = System.currentTimeMillis()
        }
        return locationInfo!!
    }
    suspend fun getCityName(lat: Double, lon: Double): String? {
        return withContext(Dispatchers.IO) {
            val connection = URL(ApiEndpoint.ReverseGeocode(lat, lon).url)
                .openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("User-Agent", "CorsaApp/1.0 (your@email.com)") // Nominatim requires contact info
            connection.setRequestProperty("Accept", "application/json")

            try {
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val address = json.getJSONObject("address")
                address.optString("city")
                    .ifEmpty { address.optString("town") }
                    .ifEmpty { address.optString("village") }
                    .ifEmpty { null }
            } finally {
                connection.disconnect()
            }
        }
    }

    suspend fun getWeather(lat: Double, lon: Double): WeatherCondition {
        return withContext(Dispatchers.IO) {
            val connection = URL(ApiEndpoint.WeatherForecast(lat, lon).url)
                .openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("User-Agent", "CorsaApp/1.0")

            try {
                val json = JSONObject(connection.inputStream.bufferedReader().readText())
                val weatherCode = json.getJSONObject("current").optInt("weather_code")
                WeatherCondition.fromWmoCode(weatherCode)
            } finally {
                connection.disconnect()
            }
        }
    }

}