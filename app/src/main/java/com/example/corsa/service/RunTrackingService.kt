package com.example.corsa.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.corsa.R
import com.example.corsa.data.location.LocationProvider
import com.example.corsa.data.location.TrackingPoint
import com.example.corsa.ui.screens.home.run.RunState
import com.example.corsa.ui.screens.home.run.StopWatchState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RunTrackingService : Service() {
    inner class RunBinder : Binder() {
        fun getService(): RunTrackingService = this@RunTrackingService
    }
    private val binder = RunBinder()
    override fun onBind(intent: Intent): IBinder = binder

    private val locationProvider: LocationProvider by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _stopWatchState = MutableStateFlow(StopWatchState())
    val stopWatchState: StateFlow<StopWatchState> = _stopWatchState.asStateFlow()

    private val _runState = MutableStateFlow(RunState())
    val runState: StateFlow<RunState> = _runState.asStateFlow()

    private var stopWatchJob: Job? = null
    private var trackingJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("00:00"))
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    fun start() {
        Log.d("RunSvc", "start() called, isRunning=${_stopWatchState.value.isRunning}")
        if (_stopWatchState.value.isRunning) return
        _stopWatchState.update { it.copy(isRunning = true) }
        if (_runState.value.startEpochMs == 0L) {
            _runState.update { it.copy(startEpochMs = System.currentTimeMillis()) }
        }
        startStopWatchJob()
        startGpsJob()
    }

    fun pause() {
        stopWatchJob?.cancel()
        trackingJob?.cancel()
        _stopWatchState.update { it.copy(isRunning = false) }
    }

    fun stopAndSnapshot(): Pair<StopWatchState, RunState> {
        pause()
        val snapshot = _stopWatchState.value to _runState.value
        reset()
        stopSelf()
        return snapshot
    }

    private fun startStopWatchJob() {
        Log.d("RunSvc", "startStopWatchJob launched")
        stopWatchJob?.cancel()
        stopWatchJob = serviceScope.launch {
            while (isActive) {
                delay(STOPWATCH_TICK_MS)
                _stopWatchState.update { it.copy(elapsedTime = it.elapsedTime + STOPWATCH_TICK_MS) }
                updateNotification(_stopWatchState.value.formattedTime)
            }
        }
    }

    private fun startGpsJob() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            locationProvider.locationFlow(GPS_INTERVAL_MS).collect { location ->
                val newPoint = TrackingPoint(
                    lat = location.latitude,
                    lng = location.longitude,
                    altitude = if (location.hasAltitude()) location.altitude else null
                )
                _runState.update { current ->
                    val updatedPoints = current.points + newPoint
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
                    current.copy(
                        points = updatedPoints,
                        distanceMeters = current.distanceMeters + addedMeters
                    )
                }
            }
        }
    }

    private fun reset() {
        _stopWatchState.value = StopWatchState()
        _runState.value = RunState()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Run Tracking",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(time: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Run in progress")
            .setContentText(time)
            .setSmallIcon(R.drawable.ic_sprint)
            .setOngoing(true)
            .build()

    private fun updateNotification(time: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(time))
    }

    companion object {
        private const val CHANNEL_ID = "run_tracking_channel"
        private const val NOTIFICATION_ID = 1
        private const val GPS_INTERVAL_MS = 3_000L
        private const val STOPWATCH_TICK_MS = 200L
    }
}