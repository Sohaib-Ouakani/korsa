package com.example.corsa.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Likes @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid,
    @SerialName("run_id")
    val runId: Uuid,
    @SerialName("profile_id")
    val profileId: Uuid,
    @SerialName("created_at")
    val createdAt: Instant,
)
