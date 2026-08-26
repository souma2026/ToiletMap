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
     */
    @SerialName("reward_points")
    val rewardPoints: Int = 0,

    /*
     * 清掃依頼を出すために使うポイント。
     * 毎日、日本時間の日付変更を基準に20pt加算される。
     * 上限でリセットせず、未使用分は翌日以降へ繰り越す。
     */
    @SerialName("request_points")
    val requestPoints: Int = 20,

    /*
     * 最後にデイリー清掃依頼ポイントを更新した日。
     * Supabase の date 型を YYYY-MM-DD の文字列として受け取る。
     */
    @SerialName("last_daily_claim")
    val lastDailyClaim: String? = null
)
