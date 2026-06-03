package com.example.corsa.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.utils.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginState(
    val isLoading: Boolean = false,
    val error: AppError = AppError.Absent,
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val showResetDialog: Boolean = false
)

data class LoginActions(
    val loginWithEmail: () -> Unit,
    val resetPassword: (email: String) -> Unit,
    val clearError: () -> Unit,
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onTogglePasswordVisibility: () -> Unit,
    val onShowResetDialog: (Boolean) -> Unit
)

class LoginViewModel(
    private val authRepository: AuthRepository
): ViewModel() {

    val loginActions = LoginActions(
        loginWithEmail = ::loginWithEmail,
        resetPassword = ::resetPassword,
        clearError = ::clearError,
        onEmailChange = { _loginState.updateState(email = it) },
        onPasswordChange = { _loginState.updateState(password = it) },
        onTogglePasswordVisibility = { _loginState.updateState(passwordVisible = !_loginState.value.passwordVisible) },
        onShowResetDialog = { _loginState.updateState(showResetDialog = it) }
    )

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    private fun loginWithEmail() {
        viewModelScope.launch {
            _loginState.updateState(
                isLoading = true,
                error = AppError.Absent,
            )
            try {
                authRepository.login(
                    _loginState.value.email,
                    _loginState.value.password
                )
            } catch (e: Exception) {
                _loginState.updateState(error = AppError.Present(e.message ?: "Error while login"))
            } finally {
                _loginState.updateState(
                    isLoading = false
                )
            }
        }
    }

    private fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                authRepository.resetPassword(email)
            } catch (e: Exception) {
                _loginState.updateState(error = AppError.Present(e.message ?: "Error while password reset"))
            }
        }
    }

    private fun clearError() {
        _loginState.updateState(
            error = AppError.Absent
        )
    }

    private fun MutableStateFlow<LoginState>.updateState(
        isLoading: Boolean? = null,
        error: AppError? = null,
        email: String? = null,
        password: String? = null,
        passwordVisible: Boolean? = null,
        showResetDialog: Boolean? = null,
    ) {
        value = value.copy(
            isLoading = isLoading ?: value.isLoading,
            error = error ?: value.error,
            email = email ?: value.email,
            password = password ?: value.password,
            passwordVisible = passwordVisible ?: value.passwordVisible,
            showResetDialog = showResetDialog ?: value.showResetDialog,
        )
    }
}