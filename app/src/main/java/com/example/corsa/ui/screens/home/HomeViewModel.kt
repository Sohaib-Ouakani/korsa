package com.example.corsa.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class HomeState(
    val goalKm: Double,
    val currentKm: Double,
    val progress: Double,
    val locationName: String
)

data class StopWatchStatus(
    val elapsedTime: Long = 0L,
    val isRunning: Boolean = false
) {
    // Derive a ready‑to‑display string (HH:MM:SS or MM:SS)
    val formattedTime: String
        get() {
            val totalSeconds = elapsedTime / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }
}

class HomeViewModel: ViewModel() {
    companion object {
        val state = HomeState(
            50.0,
            25.0,
            0.5,
            "Galeata"
        )

        val stopWatchState = StopWatchStatus()
    }

    private val _timerState = MutableStateFlow(StopWatchStatus())
    val timerState: StateFlow<StopWatchStatus> = _timerState

    private var timerJob: Job? = null

    fun start() {
        if (_timerState.value.isRunning) return
        _timerState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                delay(200L)                    // tick every second
                _timerState.update { current ->
                    current.copy(
                        elapsedTime = current.elapsedTime + 200L
                    )
                }
            }
        }
    }

    fun pause() {
        timerJob?.cancel()
        _timerState.update { it.copy(isRunning = false) }
    }

    fun reset() {
        pause()
        _timerState.update { current ->
            current.copy(
                elapsedTime = 0L
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}