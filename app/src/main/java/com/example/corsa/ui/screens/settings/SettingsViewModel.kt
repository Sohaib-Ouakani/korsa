package com.example.corsa.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.ProfileUpdate
import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.utils.Option
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val isLoading: Boolean,
    val currentUsername: String = "",
    val currentEmail: String = "",
    val isEmailUser: Boolean = false,
    val avatarUrl: String? = null,
    val error: String? = null,
)

data class SettingsActions(
    val logout: () -> Unit,
    val saveNewUsername: (newUsername: String) -> Unit,
    val saveNewPassword: (oldPassword: String, newPassword: String) -> Unit,
    val uploadAvatar: (imageBytes: ByteArray, mimeType: String) -> Unit,
    val clearError: () -> Unit,
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val profilesRepository: ProfilesRepository
) : ViewModel() {

    val settingsActions = SettingsActions(
        logout = ::logout,
        saveNewUsername = ::saveNewUsername,
        saveNewPassword = ::saveNewPassword,
        uploadAvatar = ::uploadAvatar,
        clearError = ::clearError
    )

    private val _settingsState = MutableStateFlow(SettingsState(
        isLoading = true,
    ))
    val settingsState = _settingsState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val profile = profilesRepository.getMyProfile()
                val email = authRepository.getEmail()
                val isEmailUser = authRepository.isEmailUser()
                _settingsState.updateState(
                    isLoading = false,
                    currentUsername = profile.username,
                    currentEmail = email,
                    isEmailUser = isEmailUser,
                    avatarUrl = Option.Present(
                        profile.avatarPath?.let {
                            profilesRepository.avatarUrl(it) + "?t=${profile.updatedAt}"
                        }
                    ),
                    error = Option.Present(null),  // clear any previous error on successful load
                )
            } catch (e: Exception) {
                _settingsState.updateState(error = Option.Present(e.message ?: "Error while loading profile"))
            } finally {
                _settingsState.updateState(isLoading = false)
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _settingsState.updateState(
                isLoading = true,
                error = Option.Present(null),
            )
            try {
                authRepository.logout()
            } catch (e: Exception) {
                _settingsState.updateState(error = Option.Present(e.message ?: "Error while logging out"))
            } finally {
                _settingsState.updateState(isLoading = false)
            }
        }
    }

    private fun saveNewUsername(newUsername: String) {
        viewModelScope.launch {
            _settingsState.updateState(
                isLoading = true,
                error = Option.Present(null),
            )
            try {
                val updatedProfile = profilesRepository.updateProfile(ProfileUpdate(username = newUsername.trim()))
                _settingsState.updateState(currentUsername = updatedProfile.username)
            } catch (e: Exception) {
                _settingsState.updateState(error = Option.Present(e.message ?: "Error while updating username"))
            } finally {
                _settingsState.updateState(isLoading = false)
            }
        }
    }

    private fun saveNewPassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _settingsState.updateState(
                isLoading = true,
                error = Option.Present(null),
            )
            try {
                authRepository.updatePassword(oldPassword.trim(), newPassword.trim())
            } catch (e: Exception) {
                _settingsState.updateState(error = Option.Present(e.message ?: "Error while updating password"))
            } finally {
                _settingsState.updateState(isLoading = false)
            }
        }
    }

    private fun uploadAvatar(imageBytes: ByteArray, mimeType: String) {
        viewModelScope.launch {
            _settingsState.updateState(
                isLoading = true,
                error = Option.Present(null),
            )
            try {
                val newPath = profilesRepository.uploadAvatar(imageBytes, mimeType)
                val newUrl = profilesRepository.avatarUrl(newPath) + "?t=${System.currentTimeMillis()}"
                _settingsState.updateState(avatarUrl = Option.Present(newUrl))
            } catch (e: Exception) {
                _settingsState.updateState(error = Option.Present(e.message ?: "Error while updating avatar"))
            } finally {
                _settingsState.updateState(isLoading = false)
            }
        }
    }

    private fun clearError() {
        _settingsState.updateState(error = Option.Present(null))
    }

    private fun MutableStateFlow<SettingsState>.updateState(
        isLoading: Boolean? = null,
        currentUsername: String? = null,
        currentEmail: String? = null,
        isEmailUser: Boolean? = null,
        avatarUrl: Option<String> = Option.Absent,
        error: Option<String> = Option.Absent,
    ) {
        value = value.copy(
            isLoading = isLoading ?: value.isLoading,
            currentUsername = currentUsername ?: value.currentUsername,
            currentEmail = currentEmail ?: value.currentEmail,
            isEmailUser = isEmailUser ?: value.isEmailUser,
            avatarUrl = when (avatarUrl) {
                is Option.Absent -> value.avatarUrl
                is Option.Present -> avatarUrl.value
            },
            error = when (error) {
                is Option.Absent -> value.error
                is Option.Present -> error.value
            },
        )
    }
}
