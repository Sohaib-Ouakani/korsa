package com.example.corsa.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.Run
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.ui.composables.UserEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatsScreenViewModel(
    private val profilesRepository: ProfilesRepository,
    private val runsRepository: RunsRepository
): ViewModel() {
    private val _profile = MutableStateFlow<UserEntry?>(null)
    val profile: StateFlow<UserEntry?> = _profile
    private val _runs = MutableStateFlow<List<Run>>(emptyList())
    val runs: StateFlow<List<Run>> = _runs


    init {
        loadProfile()
        observeRuns()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            _profile.value = profilesRepository.getMyUserEntry()
            _runs.value = runsRepository.getMyRuns()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _profile.value = profilesRepository.getMyUserEntry()
            _runs.value = runsRepository.getMyRuns()
        }
    }

    private fun observeRuns() {
        viewModelScope.launch {
            val userId = profilesRepository.getMyProfile().id
            runsRepository.observeRunUpdates(userId)
                .collect { runs ->
                    _runs.value = runs
                }
        }
    }
}