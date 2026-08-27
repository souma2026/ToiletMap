package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.CleaningRequest
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


/*
 * =========================================
 * 清掃完了RPCの実際の結果
 * =========================================
 *
 * Supabase complete_cleaning() が
 * 実際に処理した値を受け取る。
 */
@Serializable
data class CleaningCompletionResult(

    @SerialName("earned_points")
    val earnedPoints: Int,

    @SerialName("remaining_reward_points")
    val remainingRewardPoints: Int
)


/**
 * 清掃依頼の取得と Supabase RPC の呼び出しを担当する。
 *
 * ポイント仕様:
 * - 依頼 4pt  -> 清掃報酬 5pt
 * - 依頼 8pt  -> 清掃報酬 10pt
 * - 依頼 12pt -> 清掃報酬 15pt
 * - 清掃依頼ポイントは日本時間で1日ごとに +20pt
 * - 未使用分はリセットせず繰り越す
 *
 * ポイント残高の増減や清掃状態の変更は Android から直接 UPDATE せず、
 * PostgreSQL Function 内でロックを取得して実行する。
 */
class CleaningRepository {

    companion object {

        val SELECTABLE_REQUEST_POINTS: Set<Int> =
            setOf(
                4,
                8,
                12
            )
    }


    private val supabase =
        SupabaseClientProvider.client


    suspend fun getCurrentUserId(): String? {

        supabase
            .auth
            .awaitInitialization()


        return supabase
            .auth
            .currentUserOrNull()
            ?.id
    }


    /**
     * 現在の清掃依頼ポイントを取得する。
     *
     * 取得前にデイリー更新 RPC を呼ぶ。
     */
    suspend fun loadCurrentRequestPoints(
        userId: String
    ): Int {

        val loggedInUserId =
            requireLoggedIn(
                "依頼ポイントを確認するにはログインが必要です"
            )


        if (
            loggedInUserId != userId
        ) {

            throw IllegalStateException(
                "ログイン中のユーザー情報と一致しません"
            )
        }


        supabase
            .postgrest
            .rpc(
                function =
                    "refresh_daily_request_points"
            )


        return supabase
            .from(
                "profiles"
            )
            .select {

                limit(
                    1
                )


                filter {

                    eq(
                        "id",
                        userId
                    )
                }
            }
            .decodeList<UserProfile>()
            .firstOrNull()
            ?.requestPoints
            ?: 0
    }


    /**
     * 現在有効な清掃依頼一覧を取得する。
     *
     * 監査 #12 対応済み。
     *
     * REQUESTED / IN_PROGRESS だけを
     * Supabase側で取得する。
     */
    suspend fun loadActiveRequests():
            List<CleaningRequest> {

        return supabase
            .from(
                "cleaning_requests"
            )
            .select {

                filter {

                    isIn(
                        "status",
                        listOf(
                            CleaningStatus.REQUESTED.name,
                            CleaningStatus.IN_PROGRESS.name
                        )
                    )
                }


                order(
                    column =
                        "requested_at",

                    order =
                        Order.DESCENDING,

                    nullsFirst =
                        false
                )
            }
            .decodeList<CleaningRequest>()
    }


    suspend fun requestCleaning(

        toiletId: String,

        requestPoints: Int

    ) {

        requireLoggedIn(
            "清掃を依頼するにはログインが必要です"
        )


        require(
            requestPoints in
                    SELECTABLE_REQUEST_POINTS
        ) {

            "清掃依頼ポイントは4pt・8pt・12ptから選択してください"
        }


        supabase
            .postgrest
            .rpc(

                function =
                    "request_cleaning_with_selected_points",

                parameters =
                    buildJsonObject {

                        put(
                            "p_toilet_id",
                            toiletId
                        )


                        put(
                            "p_request_points",
                            requestPoints
                        )
                    }
            )
    }


    suspend fun acceptCleaning(
        cleaningRequestId: String
    ) {

        requireLoggedIn(
            "清掃を引き受けるにはログインが必要です"
        )


        supabase
            .postgrest
            .rpc(

                function =
                    "accept_cleaning",

                parameters =
                    buildJsonObject {

                        put(
                            "p_cleaning_request_id",
                            cleaningRequestId
                        )
                    }
            )
    }


    /*
     * =========================================
     * 清掃完了
     * =========================================
     *
     * 監査 #14 対応。
     *
     * 修正前:
     *
     * RPCを実行するだけで戻り値を使わない。
     *
     *
     * 修正後:
     *
     * DBが実際に付与した
     *
     * earned_points
     * remaining_reward_points
     *
     * をdecodeしてAndroidへ返す。
     */
    suspend fun completeCleaning(
        cleaningRequestId: String
    ): CleaningCompletionResult {

        requireLoggedIn(
            "清掃を完了するにはログインが必要です"
        )


        return supabase
            .postgrest
            .rpc(

                function =
                    "complete_cleaning",

                parameters =
                    buildJsonObject {

                        put(
                            "p_cleaning_request_id",
                            cleaningRequestId
                        )
                    }
            )
            .decodeSingle<CleaningCompletionResult>()
    }


    suspend fun cancelCleaning(
        cleaningRequestId: String
    ) {

        requireLoggedIn(
            "清掃担当をキャンセルするにはログインが必要です"
        )


        supabase
            .postgrest
            .rpc(

                function =
                    "cancel_cleaning",

                parameters =
                    buildJsonObject {

                        put(
                            "p_cleaning_request_id",
                            cleaningRequestId
                        )
                    }
            )
    }


    suspend fun cancelCleaningRequest(
        cleaningRequestId: String
    ) {

        requireLoggedIn(
            "清掃依頼を取り消すにはログインが必要です"
        )


        supabase
            .postgrest
            .rpc(

                function =
                    "cancel_cleaning_request",

                parameters =
                    buildJsonObject {

                        put(
                            "p_cleaning_request_id",
                            cleaningRequestId
                        )
                    }
            )
    }


    private suspend fun requireLoggedIn(
        message: String
    ): String {

        supabase
            .auth
            .awaitInitialization()


        return supabase
            .auth
            .currentUserOrNull()
            ?.id
            ?: throw IllegalStateException(
                message
            )
    }
}