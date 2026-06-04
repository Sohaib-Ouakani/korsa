package com.example.corsa.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.corsa.data.model.ProfileUpdate
import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.data.repositories.NotificationPreferencesRepository
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.service.notification.WeeklyChallengeScheduler
import com.example.corsa.utils.AppError
import com.example.corsa.utils.Option
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class UIState(
    val newUsername: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val newPasswordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val showReAuthDialog: Boolean = false,
    val currentPassword: String = "",
    val currentPasswordVisible: Boolean = false
)

data class SettingsState(
    val isLoading: Boolean = true,
    val currentUsername: String = "",
    val currentEmail: String = "",
    val isEmailUser: Boolean = false,
    val avatarUrl: String? = null,
    val weeklyNotificationEnabled: Boolean = false,
    val uiState: UIState = UIState(),
    val error: AppError = AppError.Absent,
)

data class OnUIStateChange(
    val onNewUsernameChange: (String) -> Unit,
    val onNewPasswordChange: (String) -> Unit,
    val onNewPasswordVisibleChange: (Boolean) -> Unit,
    val onConfirmPasswordChange: (String) -> Unit,
    val onConfirmPasswordVisibleChange: (Boolean) -> Unit,
    val onShowReAuthDialogChange: (Boolean) -> Unit,
    val onCurrentPasswordChange: (String) -> Unit,
    val onCurrentPasswordVisibleChange: (Boolean) -> Unit,
)

