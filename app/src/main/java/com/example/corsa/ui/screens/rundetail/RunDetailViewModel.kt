package com.example.corsa.ui.screens.rundetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.Run
import com.example.corsa.data.repositories.ProfilesRepository
import com.example.corsa.data.repositories.RunsRepository
import com.example.corsa.utils.AppError
import com.example.corsa.utils.Option
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.JsonNull
import kotlin.time.Instant

data class CommentEntry(
    val commentId: String,
    val runId: String,
    val commentContent: String,
    val commentCreatedAt: LocalDateTime,
    val commentUpdatedAt: LocalDateTime,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarPath: String? = null,
)

data class RunDetailState(
    val isLoading: Boolean,
    val run: Run = emptyRun,
    val comments: List<CommentEntry> = listOf(),
    val likeCount: Int = 0,
    val myUserId: String = "",
    val runnerProfile: Profile = emptyProfile,
    val alreadyLiked: Boolean = false,
    val error: AppError = AppError.Absent
)

data class RunDetailActions(
    val toggleLike: () -> Unit,
    val onAddComment: (content: String) -> Unit
)

class RunDetailViewModel(
    private val runRepository: RunsRepository,
    private val profileRepository: ProfilesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val runDetailActions = RunDetailActions(
        toggleLike = ::toggleLike,
        onAddComment = ::onAddComment
    )

    private val _runId: String? = savedStateHandle["runId"]
    private val _shareToken: String? = savedStateHandle["shareToken"]

    private val _runDetailState = MutableStateFlow(RunDetailState(
        isLoading = true
    ))
    val runDetailState = _runDetailState.asStateFlow()

    init {
        loadRun()
    }

    private fun loadRun() {
        viewModelScope.launch {
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
                _runDetailState.updateState(
                    run = run,
                    comments = comments,
                    likeCount = likeCount,
                    myUserId = myUserId,
                    runnerProfile = runnerProfile,
                    alreadyLiked = alreadyLiked,
                    error = AppError.Absent,
                )
            } catch (e: Exception) {
                _runDetailState.updateState(error = AppError.Present(e.message ?: "Error while loading run"))
            } finally {
                _runDetailState.updateState(isLoading = false)
            }
        }
    }

    private fun toggleLike() {
        viewModelScope.launch {
            _runDetailState.updateState(
                error = AppError.Absent
            )
            val isLiked = _runDetailState.value.alreadyLiked
            val likeCount = _runDetailState.value.likeCount
            val runId = _runDetailState.value.run.id
            try {
                if (isLiked) {
                    runRepository.removeLikeFromARun(runId)
                } else {
                    runRepository.addLikeToARun(runId)
                }
                _runDetailState.updateState(
                    alreadyLiked = !isLiked,
                    likeCount = if (isLiked) likeCount - 1 else likeCount + 1
                )
            } catch (e: Exception) {
                _runDetailState.updateState(error = AppError.Present(e.message ?: "Error while like toggle"))
            } finally {
                _runDetailState.updateState(isLoading = false)
            }
        }
    }

    private fun onAddComment(content: String) {
        viewModelScope.launch {
            _runDetailState.updateState(
                error = AppError.Absent,
            )
            val runId = _runDetailState.value.run.id
            try {
                runRepository.addCommentToARun(runId, content)

                // Refresh all comments, this will trigger a recomposition
                val comments = runRepository.getCommentsById(runId)
                _runDetailState.updateState(comments = comments)
            } catch (e: Exception) {
                _runDetailState.updateState(error = AppError.Present(e.message ?: "Error while adding comment"))
            } finally {
                _runDetailState.updateState(isLoading = false)
            }
        }
    }

    private fun MutableStateFlow<RunDetailState>.updateState(
        isLoading: Boolean? = null,
        run: Run? = null,
        comments: List<CommentEntry>? = null,
        likeCount: Int? = null,
        myUserId: String? = null,
        runnerProfile: Profile? = null,
        alreadyLiked: Boolean? = null,
        error: AppError? = null,
    ) {
        value = value.copy(
            isLoading = isLoading ?: value.isLoading,
            run = run ?: value.run,
            comments = comments ?: value.comments,
            likeCount = likeCount ?: value.likeCount,
            myUserId = myUserId ?: value.myUserId,
            runnerProfile = runnerProfile ?: value.runnerProfile,
            alreadyLiked = alreadyLiked ?: value.alreadyLiked,
            error = error ?: value.error,
        )
    }
}

private val emptyRun = Run(
    id = "",
    userId = "",
    startTime = Instant.DISTANT_PAST,
    endTime = Instant.DISTANT_PAST,
    path = JsonNull,
    distanceMeters = 0f,
    meanPaceSeconds = 0,
    temperature = null,
    elevationGain = null,
    createdAt = Instant.DISTANT_PAST,
    previewPath = null,
    shareToken = null
)

private val emptyProfile = Profile(
    id = "",
    authUserId = "",
    username = "",
    avatarPath = null,
    level = 1,
    completedChallenges = 0,
    totalKm = 0f,
    createdAt = null,
    updatedAt = null
)