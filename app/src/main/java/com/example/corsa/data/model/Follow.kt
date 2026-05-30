package com.example.corsa.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class Follow @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String,
    @SerialName("follower_id")
    val followerId: String,
    @SerialName("following_id")
    val followingId: String,
    @SerialName("created_at")
    val createdAt:Instant,
)

@Serializable
data class FollowInsert(
    @SerialName("follower_id")
    val followerId: String,

    @SerialName("following_id")
    val followingId: String,
)