package com.example.toiletmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ToiletEditHistory(
    val id: Long,

    @SerialName("user_id")
    val userId: String,

    @SerialName("toilet_name")
    val toiletName: String,

    val action: String,

    @SerialName("edited_at")
    val editedAt: String
)

@Serializable
data class NewToiletEditHistory(

    @SerialName("user_id")
    val userId: String,

    @SerialName("toilet_name")
    val toiletName: String,

    val action: String
)