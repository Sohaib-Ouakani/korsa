package com.example.corsa.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Comments @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid,
    @SerialName("run_id")
    val runId: Uuid,
    @SerialName("author_id")
    val authorId: Uuid,
    val content: String,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant,
)