data class SettingsActions(
    val logout: () -> Unit,
    val saveNewUsername: (newUsername: String) -> Unit,
    val saveNewPassword: (oldPassword: String, newPassword: String) -> Unit,
    val uploadAvatar: (imageBytes: ByteArray, mimeType: String) -> Unit,
    val toggleWeeklyNotification: () -> Unit,
    val updateUIState: OnUIStateChange,
    val clearError: () -> Unit,
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val profilesRepository: ProfilesRepository,
    private val notificationPreferencesRepository: NotificationPreferencesRepository,
    private val workManager: WorkManager
) : ViewModel() {

    val settingsActions = SettingsActions(
        logout = ::logout,
        saveNewUsername = ::saveNewUsername,
        saveNewPassword = ::saveNewPassword,
        uploadAvatar = ::uploadAvatar,
        clearError = ::clearError,
        toggleWeeklyNotification = ::toggleWeeklyNotification,
        updateUIState = OnUIStateChange(
            onNewUsernameChange = { _settingsState.updateState(uiState = _settingsState.value.uiState.copy(newUsername = it)) },
            onNewPasswordChange = { _settingsState.updateState(uiState = _settingsState.value.uiState.copy(newPassword = it)) },
            onConfirmPasswordChange = { _settingsState.updateState(uiState = _settingsState.value.uiState.copy(confirmPassword = it)) },
            onNewPasswordVisibleChange = { _settingsState.updateState(uiState = _settingsState.value.uiState.copy(newPasswordVisible = it)) },
            onConfirmPasswordVisibleChange = { _settingsState.updateState(uiState = _settingsState.value.uiState.copy(confirmPasswordVisible = it)) },
            onShowReAuthDialogChange = { _settingsState.updateState(uiState = _settingsState.value.uiState.copy(showReAuthDialog = it)) },
            onCurrentPasswordChange = { _settingsState.updateState(uiState = _settingsState.value.uiState.copy(currentPassword = it)) },
            onCurrentPasswordVisibleChange = { _settingsState.updateState(uiState = _settingsState.value.uiState.copy(currentPasswordVisible = it)) },
        )
    )

    private val _settingsState = MutableStateFlow(SettingsState())
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
                val weeklyNotificationEnabled = notificationPreferencesRepository.weeklyNotificationEnabled.first()
                _settingsState.updateState(
                    currentUsername = profile.username,
                    currentEmail = email,
                    isEmailUser = isEmailUser,
                    avatarUrl = Option.Present(
                        profile.avatarPath?.let {
                            profilesRepository.avatarUrl(it) + "?t=${profile.updatedAt}"
                        }
                    ),
                    weeklyNotificationEnabled = weeklyNotificationEnabled,
                    error = AppError.Absent,
                )
            } catch (e: Exception) {
                _settingsState.updateState(error = AppError.Present(e.message ?: "Error while loading profile"))
            } finally {
                _settingsState.updateState(isLoading = false)
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _settingsState.updateState(
                isLoading = true,
                error = AppError.Absent,
            )
            try {
                authRepository.logout()
            } catch (e: Exception) {
                _settingsState.updateState(error = AppError.Present(e.message ?: "Error while logging out"))
            } finally {
                _settingsState.updateState(isLoading = false)
            }
        }
    }

    private fun saveNewUsername(newUsername: String) {
        viewModelScope.launch {
            _settingsState.updateState(
                isLoading = true,
                error = AppError.Absent,
            )
            try {
                val updatedProfile = profilesRepository.updateProfile(ProfileUpdate(username = newUsername.trim()))
                _settingsState.updateState(currentUsername = updatedProfile.username)
            } catch (e: Exception) {
                _settingsState.updateState(error = AppError.Present(e.message ?: "Error while updating username"))
            } finally {
                _settingsState.updateState(isLoading = false)
            }
        }
    }

    private fun saveNewPassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _settingsState.updateState(
                isLoading = true,
                error = AppError.Absent,
            )
            try {
                authRepository.updatePassword(oldPassword.trim(), newPassword.trim())
            } catch (e: Exception) {
                _settingsState.updateState(error = AppError.Present(e.message ?: "Error while updating password"))
            } finally {
                _settingsState.updateState(isLoading = false)
            }
        }
    }

    private fun uploadAvatar(imageBytes: ByteArray, mimeType: String) {
        viewModelScope.launch {
            _settingsState.updateState(
                isLoading = true,
                error = AppError.Absent,
            )
            try {
                val newPath = profilesRepository.uploadAvatar(imageBytes, mimeType)
                val newUrl = profilesRepository.avatarUrl(newPath) + "?t=${System.currentTimeMillis()}"
                _settingsState.updateState(avatarUrl = Option.Present(newUrl))
            } catch (e: Exception) {
                _settingsState.updateState(error = AppError.Present(e.message ?: "Error while updating avatar"))
            } finally {
                _settingsState.updateState(isLoading = false)
            }
        }
    }

    private fun toggleWeeklyNotification() {
        viewModelScope.launch {
            _settingsState.updateState(
                // not updating loading status because visually would result strange
                error = AppError.Absent,
            )
            try {
                val enabled = _settingsState.value.weeklyNotificationEnabled
                notificationPreferencesRepository.setWeeklyNotificationEnabled(!enabled)
                if (enabled) {
                    WeeklyChallengeScheduler.cancel(workManager)
                } else {
                    WeeklyChallengeScheduler.schedule(workManager)
                }
                _settingsState.updateState(weeklyNotificationEnabled = !enabled)
            } catch (e: Exception) {
                _settingsState.updateState(error = AppError.Present(e.message ?: "Error while updating notification setting"))
            }
        }
    }

    private fun clearError() {
        _settingsState.updateState(error = AppError.Absent)
    }

    private fun MutableStateFlow<SettingsState>.updateState(
        isLoading: Boolean? = null,
        currentUsername: String? = null,
        currentEmail: String? = null,
        isEmailUser: Boolean? = null,
        avatarUrl: Option<String> = Option.Absent,
        error: AppError? = null,
        weeklyNotificationEnabled: Boolean? = null,
        uiState: UIState? = null
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
            weeklyNotificationEnabled = weeklyNotificationEnabled ?: value.weeklyNotificationEnabled,
            error = error ?: value.error,
            uiState = uiState ?: value.uiState
        )
    }
}
