package com.example.corsa.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    @SerialName("path_geojson")
    val path: String,
    @SerialName("distance_meters")
    val distanceMeters: Float,
    @SerialName("mean_pace_seconds")
    val meanPaceSeconds: Int,   // e.g. 320 = 5:20/km
    val temperature: Float?,
    @SerialName("elevation_gain")
    val elevationGain: Float?,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("preview_path")
    val previewPath: String? = null
)

@Serializable
data class RunInsert(
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
     val createdAt: Instant,
     @SerialName("preview_path")
     val previewPath: String? = null
)
