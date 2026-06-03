package com.example.corsa.ui.screens.resetpassword

import androidx.lifecycle.ViewModel
import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.utils.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ResetPasswordState(
    val password: String = "",
    val error: AppError = AppError.Absent
)

data class ResetPasswordActions(
    val resetPassword: (newPassword: String) -> Unit
)

class ResetPasswordViewModel(
    private val repository: AuthRepository
): ViewModel() {

    val resetPasswordActions = ResetPasswordActions(
        resetPassword = ::resetPassword
    )

    private val _resetPasswordState = MutableStateFlow(ResetPasswordState())
    val resetPasswordState = _resetPasswordState.asStateFlow()

    private fun resetPassword(password: String) {
        TODO()
    }
}