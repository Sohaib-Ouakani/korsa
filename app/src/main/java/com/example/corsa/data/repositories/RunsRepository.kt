package com.example.corsa.data.repositories

import com.example.corsa.data.location.TrackingPoint
import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.Run
import com.example.corsa.data.model.RunInsert
import com.example.corsa.ui.screens.rundetail.CommentEntry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.time.Instant
import kotlin.time.Clock

interface RunsRepository {
    suspend fun getRunById(id: String): Run
    suspend fun getRunByShareToken(token: String): Run
    suspend fun getRunsByUserId(userId: String): List<Run>
    suspend fun getMyRuns(): List<Run>
    suspend fun saveRun(
        userId: String,
        startEpochMs: Long,
        endEpochMs: Long,
        points: List<TrackingPoint>,
        distanceMeters: Float,
        meanPaceSecPerKm: Int,
    )
    fun observeRunUpdates(userId: String): Flow<List<Run>>
    suspend fun getCommentsById(id: String): List<CommentEntry>
    suspend fun getLikeCountById(id: String): Int
    suspend fun addLikeToARun(runId: String)
    suspend fun isLikedByMeById(runId: String): Boolean
    suspend fun addCommentToARun(runId: String, content: String)
    suspend fun removeLikeFromARun(runId: String)
}

