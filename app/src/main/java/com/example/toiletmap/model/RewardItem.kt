package com.example.toiletmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class RewardItem(

    val id: String,

    val code: String,

    val name: String,

    val description: String = "",

    @SerialName("required_points")
    val requiredPoints: Int,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("is_active")
    val isActive: Boolean = true,

    @SerialName("display_order")
    val displayOrder: Int = 0
)