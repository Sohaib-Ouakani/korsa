package com.example.corsa.data.repositories

import com.example.corsa.data.model.Follow
import com.example.corsa.data.model.FollowInsert
import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.ProfileUpdate
import com.example.corsa.data.model.Run
import com.example.corsa.ui.composables.UserEntry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.String
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// ── Interface ──────────────────────────────────────────────────────────────
interface ProfilesRepository {
    suspend fun getProfileByUserId(userId: String): Profile
    suspend fun getUserEntryByUserId(userId: String): UserEntry
    suspend fun getAllProfiles(): List<Profile>
    suspend fun getMyProfile(): Profile
    suspend fun updateProfile(update: ProfileUpdate): Profile
    suspend fun getMyUserEntry(): UserEntry
    suspend fun getProfileIFollow(): List<Profile>
    suspend fun weeklyKmByUserId(userId: String): Float
    suspend fun getProfilesIDoNotFollow(): List<Profile>
    suspend fun getIfIFollowAProfileByUserId(userId: String): Boolean
    suspend fun AddFollowToAProfileByUserId(userId: String)
    suspend fun StopFollowToAProfileByUserId(userId: String)
    suspend fun uploadAvatar(imageBytes: ByteArray, mimeType: String): String
    fun avatarUrl(path: String): String
}

// ── Fake implementation ────────────────────────────────────────────────────
class ProfilesRepositoryImpl(
    private val supabase: SupabaseClient
) : ProfilesRepository {

    override suspend fun getProfileByUserId(userId: String): Profile {
        return supabase.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<Profile>()
    }

    override suspend fun weeklyKmByUserId(userId: String): Float {
        val now = Clock.System.now()
        val zone = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(zone).date

        // Get the start of the current week (Monday)
        val startOfWeek = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
        val startInstant = startOfWeek.atStartOfDayIn(zone)

        val runs = supabase
            .from("runs")
            .select {
                filter {
                    eq("user_id", userId)
                    gte("start_time", startInstant.toString())
                }
            }
            .decodeList<Run>()

        return runs.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f
    }

    override suspend fun getProfilesIDoNotFollow(): List<Profile> {
        val currentProfileId = getMyProfile().id

        val follows = supabase.postgrest["follows"]
            .select {
                filter {
                    eq("follower_id", currentProfileId)
                }
            }
            .decodeList<Follow>()

        val excludedIds = (follows.map { it.followingId } + currentProfileId).toSet()

        val allProfiles = supabase.postgrest["profiles"]
            .select()
            .decodeList<Profile>()

        return allProfiles.filter { it.id !in excludedIds }
    }

    override suspend fun getIfIFollowAProfileByUserId(userId: String): Boolean {
        val currentProfileId = getMyProfile().id

        val follows = supabase.postgrest["follows"]
            .select {
                filter {
                    eq("follower_id", currentProfileId)
                    eq("following_id", userId)
                }
            }
            .decodeList<Follow>()

        return follows.isNotEmpty()
    }

    override suspend fun AddFollowToAProfileByUserId(userId: String) {
        val currentProfileId = getMyProfile().id

        supabase.postgrest["follows"]
            .insert(FollowInsert(followerId = currentProfileId, followingId = userId))
    }

    override suspend fun StopFollowToAProfileByUserId(userId: String) {
        val currentProfileId = getMyProfile().id

        supabase.postgrest["follows"]
            .delete {
                filter {
                    eq("follower_id", currentProfileId)
                    eq("following_id", userId)
                }
            }
    }

    override suspend fun getUserEntryByUserId(userId: String): UserEntry {
        val profile = supabase.postgrest["profiles"]
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingle<Profile>()

        val weeklyKm = weeklyKmByUserId(profile.id)

        return UserEntry(
            profile.username,
            profile.avatarPath,
            weeklyKm,
            profile.level,
            profile.completedChallenges,
            profile.totalKm,
        )
    }

    override suspend fun getAllProfiles(): List<Profile> {
        return supabase.postgrest["profiles"]
            .select()
            .decodeList<Profile>()
    }

    override suspend fun getMyProfile(): Profile {
        val authUserId = getMyAuthUserId()

        val results = supabase.postgrest["profiles"]
            .select {
                filter {
                    eq("auth_user_id", authUserId)
                }
            }
            .decodeSingle<Profile>()

        return results
    }

    override suspend fun updateProfile(update: ProfileUpdate): Profile {
        val authUserId = getMyAuthUserId()

        return supabase.postgrest["profiles"]
            .update(update) {
                filter {
                    eq("auth_user_id", authUserId)
                }
                select()
            }
            .decodeSingle<Profile>()
    }

    override suspend fun getMyUserEntry(): UserEntry {
        val profile = getMyProfile()
        val weeklyKm = weeklyKmByUserId(profile.id)
        val avatarUrl = if (profile.avatarPath != null) avatarUrl(profile.avatarPath) else null
        return UserEntry(
            profile.username,
            avatarUrl,
            weeklyKm,
            profile.level,
            profile.completedChallenges,
            profile.totalKm,
        )
    }

    override suspend fun getProfileIFollow(): List<Profile> {
        val currentUserId = getMyProfile().id

        val follows = supabase.postgrest["follows"]
            .select {
                filter {
                    eq("follower_id", currentUserId)
                }
            }
            .decodeList<Follow>()

        val followingIds = follows.map { it.followingId }

        if (followingIds.isEmpty()) return emptyList()

        return supabase.postgrest["profiles"]
            .select {
                filter {
                    isIn("id", followingIds)
                }
            }
            .decodeList<Profile>()

    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun uploadAvatar(imageBytes: ByteArray, mimeType: String): String {
        val userId = getMyAuthUserId()
        val extension = if (mimeType == "image/png") "png" else "jpg"

        // Delete old avatar if exists
        runCatching {
            val old = getMyProfile().avatarPath
            if (old != null) supabase.storage.from("avatars").delete(listOf(old))
        }

        val path = "$userId/avatar-${Uuid.random()}.$extension"

        supabase.storage
            .from("avatars")
            .upload(path, imageBytes) {
                contentType = ContentType.parse(mimeType)
            }

        updateProfile(ProfileUpdate(avatarPath = path))
        return path
    }

    override fun avatarUrl(path: String): String =
        supabase.storage.from("avatars").publicUrl(path)

    private fun getMyAuthUserId(): String {
        return supabase.auth.currentUserOrNull()?.id ?: error("User not authenticated")
    }
}
