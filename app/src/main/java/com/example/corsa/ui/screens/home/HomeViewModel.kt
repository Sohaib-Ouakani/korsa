package com.example.corsa.ui.screens.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.location.LocationProvider
import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.Run
import com.example.corsa.data.repositories.AuthRepository
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.ui.composables.UserEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Clock

data class HomeState(
    val goalKm: Float,
    val currentKm: Float,
    val progress: Float,
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

data class StopWatchAction(
    val start: () -> Unit,
    val pause: () -> Unit,
    val stop: () -> Unit,
)

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

class HomeViewModel(
    private val runsRepository: RunsRepository,
    private val profilesRepository: ProfilesRepository,
    private val locationProvider: LocationProvider
): ViewModel() {
    val stopWatchActions = StopWatchAction(
        { start() },
        { pause() },
        { reset() }
    )
    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    // Derived from profile reactively
    private val _state = MutableStateFlow<HomeState?>(null)
    val state: StateFlow<HomeState?> = _state
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val loaded = profilesRepository.getMyProfile()
            val weeklyKm = profilesRepository.weeklyKmByUserId(loaded.id)
            val goalKm = loaded.level * 10f

            _profile.value = loaded
            _state.value = HomeState(
                goalKm = goalKm,
                currentKm = weeklyKm,
                progress = weeklyKm / goalKm,
                locationName = "Galeata"
            )
        }
    }
    // ── Run tracking state ───────────────────────────────────────────────────────

    data class TrackingPoint(val lat: Double, val lng: Double)

    data class RunState(
        val points: List<TrackingPoint> = emptyList(),
        val distanceMeters: Float = 0f,
        val currentPaceSecPerKm: Int = 0,   // seconds per km, 0 = not yet computable
        val startEpochMs: Long = 0L,
    ) {
        val distanceKm: Float get() = distanceMeters / 1000f

        // e.g. "5:30 /km" — shown on the StopWatch screen
        val formattedPace: String get() {
            if (currentPaceSecPerKm <= 0) return "--:-- /km"
            val m = currentPaceSecPerKm / 60
            val s = currentPaceSecPerKm % 60
            return String.format(Locale.US, "%d:%02d /km", m, s)
        }
    }

    private val _runState = MutableStateFlow(RunState())
    val runState: StateFlow<RunState> = _runState

    private var trackingJob: Job? = null

    private val _timerState = MutableStateFlow(StopWatchStatus())
    val timerState: StateFlow<StopWatchStatus> = _timerState

    private var timerJob: Job? = null

    private fun start() {
        if (_timerState.value.isRunning) return
        _timerState.update { it.copy(isRunning = true) }

        // Record start time only on the very first start (not on resume)
        if (_runState.value.startEpochMs == 0L) {
            _runState.update { it.copy(startEpochMs = System.currentTimeMillis()) }
        }
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

        // GPS accumulation — starts fresh, or resumes from where we paused
        trackingJob = viewModelScope.launch {
            locationProvider.locationFlow(intervalMs = 3000L)
                .collect { location ->
                    val newPoint = TrackingPoint(location.latitude, location.longitude)
                    _runState.update { current ->
                        val updatedPoints = current.points + newPoint

                        // Distance: add the leg from the previous point to this one
                        val addedMeters = if (updatedPoints.size >= 2) {
                            val prev = updatedPoints[updatedPoints.size - 2]
                            val results = FloatArray(1)
                            android.location.Location.distanceBetween(
                                prev.lat, prev.lng,
                                newPoint.lat, newPoint.lng,
                                results
                            )
                            results[0]
                        } else 0f

                        val totalDistance = current.distanceMeters + addedMeters

                        // Pace: based on total elapsed time and total distance so far
                        val elapsedSeconds = _timerState.value.elapsedTime / 1000f
                        val pace = if (totalDistance > 0)
                            (elapsedSeconds / (totalDistance / 1000f)).toInt()
                        else 0

                        current.copy(
                            points        = updatedPoints,
                            distanceMeters = totalDistance,
                            currentPaceSecPerKm = pace
                        )
                    }
                }
        }
    }

    private fun pause() {
        timerJob?.cancel()
        trackingJob?.cancel()       // stop collecting GPS — points are preserved
        _timerState.update { it.copy(isRunning = false) }
    }


    private fun reset() {
        pause()
        if (_timerState.value.elapsedTime > 30) {
            finishRun()
        }
        _timerState.update { current ->
            current.copy(
                elapsedTime = 0L
            )
        }
        _runState.value = RunState()    // wipe accumulated points
    }

    private fun finishRun() {
        val userId = _profile.value?.id ?: return
        val run    = _runState.value
        val endMs  = Clock.System.now().toEpochMilliseconds()

        if (run.points.size < 2) {
            _saveState.value = SaveState.Error("Run too short to save")
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            runCatching<Unit> {
                runsRepository.saveRun(
                    userId           = userId,
                    startEpochMs     = run.startEpochMs,
                    endEpochMs       = endMs,
                    points           = run.points,
                    distanceMeters   = run.distanceMeters,
                    meanPaceSecPerKm = run.currentPaceSecPerKm,
                )
            }.onSuccess {
                reset()
                loadProfile()
                _saveState.value = SaveState.Success
            }.onFailure { e ->
                _saveState.value = SaveState.Error(e.message ?: "Unknown error")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        trackingJob?.cancel()
    }
}