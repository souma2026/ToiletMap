package com.example.toiletmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val username: String,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    val points: Int = 0
)