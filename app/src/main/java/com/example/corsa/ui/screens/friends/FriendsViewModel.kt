package com.example.corsa.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.sortedByDescending


data class UserRankEntry(
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val weekKm: Double,
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
    val friendsName: List<String>,
    val notFriends: List<String>
)

enum class SortBy { Kilometers, Level }

class FriendsViewModel : ViewModel() {
    val searchStatus = SearchStatus(
        listOf("Rossi", "Io", "Gardo", "Pelats", "Aguzzi", "Cloe"),
        listOf("Pianini", "Moro", "Barbuto", "Baivdi", "Corteccia")
    )
    private val _rankEntries = MutableStateFlow<List<UserRankEntry>>(emptyList())
    private val _feedEntries = MutableStateFlow<List<RunFeedEntry>>(emptyList())
    val rankEntries: StateFlow<List<UserRankEntry>> = _rankEntries
    val feedEntry: StateFlow<List<RunFeedEntry>> = _feedEntries
    fun loadRanking(sortBy: SortBy) {
        viewModelScope.launch {
            val raw = listOf(
            UserRankEntry("1", "J. Donahue", null, 64.2, 5),
            UserRankEntry("2", "A. Smith",   null, 58.9, 4),
            UserRankEntry("3", "M. Tanaka",  null, 45.1, 3),
        )   // your DB/network call
            _rankEntries.value = when (sortBy) {
                SortBy.Kilometers -> raw.sortedByDescending { it.weekKm }
                SortBy.Level      -> raw.sortedByDescending { it.level }
            }
        }
    }

    fun loadFeed() {
        viewModelScope.launch {
            val raw = listOf(
                RunFeedEntry("1","J. Donahue", null, "2026-05-26T10:30:00+02:00", null, 5.4 )
            )
//                  supabase
//                .from("runs")
//                .select() {
//                    // se vuoi filtrare solo le run degli amici:
//                    // filter { eq("user_id", currentUserId) }
//                }
//                .decodeList<RunFeedEntry>()

            _feedEntries.value = raw.sortedByDescending { it.startTime }
        }
    }
}
