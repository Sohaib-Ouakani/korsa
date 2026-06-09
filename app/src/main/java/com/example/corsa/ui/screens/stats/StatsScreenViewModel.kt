package com.example.corsa.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.Run
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.ui.composables.UserEntry
import com.example.corsa.utils.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatsState(
    val isLoading: Boolean,
    val profile: UserEntry = UserEntry("", null, 0f, 0, 0, 0f),
    val runs: List<Run> = listOf(),
    val error: AppError = AppError.Absent
)

data class StatsActions(
    val refreshProfile: () -> Unit,
)

class StatsScreenViewModel(
    private val profilesRepository: ProfilesRepository,
    private val runsRepository: RunsRepository
) : ViewModel() {

    val statsActions = StatsActions(
        refreshProfile = ::loadProfile,
    )

    private val _statsState = MutableStateFlow(StatsState(isLoading = true))
    val statsState = _statsState.asStateFlow()


    init {
        observeRuns()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _statsState.updateState(
                isLoading = true,
                error = AppError.Absent
            )
            try{
                val profile = profilesRepository.getMyUserEntry()
                val runs = runsRepository.getMyRuns()
                _statsState.updateState(
                    isLoading = false,
                    profile = profile,
                    runs = runs,
                    error = AppError.Absent,
                )
            } catch (e: Exception) {
                _statsState.updateState(error = AppError.Present(e.message ?: "Error while loading profile"))
            } finally {
                _statsState.updateState(isLoading = false)
            }
        }
    }

    private fun observeRuns() {
        viewModelScope.launch {
            _statsState.updateState(
                isLoading = true,
                error = AppError.Absent
            )
            try{
                val userId = profilesRepository.getMyProfile().id
                runsRepository.observeRunUpdates(userId)
                    .collect { runs ->
                        _statsState.updateState(runs = runs)
                    }
            } catch (e: Exception) {
                _statsState.updateState(error = AppError.Present(e.message ?: "Error while loading run preview"))
            } finally {
                _statsState.updateState(isLoading = false)
            }
        }
    }

    private fun MutableStateFlow<StatsState>.updateState(
        isLoading: Boolean? = null,
        profile: UserEntry? = null,
        runs: List<Run> ?= null,
        error: AppError? = null,
    ) {
        value = value.copy(
            isLoading = isLoading ?: value.isLoading,
            profile = profile ?: value.profile,
            runs = runs ?: value.runs,
            error = error?: value.error,
        )
    }
}
