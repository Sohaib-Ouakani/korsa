package com.example.corsa.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.repositories.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class StartDestination {
    Loading,
    Auth,
    Home
}

class AuthStateViewModel(
    authRepository: AuthRepository
) : ViewModel() {

    val startDestination = MutableStateFlow(StartDestination.Loading)

    init {
        authRepository.sessionStatus
            .map { status ->
                when (status) {
                    is SessionStatus.Authenticated -> StartDestination.Home
                    is SessionStatus.Initializing -> StartDestination.Loading
                    else -> StartDestination.Auth
                }
            }
            .distinctUntilChanged()
            .onEach { startDestination.value = it }
            .launchIn(viewModelScope)
    }
}