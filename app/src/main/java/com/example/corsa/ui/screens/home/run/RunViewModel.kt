package com.example.corsa.ui.screens.home.run

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.location.TrackingPoint
import com.example.corsa.data.model.Profile
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.service.RunTrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Clock

data class RunActions(
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
    data object Idle : SaveState()
    data object Saving : SaveState()
    data object Success : SaveState()
    data class Error(val message: String) : SaveState()
    data class ValidationError(val message: String) : SaveState()
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
    private val appContext: Context,
): ViewModel() {
    val runActions = RunActions(
        ::start,
        ::pause,
        ::stop
    )
    companion object {
        private const val MIN_RUN_DURATION_MS = 10_000L
    }

    private var service: RunTrackingService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d("RunVM", "onServiceConnected")
            service = (binder as RunTrackingService.RunBinder).getService()
            observeServiceState()
        }
        override fun onServiceDisconnected(name: ComponentName) {
            Log.d("RunVM", "onServiceDisconnected")
            service = null
        }
    }

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    private val _runState = MutableStateFlow(RunState())
    private val _stopWatchState = MutableStateFlow(StopWatchState())
    private val _profile = MutableStateFlow<Profile?>(null)


    val uiState: StateFlow<RunUiState> = combine(
        _stopWatchState, _runState, _saveState
    ) { sw, run, save ->
        val pace = calculatePace(sw.elapsedTime, run.distanceMeters)
        RunUiState(sw, run.copy(currentPaceSecPerKm = pace), save)
    }.stateIn(viewModelScope, SharingStarted.Lazily, RunUiState())

    init {
        loadProfile()
        startAndBindService()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                _profile.value = profilesRepository.getMyProfile()
            }
            catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    private fun startAndBindService() {
        val intent = Intent(appContext, RunTrackingService::class.java)
        val startResult = appContext.startService(intent)
        Log.d("RunVM", "startService result: $startResult")
        val bindResult = appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Log.d("RunVM", "bindService result: $bindResult")
    }
    private fun observeServiceState() {
        Log.d("RunVM", "observeServiceState called, service=$service")
        val svc = service ?: return
        viewModelScope.launch { svc.stopWatchState.collect { _stopWatchState.value = it } }
        viewModelScope.launch { svc.runState.collect { _runState.value = it } }
    }

    private fun start() = service?.start()
    private fun pause() = service?.pause()

    private fun stop() {
        val svc = service ?: return
        val (sw, run) = svc.stopAndSnapshot()

        if (sw.elapsedTime <= MIN_RUN_DURATION_MS) {
            _saveState.value = SaveState.ValidationError("Run too brief to save")
            return
        }
        finishRun(sw, run)
    }

    private fun finishRun(sw: StopWatchState, run: RunState) {
        val userId = _profile.value?.id ?: run {
            _saveState.value = SaveState.Error("Profile not loaded, run not saved")
            return
        }
        if (run.points.size < 2) {
            _saveState.value = SaveState.ValidationError("Run too short to save")
            return
        }
        val pace = calculatePace(sw.elapsedTime, run.distanceMeters)
        val endMs = Clock.System.now().toEpochMilliseconds()
        viewModelScope.launch { saveNewRun(userId, run.copy(currentPaceSecPerKm = pace), endMs) }
    }

    private suspend fun saveNewRun(
        userId: String,
        run: RunState,
        endMs: Long
    ) {
        _saveState.value = SaveState.Saving
        try {
            runsRepository.saveRun(
                userId           = userId,
                startEpochMs     = run.startEpochMs,
                endEpochMs       = endMs,
                points           = run.points,
                distanceMeters   = run.distanceMeters,
                meanPaceSecPerKm = run.currentPaceSecPerKm,
            )
            _saveState.value = SaveState.Success
        } catch (e: Exception) {
            _saveState.value = SaveState.Error(e.message ?: "Unknown error")
        }
    }

    override fun onCleared() {
        super.onCleared()
        appContext.unbindService(connection)
        // intentionally NOT calling stopService here —
        // if the user backgrounds the app mid-run the service keeps going
    }

    private fun calculatePace(elapsedMs: Long, distanceMeters: Float): Int =
        if (distanceMeters > 0) (elapsedMs / 1000f / (distanceMeters / 1000f)).toInt() else 0
}