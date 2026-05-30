package com.example.corsa.data.repositories

import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.ProfileUpdate
import com.example.corsa.data.model.Run
import com.example.corsa.ui.composables.UserEntry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.String
import kotlin.time.Clock

// ── Interface ──────────────────────────────────────────────────────────────
interface ProfilesRepository {
    suspend fun getProfileByUserId(userId: String): Profile
    suspend fun getUserEntryByUserId(userId: String): UserEntry
    suspend fun getAllProfiles(): List<Profile>
    suspend fun getMyProfile(): Profile
    suspend fun updateProfile(update: ProfileUpdate): Profile
    suspend fun getMyUserEntry(): UserEntry
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

    private suspend fun weeklyKmById(userId: String): Float {
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

    override suspend fun getUserEntryByUserId(userId: String): UserEntry {
        val profile = supabase.postgrest["profiles"]
            .select {
                filter {
                    eq("auth_user_id", userId)
                }
            }
            .decodeSingle<Profile>()

        val weeklyKm = weeklyKmById(profile.id)

        return UserEntry(
            profile.username,
            profile.avatarPath,
            weeklyKm,
            profile.level,
            profile.completedChallenges,
            profile.totalKm,
        )
    }

    override suspend fun getAllProfiles(): List<Profile>  {
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

    private fun getMyAuthUserId(): String {
        return supabase.auth.currentUserOrNull()?.id ?: error("User not authenticated")
    }

    override suspend fun getMyUserEntry(): UserEntry {
        val myId = supabase.auth.currentUserOrNull()?.id
            ?: error("User not authenticated")

        return getUserEntryByUserId(myId)
    }
}