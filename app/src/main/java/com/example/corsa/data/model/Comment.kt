package com.example.corsa.data.model

import com.example.corsa.ui.screens.rundetail.CommentEntry
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Serializable
data class Comment @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String,
    @SerialName("run_id")
    val runId: String,
    @SerialName("author_id")
    val authorId: String,
    val content: String,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant,
)
@Serializable
data class CommentInsert(
    @SerialName("run_id")
    val runId: String,

    @SerialName("author_id")
    val authorId: String,

    val content: String,
)

