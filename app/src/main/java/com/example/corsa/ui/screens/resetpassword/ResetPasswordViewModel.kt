package com.example.corsa.ui.screens.resetpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.utils.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResetPasswordState(
    val isLoading: Boolean = false,
    val error: AppError = AppError.Absent,
    val password: String = "",
    val passwordVisible: Boolean = false,
)

data class ResetPasswordActions(
    val resetPassword: (onSuccess: (() -> Unit) ) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onTogglePasswordVisibility: () -> Unit,
)

class ResetPasswordViewModel(
    private val authRepository: AuthRepository
): ViewModel() {

    val resetPasswordActions = ResetPasswordActions(
        resetPassword = ::resetPassword,
        onPasswordChange = { _resetPasswordState.updateState(password = it) },
        onTogglePasswordVisibility = { _resetPasswordState.updateState(passwordVisible = !_resetPasswordState.value.passwordVisible) }
    )

    private val _resetPasswordState = MutableStateFlow(ResetPasswordState())
    val resetPasswordState = _resetPasswordState.asStateFlow()

    private fun resetPassword(onSuccess: (() -> Unit)) {
        viewModelScope.launch {
            _resetPasswordState.updateState(
                isLoading = true,
                error = AppError.Absent,
            )
            try {
                authRepository.resetPassword(_resetPasswordState.value.password)
                onSuccess()
            } catch (e: Exception) {
                _resetPasswordState.updateState(error = AppError.Present(e.message ?: "Error while reset password"))
            } finally {
                _resetPasswordState.updateState(
                    isLoading = false
                )
            }
        }
    }

    private fun MutableStateFlow<ResetPasswordState>.updateState(
        isLoading: Boolean? = null,
        error: AppError? = null,
        password: String? = null,
        passwordVisible: Boolean? = null,
    ) {
        value = value.copy(
            isLoading = isLoading ?: value.isLoading,
            error = error ?: value.error,
            password = password ?: value.password,
            passwordVisible = passwordVisible ?: value.passwordVisible,
        )
    }
}