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
    val rewardPoints: Int = 0,

    /*
     * 清掃依頼を出すために使うポイント。
     * 毎日、日本時間の0:00を基準に10ptまで回復する。
     */
    @SerialName("request_points")
    val requestPoints: Int = 10,

    /*
     * 最後にデイリー清掃依頼ポイントを更新した日。
     * Supabase の date 型を YYYY-MM-DD の文字列として受け取る。
     */
    @SerialName("last_daily_claim")
    val lastDailyClaim: String? = null
)