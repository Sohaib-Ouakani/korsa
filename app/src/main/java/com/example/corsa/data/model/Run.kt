package com.example.corsa.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Run @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("start_time")
    val startTime: Instant,
    @SerialName("end_time")
    val endTime: Instant,
    val path: String,           // GeoJSON LineString from ST_AsGeoJSON
    @SerialName("distance_meters")
    val distanceMeters: Float,
    @SerialName("mean_pace_seconds")
    val meanPaceSeconds: Int,   // e.g. 320 = 5:20/km
    val temperature: Float?,
    @SerialName("elevation_gain")
    val elevationGain: Float?,
    @SerialName("created_at")
    val createdAt: Instant
)