package com.example.corsa.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class Like @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String,
    @SerialName("run_id")
    val runId: String,
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("created_at")
    val createdAt: Instant,
)
