package com.example.corsa.utils


import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * 312 -> "5:12 /km"
 */
fun formatPace(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d /km".format(minutes, seconds)
}

/**
 * 7430f -> "7.43 km"  |  430f -> "430 m"
 */
fun formatDistance(meters: Float): String {
    return if (meters >= 1000f) {
        "%.2f km".format(meters / 1000f)
    } else {
        "${meters.toInt()} m"
    }
}

/**
 * Duration between start and end -> "1h 12m" or "43m"
 */
fun formatDuration(startTime: Instant, endTime: Instant): String {
    val duration = endTime - startTime
    val totalMinutes = duration.inWholeMinutes
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}