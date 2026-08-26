package com.example.toiletmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class RewardRedemption(

    val id: String,

    @SerialName("item_name")
    val itemName: String,

    @SerialName("points_used")
    val pointsUsed: Int,

    val status: String,

    /*
     * 将来メール送信を実装するときに使用する。
     *
     * 現在は PENDING のままで問題なし。
     */
    @SerialName("email_status")
    val emailStatus: String = "PENDING",

    @SerialName("created_at")
    val createdAt: String
)