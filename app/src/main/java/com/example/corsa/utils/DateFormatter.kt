package com.example.corsa.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

fun LocalDateTime.toFeedDateString(): String {
    return try {
        val javaLdt = java.time.LocalDateTime.of(
            year, month.number, day, hour, minute
        )
        val formatter = java.time.format.DateTimeFormatter.ofPattern(
            "dd MMM yyyy · HH:mm", java.util.Locale.getDefault()
        )
        javaLdt.format(formatter)
    } catch (e: Exception) {
        toString()
    }
}