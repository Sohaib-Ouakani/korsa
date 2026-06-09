package com.example.corsa.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

enum class WeatherCondition(val icon: ImageVector, val label: String) {
    CLEAR       (Icons.Filled.WbSunny,       "Clear"),
    PARTLY_CLOUDY(Icons.Filled.WbCloudy,     "Partly cloudy"),
    OVERCAST    (Icons.Filled.Cloud,          "Overcast"),
    FOG         (Icons.Filled.Dehaze,     "Fog"),
    RAIN        (Icons.Filled.Grain,          "Rain"),
    SNOW        (Icons.Filled.AcUnit,         "Snow"),
    THUNDERSTORM(Icons.Filled.Thunderstorm,   "Thunderstorm"),
    UNKNOWN     (Icons.Filled.QuestionMark,  "Unknown");

    companion object {
        fun fromWmoCode(code: Int?): WeatherCondition = when (code) {
            0          -> CLEAR
            1, 2       -> PARTLY_CLOUDY
            3          -> OVERCAST
            45, 48     -> FOG
            51, 53, 55,
            61, 63, 65,
            80, 81, 82 -> RAIN
            71, 73, 75,
            77, 85, 86 -> SNOW
            95, 96, 99 -> THUNDERSTORM
            else       -> UNKNOWN
        }
    }
}
