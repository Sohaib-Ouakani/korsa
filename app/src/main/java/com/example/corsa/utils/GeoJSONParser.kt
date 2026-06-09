package com.example.corsa.utils

import android.util.Log
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString


fun parseRunGeoJson(geoJson: String): FeatureCollection {
    val trimmed = geoJson.trim()
    return try {
        val fc = FeatureCollection.fromJson(trimmed)
        if (!fc.features().isNullOrEmpty()) {
            fc
        } else {
            throw IllegalStateException("Empty FeatureCollection, trying other types")
        }
    } catch (_: Exception) {
        try {
            val feature = Feature.fromJson(trimmed)
            if (feature.geometry() != null) {
                FeatureCollection.fromFeatures(listOf(feature))
            } else {
                throw IllegalStateException("Feature has no geometry")
            }
        } catch (_: Exception) {
            try {
                val line = LineString.fromJson(trimmed)
                FeatureCollection.fromFeatures(listOf(Feature.fromGeometry(line)))
            } catch (_: Exception) {
                Log.e("RunDetailMap", "Failed to parse GeoJSON: $trimmed")
                FeatureCollection.fromFeatures(emptyList())
            }
        }
    }
}

fun FeatureCollection.latLngs(): List<org.maplibre.android.geometry.LatLng> =
    features()
        ?.flatMap { feature ->
            when (val geom = feature.geometry()) {
                is LineString -> geom.coordinates().map { pt ->
                    org.maplibre.android.geometry.LatLng(pt.latitude(), pt.longitude())
                }
                is org.maplibre.geojson.Point -> listOf(
                    org.maplibre.android.geometry.LatLng(geom.latitude(), geom.longitude())
                )
                else -> emptyList()
            }
        } ?: emptyList()