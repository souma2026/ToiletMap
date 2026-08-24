package com.example.toiletmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ToiletReview(

    val id: String,

    @SerialName("toilet_id")
    val toiletId: String,

    @SerialName("user_id")
    val userId: String,

    val username: String,

    val rating: Int,

    val comment: String,

    @SerialName("created_at")
    val createdAt: String
)