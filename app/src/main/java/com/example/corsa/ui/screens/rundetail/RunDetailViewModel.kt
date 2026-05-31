package com.example.corsa.ui.screens.rundetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.Run
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Instant

data class CommentEntry(
    val commentId: String,
    val runId: String,
    val commentContent: String,
    val commentCreatedAt: Instant,
    val commentUpdatedAt: Instant,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarPath: String? = null,
)

sealed interface RunDetailState {
    data object Loading : RunDetailState
    data class Error(val message: String) : RunDetailState
    data class Success(
        val run: Run,
        val comments: List<CommentEntry>,
        val likeCount: Int,
        val myUserId: String,
        val runnerProfile: Profile,
        val alreadyLiked: Boolean
    ) : RunDetailState
}

class RunDetailViewModel(
    private val runRepository: RunsRepository,
    private val profileRepository: ProfilesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _runId: String? = savedStateHandle["runId"]
    private val _shareToken: String? = savedStateHandle["shareToken"]

    private val _runDetailState = MutableStateFlow<RunDetailState>(RunDetailState.Loading)
    val runDetailState = _runDetailState.asStateFlow()

    init {
        loadRun()
    }

    private fun loadRun() {
        viewModelScope.launch {
            _runDetailState.value = RunDetailState.Loading
            try {
                val run = when {
                    _runId != null -> runRepository.getRunById(_runId)
                    _shareToken != null -> runRepository.getRunByShareToken(_shareToken)
                    else -> error("No runId or shareToken provided")
                }
                val comments = runRepository.getCommentsById(run.id)
                val likeCount = runRepository.getLikeCountById(run.id)
                val alreadyLiked = runRepository.isLikedByMeById(run.id)
                val runnerProfile = profileRepository.getProfileByUserId(run.userId)
                val myUserId = profileRepository.getMyProfile().id
                _runDetailState.value = RunDetailState.Success(
                    run = run,
                    comments = comments,
                    likeCount = likeCount,
                    myUserId = myUserId,
                    runnerProfile = runnerProfile,
                    alreadyLiked = alreadyLiked
                )
            } catch (e: Exception) {
                _runDetailState.value = RunDetailState.Error(
                    e.message ?: "Error while trying to retrieve run detail"
                )
            }
        }
    }

    fun toggleLike() {
        val currentState = _runDetailState.value as? RunDetailState.Success ?: return

        // Optimistic update
        val wasLiked = currentState.alreadyLiked
        val runId = currentState.run.id
        _runDetailState.value = currentState.copy(
            alreadyLiked = !wasLiked,
            likeCount = if (wasLiked) currentState.likeCount - 1 else currentState.likeCount + 1
        )

        viewModelScope.launch {
            try {
                if (wasLiked) {
                    runRepository.removeLikeFromARun(runId)
                } else {
                    runRepository.addLikeToARun(runId)
                }
            } catch (e: Exception) {
                // Rollback
                _runDetailState.value = currentState.copy(
                    alreadyLiked = wasLiked,
                    likeCount = currentState.likeCount
                )
            }
        }
    }

    fun onAddComment(content: String) {
        val currentState = _runDetailState.value as? RunDetailState.Success ?: return
        val runId = currentState.run.id  // ← same here

        viewModelScope.launch {
            try {
                runRepository.addCommentToARun(runId, content)
                val comments = runRepository.getCommentsById(runId)  // re-fetch
                val currentState = _runDetailState.value
                if (currentState is RunDetailState.Success) {
                    _runDetailState.value = currentState.copy(comments = comments)
                }
            } catch (e: Exception) {
                _runDetailState.value = RunDetailState.Error(
                    e.message ?: "Error while trying to add a comment"
                )
            }
        }
    }
}