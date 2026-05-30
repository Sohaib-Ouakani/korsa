package com.example.corsa.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.ProfileUpdate
import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.data.repositories.ProfilesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SettingsState {
    data object Idle: SettingsState
    data object Loading : SettingsState
    data object Success : SettingsState
    data class Error(val message: String, val id: Long = System.currentTimeMillis()): SettingsState
}

data class SettingsInfo(
    val currentUsername: String,
    val currentEmail: String,
    val isEmailUser: Boolean
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val profilesRepository: ProfilesRepository
) : ViewModel() {
    private val _settingsInfo = MutableStateFlow<SettingsInfo?>(null)
    val settingsInfo: StateFlow<SettingsInfo?> = _settingsInfo.asStateFlow()

    private val _settingsState = MutableStateFlow<SettingsState>(SettingsState.Loading)
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _settingsState.value = SettingsState.Loading
            try {
                val profile = profilesRepository.getMyProfile()
                val email = authRepository.getEmail()
                val isEmailUser = authRepository.isEmailUser()
                _settingsInfo.value = SettingsInfo(profile.username, email, isEmailUser)
                _settingsState.value = SettingsState.Idle
            } catch (e: Exception) {
                _settingsState.value = SettingsState.Error(
                    message = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _settingsState.value = SettingsState.Loading
            try {
                authRepository.logout()
                _settingsState.value = SettingsState.Success
            } catch (e: Exception) {
                _settingsState.value = SettingsState.Error(
                    message = e.message ?: "Logout failed"
                )
            }
        }
    }

    fun saveNewUsername(newUsername: String) {
        viewModelScope.launch {
            _settingsState.value = SettingsState.Loading
            try {
                val updatedProfile = profilesRepository.updateProfile(ProfileUpdate(username = newUsername.trim()))
                _settingsInfo.value = _settingsInfo.value?.copy(currentUsername = updatedProfile.username)
                _settingsState.value = SettingsState.Success
            } catch (e: Exception) {
                _settingsState.value = SettingsState.Error(
                    message = e.message ?: "Failed to update username"
                )
            }
        }
    }

    fun saveNewPassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _settingsState.value = SettingsState.Loading
            try {
                authRepository.updatePassword(oldPassword, newPassword.trim())
                _settingsState.value = SettingsState.Success
            } catch (e: Exception) {
                _settingsState.value = SettingsState.Error(
                    message = e.message ?: "Failed to update password"
                )
            }
        }
    }

    fun clearError() {
        _settingsState.value = SettingsState.Idle
    }
}