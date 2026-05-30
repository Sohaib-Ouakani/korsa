package com.example.corsa.ui.screens.profiledetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.Run
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.ui.composables.UserEntry
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
        val userInfo: Profile,
        val runs: List<Run>,
        val userEntry: UserEntry
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
        loadFollowState()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = ProfileDetailUiState.Loading
            try {
                // coroutineScope ensures both are canceled if either throws
                val (profile, runs, userEntry) = coroutineScope {
                    val profileDeferred   = async { profilesRepository.getProfileByUserId(userId) }
                    val runsDeferred      = async { runsRepository.getRunsByUserId(userId) }
                    val userEntryDeferred = async { profilesRepository.getUserEntryByUserId(userId) }
                    Triple(
                        profileDeferred.await(),
                        runsDeferred.await(),
                        userEntryDeferred.await()
                    )
                }

                _state.value = ProfileDetailUiState.Success(
                    userInfo = profile,
                    runs = runs,
                    userEntry = userEntry
                )
            } catch (e: Exception) {
                _state.value = ProfileDetailUiState.Error(
                    e.message ?: "Errore sconosciuto."
                )
            }
        }
    }

    private fun loadFollowState() {
        viewModelScope.launch {
            try {
                _isFollowing.value = profilesRepository.getIfIFollowAProfileByUserId(userId)
            } catch (e: Exception) {
                android.util.Log.e("ProfileDetail", "loadFollowState failed: ${e.message}", e)
            }
        }
    }

    fun toggleFollow() {
        viewModelScope.launch {
            try {
                if (_isFollowing.value) {
                    profilesRepository.StopFollowToAProfileByUserId(userId)
                } else {
                    profilesRepository.AddFollowToAProfileByUserId(userId)
                }
                _isFollowing.value = !_isFollowing.value
            } catch (e: Exception) {
                android.util.Log.e("ProfileDetail", "toggleFollow failed: ${e.message}", e)
            }
        }
    }
}