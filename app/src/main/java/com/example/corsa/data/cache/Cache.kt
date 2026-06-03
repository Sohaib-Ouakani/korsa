package com.example.corsa.data.cache

import com.example.corsa.ui.screens.home.LocationInfo

class LocationCache {
    var locationInfo: LocationInfo? = null
    private var timestamp: Long = 0L
    private val ttlMs = 10 * 60 * 1000L

    val isValid get() = locationInfo != null &&
            System.currentTimeMillis() - timestamp < ttlMs

    fun store(info: LocationInfo) {
        locationInfo = info
        timestamp = System.currentTimeMillis()
    }
}