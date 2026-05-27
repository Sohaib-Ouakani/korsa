package com.example.corsa.ui.screens.profiledetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.repositories.ProfileRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.ui.composables.RunEntry
import com.example.corsa.ui.composables.UserRankEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

sealed interface ProfileDetailUiState {
    data object Loading : ProfileDetailUiState
    data class Error(val message: String) : ProfileDetailUiState
    data class Success(
        val userInfo: UserRankEntry,
        val runs: List<RunEntry>
    ) : ProfileDetailUiState
}

class ProfileDetailViewModel(
    private val profileRepository: ProfileRepository,
    private val runsRepository: RunsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])

    val state = combine(
        profileRepository.getProfileById(userId),
        runsRepository.getRunsByUser(userId)
    ) { profile, runs ->
        if (profile == null) {
            ProfileDetailUiState.Error("Profilo non trovato.")
        } else {
            ProfileDetailUiState.Success(
                userInfo = profile,
                runs = runs.map { run ->
                    RunEntry(
                        userId      = run.userId,
                        displayName = profile.displayName,
                        avatarUrl   = profile.avatarUrl,
                        startTime   = run.startTime.toString(),
                        pathUrl     = null,
                        distance    = (run.distanceMeters / 1000.0)
                    )
                }
            )
        }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileDetailUiState.Loading
    )
    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    fun toggleFollow() {
        _isFollowing.update { !it }
        // TODO: chiamata reale al repository
    }
}