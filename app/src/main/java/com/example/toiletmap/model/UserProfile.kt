package com.example.toiletmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val username: String,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    /*
     * 既存ポイント。
     * これまでのアカウント機能との互換性を維持するため残す。
     */
    val points: Int = 0,

    /*
     * 清掃完了でもらえる報酬ポイント。
     * 第5段階から利用する。
     */
    @SerialName("reward_points")
    val rewardPoints: Int = 0
)