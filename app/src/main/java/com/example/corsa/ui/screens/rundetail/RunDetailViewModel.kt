package com.example.corsa.ui.screens.rundetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.Run
import com.example.corsa.data.repositories.RunsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

// ── UI State ───────────────────────────────────────────────────────────────

sealed interface RunDetailUiState {
    data object Loading : RunDetailUiState
    data class Error(val message: String) : RunDetailUiState
    data class Success(
        val run: Run,
        val isMapExpanded: Boolean = false   // bottom sheet peek vs hidden
    ) : RunDetailUiState
}

// ── Actions ────────────────────────────────────────────────────────────────

data class RunDetailActions(
    val setMapExpanded: (Boolean) -> Unit,
    val retry: () -> Unit
)

// ── ViewModel ──────────────────────────────────────────────────────────────

class RunDetailViewModel(
    private val repository: RunsRepository,
    savedStateHandle: SavedStateHandle  // Koin + nav inject this automatically
) : ViewModel() {

    // Read runId from SavedStateHandle — set once by the nav system, never during composition
    private val runId: String = checkNotNull(savedStateHandle["runId"])

    private val _isMapExpanded = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = _isMapExpanded
        .flatMapLatest { expanded ->
            repository.getRunById(runId)
                .map { run ->
                    if (run != null)
                        RunDetailUiState.Success(run = run, isMapExpanded = expanded)
                    else
                        RunDetailUiState.Error("Run not found.")
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RunDetailUiState.Loading
        )

    val actions = RunDetailActions(
        setMapExpanded = { expanded -> _isMapExpanded.update { expanded } },
        retry = { /* re-trigger by re-subscribing */ }
    )
}