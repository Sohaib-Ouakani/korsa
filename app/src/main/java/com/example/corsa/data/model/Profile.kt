package com.example.corsa.data.model

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    @SerialName("auth_user_id")
    val authUserId: String,
    val username: String,
    @SerialName("avatar_path")
    val avatarPath: String? = null,
    val level: Int = 1,
    @SerialName("completed_challenges")
    val completedChallenges: Int = 0,
    @SerialName("total_km")
    val totalKm: Float = 0f,
    @SerialName("created_at")
    val createdAt: Instant? = null,
    @SerialName("updated_at")
    val updatedAt: Instant? = null,
)