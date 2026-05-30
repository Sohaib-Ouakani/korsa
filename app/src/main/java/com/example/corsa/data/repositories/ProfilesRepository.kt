package com.example.corsa.data.repositories

import com.example.corsa.ui.composables.UserRankEntry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

// ── Interface ──────────────────────────────────────────────────────────────
interface ProfilesRepository {
    suspend fun getProfileByUserId(userId: String): UserRankEntry
    suspend fun getAllProfiles(): List<UserRankEntry>
}

// ── Fake implementation ────────────────────────────────────────────────────
class ProfilesRepositoryImpl(
    private val supabase: SupabaseClient
) : ProfilesRepository {

    private val fakeProfiles = listOf(
        UserRankEntry(
            userId             = "1",
            displayName        = "J. Donahue",
            avatarUrl          = null,
            weekKm             = 69.0,
            level              = 7,
            completedChallenge = 8,
            totKn              = 48.3
        ),
        UserRankEntry(
            userId             = "user-abc",
            displayName        = "M. Rossi",
            avatarUrl          = null,
            weekKm             = 22.4,
            level              = 4,
            completedChallenge = 3,
            totKn              = 312.5
        )
    )

    override suspend fun getProfileByUserId(userId: String): UserRankEntry {
        return supabase.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<UserRankEntry>()

    }

    override suspend fun getAllProfiles(): List<UserRankEntry>  {
        return supabase.postgrest["profiles"]
                .select()
                .decodeList<UserRankEntry>()
    }
}