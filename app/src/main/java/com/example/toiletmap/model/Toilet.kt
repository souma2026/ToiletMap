package com.example.toiletmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID


@Serializable
enum class CleaningStatus {
    NORMAL,
    REQUESTED,
    IN_PROGRESS,
    COMPLETED
}


@Serializable
data class Toilet(

    val id: String =
        UUID.randomUUID().toString(),

    val name: String,

    val latitude: Double,

    val longitude: Double,

    val cleanliness: Int,

    val comment: String,

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

    @SerialName("created_by")
    val createdBy: String? =
        null,

    @SerialName("created_at")
    val createdAt: String? =
        null,

    /*
     * =====================================
     * データの登録元
     * =====================================
     *
     * USER
     *     アプリから登録
     *
     * OSM_IMPORT
     *     OpenStreetMapから取り込み
     */
    @SerialName("source_type")
    val sourceType: String =
        "USER"
)