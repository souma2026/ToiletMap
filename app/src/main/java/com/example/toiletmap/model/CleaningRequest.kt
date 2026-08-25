package com.example.toiletmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Supabase の public.cleaning_requests と対応する清掃依頼。
 *
 * NORMAL は toilets.cleaning_status だけで使用し、
 * cleaning_requests には REQUESTED / IN_PROGRESS / COMPLETED を保存する。
 */
@Serializable
data class CleaningRequest(

    val id: String,

    @SerialName("toilet_id")
    val toiletId: String,

    @SerialName("requester_id")
    val requesterId: String,

    @SerialName("cleaner_id")
    val cleanerId: String? = null,

    val status: CleaningStatus,

    @SerialName("request_points_used")
    val requestPointsUsed: Int = 0,

    @SerialName("reward_points")
    val rewardPoints: Int = 5,

    @SerialName("requested_at")
    val requestedAt: String? = null,

    @SerialName("accepted_at")
    val acceptedAt: String? = null,

    @SerialName("completed_at")
    val completedAt: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
) {

    val isActive: Boolean
        get() =
            status == CleaningStatus.REQUESTED ||
                    status == CleaningStatus.IN_PROGRESS
}
