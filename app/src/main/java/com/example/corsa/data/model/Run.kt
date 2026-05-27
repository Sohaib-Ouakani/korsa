package com.example.corsa.data.model

import java.time.ZonedDateTime

data class Run(
    val id: String,
    val userId: String,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val path: String,           // GeoJSON LineString from ST_AsGeoJSON
    val distanceMeters: Float,
    val meanPaceSeconds: Int,   // e.g. 320 = 5:20/km
    val temperature: Float?,
    val elevationGain: Float?
)