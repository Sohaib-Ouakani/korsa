package com.example.corsa.ui.screens.profiledetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.Run
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.ui.composables.UserEntry
import com.example.corsa.utils.AppError
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileDetailState (
    val isLoading : Boolean,
    val runs: List<Run> = listOf(),
    val userEntry: UserEntry  = UserEntry("", null, 0f, 0, 0, 0f),
    val isFollowing: Boolean = false,
    val error: AppError = AppError.Absent,
)

data class ProfileDetailAction(
    val toggleFollow: () -> Unit
)

class ProfileDetailViewModel(
    private val profilesRepository: ProfilesRepository,
    private val runsRepository: RunsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val profileDetailAction = ProfileDetailAction(
        toggleFollow = ::toggleFollow
    )
    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _state = MutableStateFlow(ProfileDetailState(isLoading = true))
    val state: StateFlow<ProfileDetailState> = _state.asStateFlow()


    init {
        loadProfile()
        loadFollowState()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.updateState(
                isLoading = true,
                error = AppError.Absent
            )
            try {
                val (runs, userEntry) = coroutineScope {
                    val runsDeferred      = async { runsRepository.getRunsByUserId(userId) }
                    val userEntryDeferred = async { profilesRepository.getUserEntryByUserId(userId) }
                    Pair(
                        runsDeferred.await(),
                        userEntryDeferred.await()
                    )
                }

                _state.updateState(
                    isLoading = false,
                    runs = runs,
                    userEntry = userEntry,
                    error = AppError.Absent,
                )
            } catch (e: Exception) {
                _state.updateState(error = AppError.Present(e.message ?: "Error while loading profile"))
            } finally {
                _state.updateState(isLoading = false)
            }
        }
    }

    private fun loadFollowState() {
        viewModelScope.launch {
            _state.updateState(
                isLoading = true,
                error = AppError.Absent
            )
            try {
                _state.updateState(isFollowing =  profilesRepository.getIfIFollowAProfileByUserId(userId))
            } catch (e: Exception) {
                _state.updateState(error = AppError.Present(e.message ?: "Error while loading follow profile"))
            } finally {
                _state.updateState(isLoading = false)
            }
        }
    }

    fun toggleFollow() {
        viewModelScope.launch {
            _state.updateState(
                isLoading = true,
                error = AppError.Absent
            )
            try {
                if (_state.value.isFollowing) {
                    profilesRepository.stopFollowToAProfileByUserId(userId)
                } else {
                    profilesRepository.addFollowToAProfileByUserId(userId)
                }
                _state.updateState(isFollowing = !_state.value.isFollowing)
            } catch (e: Exception) {
                _state.updateState(error = AppError.Present(e.message ?: "Error while loading follow profile"))
            } finally {
                _state.updateState(isLoading = false)
            }
        }
    }

    private fun MutableStateFlow<ProfileDetailState>.updateState(
         isLoading : Boolean? = null,
         runs: List<Run>? = null,
         userEntry: UserEntry?  = null,
         isFollowing: Boolean? = null,
         error: AppError? = null,
    ){
        value = value.copy(
            isLoading = isLoading ?: value.isLoading,
            runs = runs ?: value.runs,
            userEntry = userEntry ?: value.userEntry,
            isFollowing = isFollowing ?: value.isFollowing,
            error = error?: value.error,
            )
    }
}