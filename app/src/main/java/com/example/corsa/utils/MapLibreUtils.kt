package com.example.corsa.utils

import org.maplibre.geojson.LineString

/**
 * Converts a GeoJSON string (as returned by PostGIS ST_AsGeoJSON)
 * directly into a MapLibre LineString. No manual parsing needed.
 */
fun geoJsonToLineString(geoJson: String): LineString =
    LineString.fromJson(geoJson)