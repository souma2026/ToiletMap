package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.RewardItem
import com.example.toiletmap.model.RewardRedemption
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


@Serializable
data class RewardRedemptionResult(

    @SerialName("redemption_id")
    val redemptionId: String,

    @SerialName("item_name")
    val itemName: String,

    @SerialName("points_used")
    val pointsUsed: Int,

    @SerialName("remaining_reward_points")
    val remainingRewardPoints: Int,

    @SerialName("already_processed")
    val alreadyProcessed: Boolean = false
)


object PointExchangeRepository {

    private val supabase =
        SupabaseClientProvider.client


    /*
     * =========================================
     * 交換可能商品一覧
     * =========================================
     */
    suspend fun loadRewardItems(): List<RewardItem> {

        return supabase
            .from("reward_items")
            .select {
                filter {
                    eq(
                        "is_active",
                        true
                    )
                }
            }
            .decodeList<RewardItem>()
            .sortedWith(
                compareBy<RewardItem> {
                    it.requiredPoints
                }.thenBy {
                    it.displayOrder
                }.thenBy {
                    it.name
                }
            )
    }


    /*
     * =========================================
     * 自分のポイント交換履歴
     * =========================================
     *
     * SupabaseのRLSにより、
     * ログイン中ユーザー本人の履歴だけ取得する。
     *
     * 監査 #11 対応:
     *
     * Android側で50件取得後に並べ替えるのではなく、
     *
     * 1. Supabase側で created_at DESC
     * 2. その後、最大50件に制限
     *
     * とする。
     *
     * これにより履歴が50件を超えても、
     * 正しく最新50件を取得できる。
     */
    suspend fun loadRedemptionHistory(): List<RewardRedemption> {

        return supabase
            .from("reward_redemptions")
            .select(
                columns = Columns.list(
                    "id",
                    "item_name",
                    "points_used",
                    "status",
                    "email_status",
                    "created_at"
                )
            ) {

                /*
                 * 最新の交換履歴から取得
                 */
                order(
                    column = "created_at",
                    order = Order.DESCENDING
                )


                /*
                 * 最新50件だけ取得
                 */
                limit(
                    50
                )
            }
            .decodeList<RewardRedemption>()
    }


    /*
     * =========================================
     * ポイント交換
     * =========================================
     */
    suspend fun redeemRewardItem(

        rewardItemId: String,

        clientRequestId: String

    ): RewardRedemptionResult {

        val parameters =
            buildJsonObject {

                put(
                    "p_reward_item_id",
                    rewardItemId
                )

                put(
                    "p_client_request_id",
                    clientRequestId
                )
            }


        return supabase
            .postgrest
            .rpc(
                function =
                    "redeem_reward_item",

                parameters =
                    parameters
            )
            .decodeSingle<RewardRedemptionResult>()
    }
}