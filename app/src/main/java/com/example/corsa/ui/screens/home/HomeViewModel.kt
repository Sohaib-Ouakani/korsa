package com.example.corsa.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.location.LocationProvider
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.utils.AppError
import com.example.corsa.utils.WeatherCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class LocationInfo(
    val cityName: String? = null,
    val weatherCode: WeatherCondition = WeatherCondition.UNKNOWN
)
data class HomeState(
    val goalKm: Float,
    val currentKm: Float,
    val progress: Float,
    val locationInfo: LocationInfo = LocationInfo(),
    val myProfileUrl: String? = null,
    val appError: AppError = AppError.Absent
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
class HomeViewModel(
    private val profilesRepository: ProfilesRepository,
    private val locationProvider: LocationProvider
): ViewModel() {
    private val _state = MutableStateFlow<HomeState?>(null)
    val state: StateFlow<HomeState?> = _state

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val loaded = profilesRepository.getMyProfile()
                val weeklyKm = profilesRepository.weeklyKmByUserId(loaded.id)
                val goalKm = loaded.level * 10f

                _state.value = HomeState(
                    goalKm = goalKm,
                    currentKm = weeklyKm,
                    progress = weeklyKm / goalKm,
                    myProfileUrl = if(loaded.avatarPath != null) profilesRepository.avatarUrl(loaded.avatarPath) else null
                )
                launch {
                    var locationInfo = LocationInfo()
                    var appError: AppError = AppError.Absent
                    try {
                        locationInfo = getLocationInfo()
                    } catch (e: Exception) {
                        appError = AppError.Present(e.message ?: "Failed to fetch location info")
                    }
                    _state.updateState(locationInfo = locationInfo, appError = appError)
                }
            } catch (e: Exception) {
                _state.updateState(appError = AppError.Present(e.message ?: "Failed to load profile"))
            }
        }
    }

    private suspend fun getLocationInfo(): LocationInfo{
        val location = locationProvider.locationFlow(intervalMs = 0L).first()
        return LocationInfo(
            cityName = getCityName(location.latitude, location.longitude),
            weatherCode = getWeather(location.latitude, location.longitude)
        )
    }

    private suspend fun getCityName(lat: Double, lon: Double): String? {
        return withContext(Dispatchers.IO) {
            val url = ApiEndpoint.ReverseGeocode(lat, lon).url

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "CorsaApp/1.0")

            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)

            val address = json.getJSONObject("address")
            address.optString("city")
                .ifEmpty { address.optString("town") }
                .ifEmpty { address.optString("village") }
                .ifEmpty { null }
        }
    }

    private suspend fun getWeather(lat: Double, lon: Double): WeatherCondition {
        return withContext(Dispatchers.IO) {
            val weatherUrl = ApiEndpoint.WeatherForecast(lat, lon).url
            val weatherConn = URL(weatherUrl).openConnection() as HttpURLConnection
            val weatherJson = JSONObject(weatherConn.inputStream.bufferedReader().readText())
            val weatherCode = weatherJson.getJSONObject("current").optInt("weather_code")

            WeatherCondition.fromWmoCode(weatherCode)
        }
    }

    private fun MutableStateFlow<HomeState?>.updateState(
        goalKm: Float? = null,
        currentKm: Float? = null,
        progress: Float? = null,
        locationInfo: LocationInfo? = null,
        myProfileUrl: String? = null,
        appError: AppError? = null
    ) {
        val current = value ?: return
        value = current.copy(
            goalKm = goalKm ?: current.goalKm,
            currentKm = currentKm ?: current.currentKm,
            progress = progress ?: current.progress,
            locationInfo = locationInfo ?: current.locationInfo,
            myProfileUrl = myProfileUrl ?: current.myProfileUrl,
            appError = appError ?: current.appError
        )
    }
}