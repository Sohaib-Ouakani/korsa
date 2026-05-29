package com.example.corsa.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Follows @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid,
    @SerialName("follower_id")
    val followerId: Uuid,
    @SerialName("following_id")
    val followingId: Uuid,
)