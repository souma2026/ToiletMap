package com.example.toiletmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID


/*
 * =====================================
 * 清掃状態
 * =====================================
 */
@Serializable
enum class CleaningStatus {

    // 通常状態：赤いピン
    NORMAL,

    // 清掃依頼中：黄色いピン
    REQUESTED,

    // 清掃担当者が決まり清掃中：青いピン
    IN_PROGRESS,

    // 履歴保存用。トイレ表示は完了後に NORMAL へ戻す
    COMPLETED
}


/*
 * =====================================
 * トイレ情報
 * =====================================
 *
 * Supabaseの
 * public.toilets
 * と対応するデータ
 */
@Serializable
data class Toilet(

    /*
     * 各トイレを区別するID
     */
    val id: String =
        UUID.randomUUID().toString(),


    /*
     * トイレ名
     */
    val name: String,


    /*
     * 緯度
     */
    val latitude: Double,


    /*
     * 経度
     */
    val longitude: Double,


    /*
     * 清潔度 1～5
     */
    val cleanliness: Int,


    /*
     * コメント
     */
    val comment: String,


    /*
     * =====================================
     * 清掃状態
     *
     * Kotlin側
     * cleaningStatus
     *
     * Supabase側
     * cleaning_status
     * =====================================
     */
    @SerialName("cleaning_status")
    val cleaningStatus:
    CleaningStatus =
        CleaningStatus.NORMAL,


    /*
     * =====================================
     * 前回清掃時刻
     *
     * Kotlin側
     * lastCleanedAtMillis
     *
     * Supabase側
     * last_cleaned_at_millis
     * =====================================
     */
    @SerialName("last_cleaned_at_millis")
    val lastCleanedAtMillis:
    Long? =
        null,


    /*
     * =====================================
     * 清掃依頼で支払われた報酬ポイント
     * =====================================
     */
    @SerialName("cleaning_reward_points")
    val cleaningRewardPoints:
    Int =
        0,


    /*
     * =====================================
     * 清掃依頼を出したユーザーID
     * =====================================
     */
    @SerialName("cleaning_requested_by")
    val cleaningRequestedBy:
    String? =
        null,


    /*
     * =====================================
     * 登録したユーザーID
     *
     * Supabase AuthのUUID
     * =====================================
     */
    @SerialName("created_by")
    val createdBy:
    String? =
        null,


    /*
     * =====================================
     * 登録日時
     *
     * Supabase側で自動作成
     * =====================================
     */
    @SerialName("created_at")
    val createdAt:
    String? =
        null
)