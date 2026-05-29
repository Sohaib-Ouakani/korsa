package com.example.corsa.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.repositories.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

sealed class StartDestination {
    object Loading : StartDestination()
    object Auth : StartDestination()
    object Home : StartDestination()
}

class AuthStateViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val startDestination = MutableStateFlow<StartDestination>(StartDestination.Loading)

    init {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        authRepository.getCurrentProfile()
                            .onSuccess { profile ->
                                startDestination.value = if (profile != null) {
                                    StartDestination.Home
                                } else {
                                    StartDestination.Auth
                                }
                            }
                            .onFailure {
                                startDestination.value = StartDestination.Auth
                            }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        startDestination.value = StartDestination.Auth
                    }
                    is SessionStatus.Initializing -> {
                        startDestination.value = StartDestination.Loading
                    }
                    is SessionStatus.RefreshFailure -> {
                        startDestination.value = StartDestination.Auth
                    }
                }
            }
        }
    }
}