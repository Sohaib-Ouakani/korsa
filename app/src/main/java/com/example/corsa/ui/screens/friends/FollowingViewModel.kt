package com.example.corsa.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.Profile
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.utils.AppError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

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
    val startTime: LocalDateTime,
    val pathUrl: String?,
    val distance: Double
)

data class FollowState (
    val isLoading: Boolean,
    val rankEntry: List<UserRankEntry> = listOf(),
    val feedEntry: List<RunFeedEntry> = listOf(),
    val myUserId: String = "",
    val error: AppError = AppError.Absent
)

data class SearchState(
    val isLoading: Boolean,
    val friendsName: List<Profile> = emptyList(),
    val notFriends: List<Profile> = emptyList(),
    val error: AppError = AppError.Absent,
    val myProfileUrl: String? = null
)

data class FollowAction(
    val refreshFriends: () -> Unit,
    val loadRanking:  (SortBy) -> Unit,
    val buildAvatarUrl: (String) -> String,
    val loadFeed: () -> Unit,
    val getAvatarUrl: (String) -> String?
)

enum class SortBy { Kilometers, Level }

class FollowingViewModel(
    private val profilesRepository: ProfilesRepository,
    private val runsRepository: RunsRepository,
) : ViewModel() {

    val followAction = FollowAction(
        refreshFriends = ::refreshFriends,
        loadRanking = ::loadRanking,
        buildAvatarUrl = ::buildAvatarUrl,
        loadFeed = ::loadFeed,
        getAvatarUrl = ::getAvatarUrl
    )

    private var myProfile: Profile? = null
    private val _followState = MutableStateFlow(FollowState(isLoading = true))
    val followState = _followState.asStateFlow()
    private val _searchState = MutableStateFlow(SearchState(isLoading = true))
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    init {
        loadFriendsProfiles()
        getMyProfileUrl()
    }

    private  fun loadFriendsProfiles() {
        viewModelScope.launch {
            try {
                val friends = profilesRepository.getProfileIFollow()
                val notFriends = profilesRepository.getProfilesIDoNotFollow()
                _searchState.updateSearchState(
                    friendsName = friends,
                    notFriends  = notFriends,
                    error = AppError.Absent,
                )
                loadRanking(SortBy.Kilometers)
                loadFeed()
            } catch (e: Exception) {
                _followState.updateFollowState(error = AppError.Present(e.message ?: "Error while loading"))
                _searchState.updateSearchState(error = AppError.Present(e.message ?: "Error while loading"))
            } finally {
                _followState.updateFollowState(isLoading = false)
                _searchState.updateSearchState(isLoading = false)
            }
        }
    }

    private fun getMyProfileUrl() {
        viewModelScope.launch {
            _searchState.updateSearchState(
                isLoading = true,
                error = AppError.Absent,
            )
            try {
                profilesRepository.getMyUserEntry().avatarUrl
               _searchState.updateSearchState(myProfileUrl =  profilesRepository.getMyUserEntry().avatarUrl)
            } catch (e: Exception) {
                _searchState.updateSearchState(error = AppError.Present(e.message ?: "Error loading Profile immage"))
            } finally {
                _searchState.updateSearchState(isLoading = false)
            }
        }
    }

    private fun refreshFriends() {
        viewModelScope.launch {
            loadFriendsProfiles()
        }
    }
    private fun loadRanking(sortBy: SortBy) {
        viewModelScope.launch {
            _followState.updateFollowState(
                isLoading = true,
                error = AppError.Absent
            )
            try {
                val entries: List<UserRankEntry> = coroutineScope {
                    profilesRepository.getProfileIFollow()
                        .plus(profilesRepository.getMyProfile()).map { profile ->
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
                _followState.updateFollowState(rankEntry =  when (sortBy) {
                    SortBy.Kilometers -> entries.sortedByDescending { it.weekKm }
                    SortBy.Level      -> entries.sortedByDescending { it.level }
                })
                myProfile = profilesRepository.getMyProfile()
                _followState.updateFollowState(myUserId = myProfile?.id)
            } catch (e: Exception) {
                _followState.updateFollowState(error = AppError.Present(e.message ?: "Error loading rank"))
            } finally {
                _followState.updateFollowState(isLoading = false)
            }
        }
    }

    private fun buildAvatarUrl(avatarPath: String): String {
        return profilesRepository.avatarUrl(avatarPath)
    }

    private fun loadFeed() {
        viewModelScope.launch {
            _followState.updateFollowState(
                isLoading = true,
                error = AppError.Absent,
            )
            try {
                val allRuns = profilesRepository.getProfileIFollow().flatMap { profile ->
                    runsRepository.getRunsByUserId(profile.id)
                        .map { run ->
                            RunFeedEntry(
                                runId       = run.id,
                                userId      = profile.id,
                                displayName = profile.username,
                                avatarUrl   = profile.avatarPath,
                                startTime   = run.startTimeLocal,
                                pathUrl     = run.previewPath,
                                distance    = run.distanceMeters.toDouble() / 1000.0,
                            )
                        }
                }
                _followState.updateFollowState(feedEntry = allRuns.sortedByDescending { it.startTime })
            } catch (e: Exception) {
                _followState.updateFollowState(error = AppError.Present(e.message ?: "Error loading feed"))
            } finally {
                _followState.updateFollowState(isLoading = false)
            }
        }
    }

    private fun getAvatarUrl(userId: String): String? {
        val allProfiles = _searchState.value.friendsName + listOfNotNull(myProfile)
        return allProfiles
            .find { it.id == userId }
            ?.avatarPath
            ?.let { profilesRepository.avatarUrl(it) }
    }

    private fun MutableStateFlow<FollowState>.updateFollowState(
        isLoading: Boolean? = null,
        rankEntry: List<UserRankEntry>? = null,
        feedEntry: List<RunFeedEntry>? = null,
        myUserId: String? = null,
        error: AppError? = null,
    ){
        value = value.copy(
            isLoading = isLoading ?: value.isLoading,
            rankEntry = rankEntry ?: value.rankEntry,
            feedEntry = feedEntry ?: value.feedEntry,
            myUserId = myUserId ?: value.myUserId,
            error = error ?: value.error

        )
    }

    private fun MutableStateFlow<SearchState>.updateSearchState(
        isLoading: Boolean? = null,
        friendsName: List<Profile>? = null,
        notFriends: List<Profile>? = null,
        myProfileUrl: String? = null,
        error: AppError? = null,
    ){
        value = value.copy(
            isLoading = isLoading ?: value.isLoading,
            friendsName = friendsName ?: value.friendsName,
            notFriends = notFriends?: value.notFriends,
            myProfileUrl = myProfileUrl?: value.myProfileUrl,
            error = error ?: value.error
        )

    }
}