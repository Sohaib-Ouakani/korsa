package com.example.corsa.ui.screens.rundetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.Run
import com.example.corsa.data.repositories.RunsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
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

    val state = combine(
        flowOf(runId),
        _isMapExpanded
    ) { id, expanded ->
        var result: RunDetailUiState = RunDetailUiState.Loading
        repository.getRunById(id).collect { run ->
            result = if (run != null)
                RunDetailUiState.Success(run = run, isMapExpanded = expanded)
            else
                RunDetailUiState.Error("Run not found.")
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RunDetailUiState.Loading
    )

    val actions = RunDetailActions(
        setMapExpanded = { expanded -> _isMapExpanded.update { expanded } },
        retry = { /* re-trigger by re-subscribing */ }
    )
}