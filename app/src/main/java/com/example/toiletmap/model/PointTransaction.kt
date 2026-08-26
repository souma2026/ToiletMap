package com.example.toiletmap.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Supabase の public.point_transactions と対応するモデル。
 *
 * pointType:
 * - REQUEST: 清掃依頼ポイント
 * - REWARD: 清掃報酬ポイント
 *
 * amount:
 * - 正数: 獲得・回復
 * - 負数: 消費
 */
@Serializable
data class PointTransaction(
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("point_type")
    val pointType: String,

    val amount: Int,

    val reason: String,

    @SerialName("cleaning_request_id")
    val cleaningRequestId: String? = null,

    @SerialName("created_at")
    val createdAt: String
)
