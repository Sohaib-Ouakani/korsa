package com.example.corsa.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.utils.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: AppError = AppError.Absent
)

data class RegisterActions(
    val registerWithEmail: () -> Unit,
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onTogglePasswordVisibility: () -> Unit,
    val clearError: () -> Unit
)


class RegisterViewModel(
    private val authRepository: AuthRepository
): ViewModel() {


    val registerActions = RegisterActions(
        registerWithEmail = ::registerWithEmail,
        onEmailChange = { _registerState.updateState(email = it) },
        onPasswordChange = { _registerState.updateState(password = it) },
        onTogglePasswordVisibility = {
            _registerState.updateState(passwordVisible = !_registerState.value.passwordVisible)
        },
        clearError = ::clearError
    )

    private val _registerState = MutableStateFlow(RegisterState())
    val registerState = _registerState.asStateFlow()

    private fun registerWithEmail() {
        viewModelScope.launch {
            _registerState.updateState(
                isLoading = true,
                error = AppError.Absent,
            )
            try {
                authRepository.register(
                    _registerState.value.email,
                    _registerState.value.password
                )
            } catch (e: Exception) {
                _registerState.updateState(error = AppError.Present(e.message ?: "Error while registration"))
            } finally {
                _registerState.updateState(
                    isLoading = false
                )
            }
        }
    }

    private fun clearError() {
        _registerState.updateState(
            error = AppError.Absent
        )
    }

    private fun MutableStateFlow<RegisterState>.updateState(
        email: String? = null,
        password: String? = null,
        passwordVisible: Boolean? = null,
        isLoading: Boolean? = null,
        error: AppError? = null,
    ) {
        value = value.copy(
            email = email ?: value.email,
            password = password ?: value.password,
            passwordVisible = passwordVisible ?: value.passwordVisible,
            isLoading = isLoading ?: value.isLoading,
            error = error ?: value.error
        )
    }
}
