package com.example.corsa.ui.screens.profiledetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.ui.composables.RunEntry
import com.example.corsa.ui.composables.UserRankEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ProfileDetailUiState {
    data object Loading : ProfileDetailUiState
    data class Error(val message: String) : ProfileDetailUiState
    data class Success(
        val userInfo: UserRankEntry,
        val runs: List<RunEntry>
    ) : ProfileDetailUiState
}

class ProfileDetailViewModel(
    private val profilesRepository: ProfilesRepository,
    private val runsRepository: RunsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _state = MutableStateFlow<ProfileDetailUiState>(ProfileDetailUiState.Loading)
    val state: StateFlow<ProfileDetailUiState> = _state.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = ProfileDetailUiState.Loading
            try {
                // coroutineScope ensures both are canceled if either throws
                val (profile, runs) = coroutineScope {
                    val profileDeferred = async { profilesRepository.getProfileByUserId(userId) }
                    val runsDeferred = async { runsRepository.getRunsByUserId(userId) }
                    Pair(profileDeferred.await(), runsDeferred.await())
                }

                ProfileDetailUiState.Success(
                    userInfo = profile,
                    runs = runs.map { run ->
                        RunEntry(
                            userId      = run.userId,
                            displayName = profile.displayName,
                            avatarUrl   = profile.avatarUrl,
                            startTime   = run.startTime.toString(),
                            pathUrl     = null,
                            distance    = run.distanceMeters / 1000.0
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = ProfileDetailUiState.Error(
                    e.message ?: "Errore sconosciuto."
                )
            }
        }
    }

    fun toggleFollow() {
        _isFollowing.update { !it }
        // TODO: chiamata reale al repository
    }
}