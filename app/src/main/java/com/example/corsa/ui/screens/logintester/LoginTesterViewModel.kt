package com.example.corsa.ui.screens.logintester

import androidx.lifecycle.ViewModel
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


sealed interface LoginState {
    data object Idle : LoginState
    data object Loading : LoginState
    data object Success : LoginState
    data class Error(val message: String) : LoginState
}

class LoginTesterViewModel(private val supabase: SupabaseClient) : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state = _state.asStateFlow()

    fun onGoogleSignInSuccess() {
        _state.value = LoginState.Success
    }

    fun onSignInError(message: String) {
        _state.value = LoginState.Error(message)
    }

    fun onSignInDismissed() {
        _state.value = LoginState.Error("Sign-in dismissed.")
    }
}