class RunsRepositoryImpl(
    private val supabase: SupabaseClient,
    private val httpClient: HttpClient
) : RunsRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val activeChannels = mutableMapOf<String, RealtimeChannel>()

    override suspend fun getRunById(id: String): Run {
        return supabase
            .from("runs_with_geojson")  // ← was "runs"
            .select {                    // ← drop Columns.raw(...)
                filter { eq("id", id) }
            }
            .decodeSingle<Run>()
            .wrapPathAsFeatureCollection()
    }

    override suspend fun getRunByShareToken(token: String): Run {
        return supabase
            .from("runs_with_geojson")
            .select {
                filter { eq("share_token", token) }
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

    override suspend fun saveRun(
        userId: String,
        startEpochMs: Long,
        endEpochMs: Long,
        points: List<TrackingPoint>,
        distanceMeters: Float,
        meanPaceSecPerKm: Int,
    ) {
        require(points.size >= 2) { "Need at least 2 points to save a run" }

        val wkt = buildString {
            append("LINESTRING(")
            append(points.joinToString(", ") { "${it.lng} ${it.lat}" })
            append(")")
        }

        val elevationGain = calculateElevationGain(points)

        val startTime = Instant.fromEpochMilliseconds(startEpochMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val temperature = fetchTemperatureForRun(
            client = httpClient,
            lat = points.first().lat,
            lon = points.first().lng,
            startTime = startTime
        )

        val run = RunInsert(
            userId = userId,
            startTime = Instant.fromEpochMilliseconds(startEpochMs),
            endTime = Instant.fromEpochMilliseconds(endEpochMs),
            path = wkt,
            distanceMeters = distanceMeters,
            meanPaceSeconds = meanPaceSecPerKm,
            temperature = temperature,
            elevationGain = elevationGain,
            previewPath = null,
        )

        supabase.from("runs").insert(run)
    }

    private fun calculateElevationGain(points: List<TrackingPoint>): Float? {
        val altitudes = points.mapNotNull { it.altitude }
        if (altitudes.size < 2) return null

        var gain = 0.0
        for (i in 1 until altitudes.size) {

            val diff = altitudes[i] - altitudes[i - 1]
            if (diff > 0) gain += diff
        }
        return gain.toFloat()
    }

    @Serializable
    private data class OpenMeteoResponse(val hourly: HourlyData)

    @Serializable
    private data class HourlyData(
        val time: List<String>,
        val temperature_2m: List<Double?>
    )
    private suspend fun fetchTemperatureForRun(
        client: HttpClient,
        lat: Double,
        lon: Double,
        startTime: LocalDateTime
    ): Float? {
        val date = startTime.date.toString()
        val hour = startTime.hour
        val isToday = startTime.date == Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date

        val baseUrl = if (isToday)
            "https://api.open-meteo.com/v1/forecast"
        else
            "https://archive-api.open-meteo.com/v1/archive"

        return try {
            val response = client.get(baseUrl) {
                parameter("latitude", lat)
                parameter("longitude", lon)
                parameter("hourly", "temperature_2m")
                parameter("start_date", date)
                parameter("end_date", date)
                parameter("timezone", "auto")
            }
            val body = json.decodeFromString<OpenMeteoResponse>(response.bodyAsText())
            val targetTime = "${date}T${hour.toString().padStart(2, '0')}:00"
            val index = body.hourly.time.indexOf(targetTime)
            if (index >= 0) body.hourly.temperature_2m[index]?.toFloat() else null
        } catch (e: Exception) {
            null
        }
    }

    override fun observeRunUpdates(userId: String): Flow<List<Run>> = callbackFlow {
        trySend(getRunsByUserId(userId))

        // Unsubscribe existing channel for this user if any
        activeChannels[userId]?.let {
            it.unsubscribe()
            activeChannels.remove(userId)
        }

        val channel = supabase.realtime.channel("runs:$userId")
        activeChannels[userId] = channel

        channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "runs"
            filter("user_id", FilterOperator.EQ, userId)
        }.onEach {
            trySend(getRunsByUserId(userId))
        }.launchIn(this)

        channel.subscribe()

        awaitClose {
            launch {
                channel.unsubscribe()
                activeChannels.remove(userId)
            }
        }
    }

    override suspend fun getCommentsById(id: String): List<CommentEntry> {
        return supabase
            .from("comments")
            .select(Columns.raw("*, profiles!comments_author_id_fkey(id, username, avatar_path)")) {
                filter { eq("run_id", id) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<CommentWithProfile>()
            .map { it.toCommentEntry() }
    }

    override suspend fun getLikeCountById(id: String): Int {
        return supabase
            .from("likes")
            .select {
                filter { eq("run_id", id) }
            }
            .decodeList<Map<String, String>>()
            .size
    }

    override suspend fun addLikeToARun(runId: String) {
        val authUserId = supabase.auth.currentUserOrNull()?.id
            ?: error("User not authenticated")

        val profileId = supabase.postgrest["profiles"]
            .select {
                filter { eq("auth_user_id", authUserId) }
            }
            .decodeSingle<Profile>()
            .id

        supabase.from("likes").insert(
            mapOf(
                "run_id" to runId,
                "profile_id" to profileId,
            )
        )
    }

    override suspend fun isLikedByMeById(runId: String): Boolean {
        val authUserId = supabase.auth.currentUserOrNull()?.id
            ?: error("User not authenticated")

        val profileId = supabase.postgrest["profiles"]
            .select {
                filter { eq("auth_user_id", authUserId) }
            }
            .decodeSingle<Profile>()
            .id

        return supabase
            .from("likes")
            .select {
                filter {
                    eq("run_id", runId)
                    eq("profile_id", profileId)
                }
            }
            .decodeList<Map<String, String>>()
            .isNotEmpty()
    }

    override suspend fun addCommentToARun(runId: String, content: String) {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("User not authenticated")

        val profileId = supabase.postgrest["profiles"]
            .select {
                filter { eq("auth_user_id", userId) }
            }
            .decodeSingle<Profile>()
            .id

        supabase.from("comments").insert(
            mapOf(
                "run_id" to runId,
                "author_id" to profileId,
                "content" to content,
            )
        )
    }

    override suspend fun removeLikeFromARun(runId: String) {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: error("User not authenticated")

        val profileId = supabase.postgrest["profiles"]
            .select {
                filter { eq("auth_user_id", userId) }
            }
            .decodeSingle<Profile>()
            .id

        supabase.from("likes").delete {
            filter {
                eq("run_id", runId)
                eq("profile_id", profileId)
            }
        }
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

}

@Serializable
private data class CommentWithProfile(
    val id: String,
    @SerialName("run_id") val runId: String,
    val content: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    val profiles: AuthorProfile,
) {
    fun toCommentEntry() = CommentEntry(
        commentId = id,
        runId = runId,
        commentContent = content,
        commentCreatedAt = createdAt,
        commentUpdatedAt = updatedAt,
        authorId = profiles.id,
        authorUsername = profiles.username,
        authorAvatarPath = profiles.avatarPath,
    )
}

@Serializable
private data class AuthorProfile(
    val id: String,
    val username: String,
    @SerialName("avatar_path") val avatarPath: String? = null,
)