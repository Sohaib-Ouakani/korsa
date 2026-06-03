package com.example.corsa.ui.screens

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.repositories.AuthRepository
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.ktor.http.ParametersBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

enum class AppSessionStatus {
    Loading,
    NotAuthenticated,
    Authenticated
}

class SessionViewModel(
    authRepository: AuthRepository,
): ViewModel() {
    private val _appSessionStatus = MutableStateFlow(AppSessionStatus.Loading)
    val appSessionStatus = _appSessionStatus.asStateFlow()

    init {
        authRepository.sessionStatus
            .map { status ->
                when (status) {
                    is SessionStatus.Authenticated -> AppSessionStatus.Authenticated
                    is SessionStatus.Initializing -> AppSessionStatus.Loading
                    else -> AppSessionStatus.NotAuthenticated
                }
            }
            .distinctUntilChanged()
            .onEach { _appSessionStatus.value = it }
            .launchIn(viewModelScope)
    }
}