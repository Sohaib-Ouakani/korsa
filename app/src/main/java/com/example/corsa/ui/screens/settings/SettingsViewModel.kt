package com.example.corsa.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.repositories.AuthRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository
): ViewModel() {
    fun logout(){
        viewModelScope.launch {
            authRepository.logout() // TODO: ignoring the result is not ok
        }
    }
}