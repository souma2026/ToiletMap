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

    NORMAL,

    REQUESTED,

    IN_PROGRESS,

    COMPLETED
}


/*
 * =====================================
 * トイレ
 * =====================================
 */
@Serializable
data class Toilet(

    /*
     * =====================================
     * 基本情報
     * =====================================
     */
    val id: String =
        UUID.randomUUID().toString(),

    val name: String,

    val latitude: Double,

    val longitude: Double,

    val cleanliness: Int,

    val comment: String,


    /*
     * =====================================
     * 清掃情報
     * =====================================
     */
    @SerialName("cleaning_status")
    val cleaningStatus: CleaningStatus =
        CleaningStatus.NORMAL,

    @SerialName("last_cleaned_at_millis")
    val lastCleanedAtMillis: Long? =
        null,

    @SerialName("cleaning_reward_points")
    val cleaningRewardPoints: Int =
        0,

    @SerialName("cleaning_requested_by")
    val cleaningRequestedBy: String? =
        null,


    /*
     * =====================================
     * 登録情報
     * =====================================
     */
    @SerialName("created_by")
    val createdBy: String? =
        null,

    @SerialName("created_at")
    val createdAt: String? =
        null,


    /*
     * =====================================
     * データ元
     *
     * USER
     * OSM_IMPORT
     * =====================================
     */
    @SerialName("source_type")
    val sourceType: String =
        "USER",


    /*
     * =====================================
     * 旧形式
     *
     * 男女別になる前の設備情報。
     *
     * 既存データとの互換性のため
     * 削除しない。
     * =====================================
     */
    @SerialName("western_toilet_count")
    val westernToiletCount: Int? =
        null,

    @SerialName("japanese_toilet_count")
    val japaneseToiletCount: Int? =
        null,


    /*
     * =====================================
     * 男子トイレ
     *
     * null
     *   = 情報なし
     *
     * 0
     *   = 0台と確認済み
     *
     * 1以上
     *   = 確認済み台数
     * =====================================
     */
    @SerialName("male_western_toilet_count")
    val maleWesternToiletCount: Int? =
        null,

    @SerialName("male_japanese_toilet_count")
    val maleJapaneseToiletCount: Int? =
        null,


    /*
     * =====================================
     * 女子トイレ
     * =====================================
     */
    @SerialName("female_western_toilet_count")
    val femaleWesternToiletCount: Int? =
        null,

    @SerialName("female_japanese_toilet_count")
    val femaleJapaneseToiletCount: Int? =
        null,


    /*
     * =====================================
     * ベビーチェア
     *
     * null  = 情報なし
     * true  = あり
     * false = なし
     * =====================================
     */
    @SerialName("has_baby_chair")
    val hasBabyChair: Boolean? =
        null,


    /*
     * =====================================
     * おむつ交換台
     * =====================================
     */
    @SerialName("has_diaper_changing_table")
    val hasDiaperChangingTable: Boolean? =
        null,


    /*
     * =====================================
     * 車いす対応個室
     * =====================================
     */
    @SerialName("has_accessible_stall")
    val hasAccessibleStall: Boolean? =
        null,


    /*
     * =====================================
     * オストメイト
     * =====================================
     */
    @SerialName("has_ostomate")
    val hasOstomate: Boolean? =
        null,


    /*
     * =====================================
     * 設備情報更新者
     * =====================================
     */
    @SerialName("facility_updated_by")
    val facilityUpdatedBy: String? =
        null,


    /*
     * =====================================
     * 設備情報更新日時
     * =====================================
     */
    @SerialName("facility_updated_at")
    val facilityUpdatedAt: String? =
        null
)