package com.example.corsa.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.utils.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean,
    val error: AppError = AppError.Absent
)

data class AuthActions(
    val loginWithEmail: (email: String, password: String) -> Unit,
    val registerWithEmail: (email: String, password: String) -> Unit,
    val resetPassword: (email: String) -> Unit
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    val authActions = AuthActions(
        loginWithEmail = ::loginWithEmail,
        registerWithEmail = ::registerWithEmail,
        resetPassword = ::resetPassword
    )

    private val _authState = MutableStateFlow(AuthState(
        isLoading = false
    ))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.updateState(
                isLoading = true,
                error = AppError.Absent,
            )
            try {
                repository.login(email, password)
            } catch (e: Exception) {
                _authState.updateState(error = AppError.Present(e.message ?: "Error while login"))
            } finally {
                _authState.updateState(
                    isLoading = false
                )
            }
        }
    }

    private fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.updateState(
                isLoading = true,
                error = AppError.Absent,
            )
            try {
                repository.register(email, password)
            } catch (e: Exception) {
                _authState.updateState(error = AppError.Present(e.message ?: "Error while registration"))
            } finally {
                _authState.updateState(
                    isLoading = false
                )
            }
        }
    }

    private fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                repository.resetPassword(email)
            } catch (e: Exception) {
                _authState.updateState(error = AppError.Present(e.message ?: "Error while password reset"))
            }
        }
    }

    private fun MutableStateFlow<AuthState>.updateState(
        isLoading: Boolean? = null,
        error: AppError? = null,
    ) {
        value = value.copy(
            isLoading = isLoading ?: value.isLoading,
            error = error ?: value.error
        )
    }
}
