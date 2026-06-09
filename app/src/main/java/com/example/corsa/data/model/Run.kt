package com.example.corsa.data.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class Run @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("start_time")
    val startTime: Instant,
    @SerialName("end_time")
    val endTime: Instant,
    @SerialName("path")
    val path: JsonElement,
    @SerialName("distance_meters")
    val distanceMeters: Float,
    @SerialName("mean_pace_seconds")
    val meanPaceSeconds: Int,
    val temperature: Float?,
    @SerialName("elevation_gain")
    val elevationGain: Float?,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("preview_path")
    val previewPath: String? = null,
    @SerialName("share_token")
    val shareToken: String? = null,
) {
    val startTimeLocal: LocalDateTime
        get() = startTime.toLocalDateTime(TimeZone.currentSystemDefault())
}

@Serializable
data class RunInsert(
     @SerialName("user_id")
     val userId: String,
     @SerialName("start_time")
     val startTime: Instant,
     @SerialName("end_time")
     val endTime: Instant,
     val path: String,
     @SerialName("distance_meters")
     val distanceMeters: Float,
     @SerialName("mean_pace_seconds")
     val meanPaceSeconds: Int,
     val temperature: Float?,
     @SerialName("elevation_gain")
     val elevationGain: Float?,
     @SerialName("preview_path")
     val previewPath: String? = null
)
