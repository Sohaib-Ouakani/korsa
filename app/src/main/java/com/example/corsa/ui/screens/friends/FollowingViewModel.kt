package com.example.corsa.ui.screens.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.Profile
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val runId: String,
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

class FollowingViewModel(
    private val profilesRepository: ProfilesRepository,
    private val runsRepository: RunsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FriendUIState>(FriendUIState.Loading)
    val uiState: StateFlow<FriendUIState> = _uiState

    private val _rankEntries = MutableStateFlow<List<UserRankEntry>>(emptyList())
    val rankEntries: StateFlow<List<UserRankEntry>> = _rankEntries

    private val _feedEntries = MutableStateFlow<List<RunFeedEntry>>(emptyList())
    val feedEntry: StateFlow<List<RunFeedEntry>> = _feedEntries

    private val _isRankLoading = MutableStateFlow(true)
    val isRankLoading: StateFlow<Boolean> = _isRankLoading.asStateFlow()

    // Cached friends profiles to avoid repeated network calls
    private var cachedFriendProfiles: List<Profile> = emptyList()

    // Holds friends names for the search bar
    private val _searchStatus = MutableStateFlow(SearchStatus(emptyList(), emptyList()))
    val searchStatus: StateFlow<SearchStatus> = _searchStatus.asStateFlow()

    init {
        viewModelScope.launch {
            loadFriendsProfiles()
        }
    }

    // ── Load & cache friends profiles once ──────────────────────────────────

    private suspend fun loadFriendsProfiles() {
        try {
            val friends = profilesRepository.getProfileIFollow()
            val notFriends = profilesRepository.getProfilesIDoNotFollow()
            cachedFriendProfiles = friends
            _searchStatus.value = SearchStatus(
                friendsName = friends,
                notFriends  = notFriends   // filled in AddFriendsScreen scope
            )
            _uiState.value = FriendUIState.Success
            loadRanking(SortBy.Kilometers)
            loadFeed()
        } catch (e: Exception) {
            _uiState.value = FriendUIState.Error(e.message ?: "Unknown error")
        }
    }

    // ── Ranking ─────────────────────────────────────────────────────────────

    fun refreshFriends() {
        viewModelScope.launch {
            loadFriendsProfiles()
        }
    }
    suspend fun loadRanking(sortBy: SortBy) {
        _isRankLoading.value = true
        try {
            val entries = coroutineScope {
                cachedFriendProfiles.map { profile ->
                    async {
                        UserRankEntry(
                            userId      = profile.id,
                            displayName = profile.username,
                            avatarUrl   = profile.avatarPath,
                            weekKm      = profilesRepository.weeklyKmByUserId(profile.id),
                            level       = profile.level,
                        )
                    }
                }.awaitAll()
            }
            _rankEntries.value = when (sortBy) {
                SortBy.Kilometers -> entries.sortedByDescending { it.weekKm }
                SortBy.Level      -> entries.sortedByDescending { it.level }
            }
        } catch (e: Exception) {
            _uiState.value = FriendUIState.Error(e.message ?: "Unknown error")
        } finally {
            _isRankLoading.value = false
        }
    }

    // ── Feed ────────────────────────────────────────────────────────────────

    fun loadFeed() {
        viewModelScope.launch {
            try {
                val allRuns = cachedFriendProfiles.flatMap { profile ->
                    runsRepository.getRunsByUserId(profile.id)
                        .map { run ->
                            RunFeedEntry(
                                runId       = run.id,
                                userId      = profile.id,
                                displayName = profile.username,
                                avatarUrl   = profile.avatarPath,
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