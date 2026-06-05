package com.example.corsa.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.remote.LocationInfo
import com.example.corsa.data.remote.LocationInfoRemote
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.utils.AppError
import com.example.corsa.utils.goalFromLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeState(
    val goalKm: Float = 0f,
    val currentKm: Float = 0f,
    val progress: Float = 0f,
    val locationInfo: LocationInfo = LocationInfo(),
    val myProfileUrl: String? = null,
    val appError: AppError = AppError.Absent,
    val isLoading: Boolean
)

data class HomeActions(
    val updateLocationLabelData: () -> Unit,
)


class HomeViewModel(
    private val profilesRepository: ProfilesRepository,
    private val locationInfoRemote: LocationInfoRemote
): ViewModel() {
    val homeActions = HomeActions(
        ::updateLocationLabelData
    )
    private val _state = MutableStateFlow(HomeState(
        isLoading = true
    ))
    val state: StateFlow<HomeState> = _state

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val loaded = profilesRepository.getMyProfile()
                val weeklyKm = profilesRepository.weeklyKmByUserId(loaded.id)
                val goalKm = goalFromLevel(loaded.level)
                val profileUrl = if(loaded.avatarPath != null) {
                    profilesRepository.avatarUrl(loaded.avatarPath)
                } else null

                _state.updateState(
                    goalKm = goalKm,
                    currentKm = weeklyKm,
                    progress = weeklyKm / goalKm,
                    myProfileUrl = profileUrl
                )
            } catch (e: Exception) {
                _state.updateState(appError = AppError.Present(e.message ?: "Failed to load profile"))
                Log.e("HomeViewModel", e.message ?: "Failed to load profile")
            } finally {
                _state.updateState(isLoading = false)
            }
        }
    }

    private fun updateLocationLabelData() {
        viewModelScope.launch {
            var appError: AppError = AppError.Absent
            val locationInfo = try {
                locationInfoRemote.getLocationInfo()
            } catch (e: Exception) {
                appError = AppError.Present(e.message ?: "Failed to fetch location info")
                Log.e("HomeViewModel", e.message ?: "Failed to fetch location info")
                LocationInfo()
            }
            _state.updateState(locationInfo = locationInfo, appError = appError)
        }
    }


    private fun MutableStateFlow<HomeState>.updateState(
        goalKm: Float? = null,
        currentKm: Float? = null,
        progress: Float? = null,
        locationInfo: LocationInfo? = null,
        myProfileUrl: String? = null,
        appError: AppError? = null,
        isLoading: Boolean? = null
    ) {
        value = value.copy(
            goalKm = goalKm ?: value.goalKm,
            currentKm = currentKm ?: value.currentKm,
            progress = progress ?: value.progress,
            locationInfo = locationInfo ?: value.locationInfo,
            myProfileUrl = myProfileUrl ?: value.myProfileUrl,
            appError = appError ?: value.appError,
            isLoading = isLoading ?: value.isLoading
        )
     }
}