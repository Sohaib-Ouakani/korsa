package com.example.corsa.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.repositories.AuthRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository
): ViewModel() {
    fun logout(){
        viewModelScope.launch {
            authRepository.logout() // TODO: ignoring the result is not ok
        }
    }
}