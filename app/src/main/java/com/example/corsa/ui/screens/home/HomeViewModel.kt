package com.example.corsa.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.location.LocationProvider
import com.example.corsa.data.model.Profile
import com.example.corsa.data.repositories.ProfilesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class HomeState(
    val goalKm: Float,
    val currentKm: Float,
    val progress: Float,
    val locationName: String?
)
class HomeViewModel(
    private val profilesRepository: ProfilesRepository,
    private val locationProvider: LocationProvider
): ViewModel() {
    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    // Derived from profile reactively
    private val _state = MutableStateFlow<HomeState?>(null)
    val state: StateFlow<HomeState?> = _state

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val loaded = profilesRepository.getMyProfile()
            val weeklyKm = profilesRepository.weeklyKmByUserId(loaded.id)
            val goalKm = loaded.level * 10f

            _profile.value = loaded
            _state.value = HomeState(
                goalKm = goalKm,
                currentKm = weeklyKm,
                progress = weeklyKm / goalKm,
                locationName = null
            )

            launch {
                val cityName = getCityName()
                _state.update { it?.copy(locationName = cityName) }
            }
        }
    }
    private suspend fun getCityName(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val location = locationProvider.locationFlow(intervalMs = 0L).first()
                val url = "https://nominatim.openstreetmap.org/reverse?lat=${location.latitude}&lon=${location.longitude}&format=json"

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "CorsaApp/1.0")

                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                val address = json.getJSONObject("address")
                address.optString("city")
                    .ifEmpty { address.optString("town") }
                    .ifEmpty { address.optString("village") }
                    .ifEmpty { null }
            } catch (e: Exception) {
                null
            }
        }
    }
}