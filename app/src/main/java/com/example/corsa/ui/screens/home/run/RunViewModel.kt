package com.example.corsa.ui.screens.home.run

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.location.LocationProvider
import com.example.corsa.data.location.TrackingPoint
import com.example.corsa.data.model.Profile
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.collections.plus
import kotlin.time.Clock

data class RunAction(
    val start: () -> Unit,
    val pause: () -> Unit,
    val stop: () -> Unit,
)

data class StopWatchState(
    val elapsedTime: Long = 0L,
    val isRunning: Boolean = false
) {
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

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
    data class Validation(val message: String) : SaveState()
}

data class RunState(
    val points: List<TrackingPoint> = emptyList(),
    val distanceMeters: Float = 0f,
    val currentPaceSecPerKm: Int = 0,
    val startEpochMs: Long = 0L,
) {
    val distanceKm: Float get() = distanceMeters / 1000f
    val formattedPace: String get() {
        if (currentPaceSecPerKm <= 0) return "--:-- /km"
        val m = currentPaceSecPerKm / 60
        val s = currentPaceSecPerKm % 60
        return String.format(Locale.US, "%d:%02d /km", m, s)
    }
}

data class RunUiState(
    val stopWatch: StopWatchState = StopWatchState(),
    val run: RunState = RunState(),
    val saveState: SaveState = SaveState.Idle,
)

class RunViewModel(
    private val runsRepository: RunsRepository,
    private val profilesRepository: ProfilesRepository,
    private val locationProvider: LocationProvider
): ViewModel() {
    val runActions = RunAction(
        { start() },
        { pause() },
        { stop() }
    )
    private val MIN_RUN_DURATION_MS = 10_000L
    private val _profile = MutableStateFlow<Profile?>(null)
    private val profile: StateFlow<Profile?> = _profile
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    private val _runState = MutableStateFlow(RunState())
    private val runState: StateFlow<RunState> = _runState
    private var trackingJob: Job? = null
    private val _stopWatchState = MutableStateFlow(StopWatchState())
    private val stopWatchState: StateFlow<StopWatchState> = _stopWatchState
    private var stopWatchJob: Job? = null
    val uiState: StateFlow<RunUiState> = combine(
        _stopWatchState, _runState, _saveState
    ) { sw, run, save ->
        RunUiState(sw, run, save)
    }.stateIn(viewModelScope, SharingStarted.Lazily, RunUiState())

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _profile.value = profilesRepository.getMyProfile()
        }
    }

    private fun start() {
        if (_stopWatchState.value.isRunning) return
        _stopWatchState.update { it.copy(isRunning = true) }

        if (_runState.value.startEpochMs == 0L) {
            _runState.update { it.copy(startEpochMs = System.currentTimeMillis()) }
        }
        startStopWatchJob()
        startGPSJob()
    }

    private fun startGPSJob() {
        trackingJob = viewModelScope.launch {
            locationProvider.locationFlow(intervalMs = 3000L)
                .collect { location ->
                    val newPoint = TrackingPoint(
                        location.latitude,
                        location.longitude,
                        if (location.hasAltitude()) location.altitude else null
                    )
                    _runState.update { current ->
                        val updatedPoints = current.points + newPoint

                        // Distance: add the previous point to this one
                        val addedMeters = if (updatedPoints.size >= 2) {
                            val prev = updatedPoints[updatedPoints.size - 2]
                            val results = FloatArray(1)
                            Location.distanceBetween(
                                prev.lat, prev.lng,
                                newPoint.lat, newPoint.lng,
                                results
                            )
                            results[0]
                        } else 0f

                        val totalDistance = current.distanceMeters + addedMeters

                        val elapsedSeconds = _stopWatchState.value.elapsedTime / 1000f
                        val pace = if (totalDistance > 0)
                            (elapsedSeconds / (totalDistance / 1000f)).toInt()
                        else 0

                        current.copy(
                            points = updatedPoints,
                            distanceMeters = totalDistance,
                            currentPaceSecPerKm = pace
                        )
                    }
                }
        }
    }

    private fun startStopWatchJob() {
        stopWatchJob = viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                delay(200L)
                _stopWatchState.update { current ->
                    current.copy(
                        elapsedTime = current.elapsedTime + 200L
                    )
                }
            }
        }
    }

    private fun pause() {
        stopWatchJob?.cancel()
        trackingJob?.cancel()
        _stopWatchState.update { it.copy(isRunning = false) }
    }


    private fun stop() {
        pause()
        if (stopWatchState.value.elapsedTime > MIN_RUN_DURATION_MS) {
            finishRun()
        } else {
            _saveState.value = SaveState.Validation("Run too brief to save")
            reset()
        }
    }

    private fun finishRun() {
        val userId = profile.value?.id ?: return
        val run    = runState.value
        val endMs  = Clock.System.now().toEpochMilliseconds()
        reset()

        if (run.points.size < 2) {
            _saveState.value = SaveState.Validation("Run too short to save")
            return
        }

        viewModelScope.launch {
            saveNewRun(userId, run, endMs)
        }
    }

    private suspend fun saveNewRun(
        userId: String,
        run: RunState,
        endMs: Long
    ) {
        _saveState.value = SaveState.Saving
        runCatching {
            runsRepository.saveRun(
                userId = userId,
                startEpochMs = run.startEpochMs,
                endEpochMs = endMs,
                points = run.points,
                distanceMeters = run.distanceMeters,
                meanPaceSecPerKm = run.currentPaceSecPerKm,
            )
        }.onSuccess {
            _saveState.value = SaveState.Success
        }.onFailure { e ->
            _saveState.value = SaveState.Error(e.message ?: "Unknown error")
        }
    }

    private fun reset() {
        _stopWatchState.update { it.copy(elapsedTime = 0L) }
        _runState.value = RunState()
    }

    override fun onCleared() {
        super.onCleared()
        stopWatchJob?.cancel()
        trackingJob?.cancel()
    }
}