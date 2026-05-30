package com.example.corsa.data.repositories

import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.Run
import com.example.corsa.data.model.RunInsert
import com.example.corsa.ui.screens.home.HomeViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.time.Clock
import kotlin.time.Instant

interface RunsRepository {
    suspend fun getRunById(id: String): Run
    suspend fun getRunsByUserId(userId: String): List<Run>
    suspend fun getMyRuns(): List<Run>
    suspend fun saveRun(
        userId: String,
        startEpochMs: Long,
        endEpochMs: Long,
        points: List<HomeViewModel.TrackingPoint>,
        distanceMeters: Float,
        meanPaceSecPerKm: Int,
    ): Unit
}

class RunsRepositoryImpl(
    private val supabase: SupabaseClient
) : RunsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getRunById(id: String): Run {
        return supabase
            .from("runs")
            .select(Columns.raw("*, ST_AsGeoJSON(path) as path")) {
                filter { eq("id", id) }
            }
            .decodeSingle<Run>()
            .wrapPathAsFeatureCollection()
    }

    override suspend fun getRunsByUserId(userId: String): List<Run> {
        return supabase
            .from("runs_with_geojson")
            .select {
                filter { eq("user_id", userId) }
                order("start_time", Order.DESCENDING)
            }
            .decodeList<Run>()
            .map { it.wrapPathAsFeatureCollection() }
    }

    override suspend fun getMyRuns(): List<Run> {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("User not authenticated")

        val profileId = supabase.postgrest["profiles"]
            .select {
                filter {
                    eq("auth_user_id", userId)
                }
            }
            .decodeSingle<Profile>()
            .id

        return getRunsByUserId(profileId)
    }

    private fun Run.wrapPathAsFeatureCollection(): Run {
        val featureCollection = buildJsonObject {
            put("type", "FeatureCollection")
            putJsonArray("features") {
                addJsonObject {
                    put("type", "Feature")
                    putJsonObject("properties") {}
                    put("geometry", path)   // path is already JsonElement, no parsing needed
                }
            }
        }
        return copy(path = featureCollection)
    }

    private fun Long.toIsoString(): String =
        Instant.fromEpochMilliseconds(this).toString()  // produces "2024-05-30T10:30:00Z"

    override suspend fun saveRun(
        userId: String,
        startEpochMs: Long,
        endEpochMs: Long,
        points: List<HomeViewModel.TrackingPoint>,
        distanceMeters: Float,
        meanPaceSecPerKm: Int,
    ) {
        require(points.size >= 2) { "Need at least 2 points to save a run" }

        val wkt = buildString {
            append("LINESTRING(")
            append(points.joinToString(", ") { "${it.lng} ${it.lat}" })
            append(")")
        }

        val run = RunInsert(
            userId = userId,
            startTime = Instant.fromEpochMilliseconds(startEpochMs),
            endTime = Instant.fromEpochMilliseconds(endEpochMs),
            path = wkt,
            distanceMeters = distanceMeters,
            meanPaceSeconds = meanPaceSecPerKm,
            temperature = null,
            elevationGain = null,
            previewPath = null,
        )

        supabase.from("runs").insert(run)
    }
}