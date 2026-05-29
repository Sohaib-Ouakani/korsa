package com.example.corsa.data.repositories

import com.example.corsa.ui.composables.UserRankEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// ── Interface ──────────────────────────────────────────────────────────────
interface ProfileRepository {
    fun getProfileById(userId: String): Flow<UserRankEntry?>
    fun getAllProfiles(): Flow<List<UserRankEntry>>
}

// ── Fake implementation ────────────────────────────────────────────────────
class FakeProfileRepository : ProfileRepository {

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

    override fun getProfileById(userId: String): Flow<UserRankEntry?> = flow {
        emit(fakeProfiles.firstOrNull { it.userId == userId })
    }

    override fun getAllProfiles(): Flow<List<UserRankEntry>> = flow {
        emit(fakeProfiles)
    }
}