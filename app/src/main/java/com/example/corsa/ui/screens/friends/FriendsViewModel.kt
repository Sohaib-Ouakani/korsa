package com.example.corsa.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.Profile
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface FriendUIState {
    data object Loading : FriendUIState
    data class Error(val message: String) : FriendUIState
    data object Success : FriendUIState
}

data class UserRankEntry(
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val weekKm: Float,
    val level: Int,
)

data class RunFeedEntry(
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val startTime: String,
    val pathUrl: String?,
    val distance: Double
)

data class SearchStatus(
    val friendsName: List<Profile>,
    val notFriends: List<Profile>
)

enum class SortBy { Kilometers, Level }

class FriendsViewModel(
    private val profilesRepository: ProfilesRepository,
    private val runsRepository: RunsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FriendUIState>(FriendUIState.Loading)
    val uiState: StateFlow<FriendUIState> = _uiState

    private val _rankEntries = MutableStateFlow<List<UserRankEntry>>(emptyList())
    val rankEntries: StateFlow<List<UserRankEntry>> = _rankEntries

    private val _feedEntries = MutableStateFlow<List<RunFeedEntry>>(emptyList())
    val feedEntry: StateFlow<List<RunFeedEntry>> = _feedEntries

    // Cached friends profiles to avoid repeated network calls
    private var cachedFriendProfiles: List<com.example.corsa.data.model.Profile> = emptyList()

    // Holds friends names for the search bar
    private val _searchStatus = MutableStateFlow(SearchStatus(emptyList(), emptyList()))
    val searchStatus: SearchStatus get() = _searchStatus.value

    init {
        viewModelScope.launch {
            loadFriendsProfiles()
        }
    }

    // ── Load & cache friends profiles once ──────────────────────────────────

    private suspend fun loadFriendsProfiles() {
        try {
            val friends = profilesRepository.getFriendsProfile()
            val notFriends = profilesRepository.getNotFriendsProfile()
            cachedFriendProfiles = friends
            _searchStatus.value = SearchStatus(
                friendsName = friends,
                notFriends  = notFriends   // filled in AddFriendsScreen scope
            )
            _uiState.value = FriendUIState.Success
        } catch (e: Exception) {
            _uiState.value = FriendUIState.Error(e.message ?: "Unknown error")
        }
    }

    // ── Ranking ─────────────────────────────────────────────────────────────

    fun loadRanking(sortBy: SortBy) {
        viewModelScope.launch {
            try {
                // For each friend, fetch their runs this week via RunsRepository
                val entries = cachedFriendProfiles.map { profile ->
                    UserRankEntry(
                        userId      = profile.id,
                        displayName = profile.username,
                        avatarUrl   = profile.avatarPath,
                        weekKm      = profilesRepository.weeklyKmById(profile.id),
                        level       = profile.level,
                    )
                }

                _rankEntries.value = when (sortBy) {
                    SortBy.Kilometers -> entries.sortedByDescending { it.weekKm }
                    SortBy.Level      -> entries.sortedByDescending { it.level }
                }
            } catch (e: Exception) {
                _uiState.value = FriendUIState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ── Feed ────────────────────────────────────────────────────────────────

    fun loadFeed() {
        viewModelScope.launch {
            try {
                // Build a name+avatar lookup so we don't hit the DB per run
                val profileById = cachedFriendProfiles.associateBy { it.authUserId }

                val allRuns = cachedFriendProfiles.flatMap { profile ->
                    runsRepository.getRunsByUserId(profile.authUserId).map { run ->
                        val owner = profileById[profile.authUserId]
                        RunFeedEntry(
                            userId      = profile.id,
                            displayName = owner?.username ?: profile.username,
                            avatarUrl   = owner?.avatarPath,
                            startTime   = run.startTime.toString(),
                            pathUrl     = run.previewPath,
                            distance    = run.distanceMeters.toDouble() / 1000.0,
                        )
                    }
                }

                _feedEntries.value = allRuns.sortedByDescending { it.startTime }
            } catch (e: Exception) {
                _uiState.value = FriendUIState.Error(e.message ?: "Unknown error")
            }
        }
    }
}