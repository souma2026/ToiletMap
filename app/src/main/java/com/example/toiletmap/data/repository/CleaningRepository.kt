package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.CleaningRequest
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


/**
 * 清掃依頼の取得と、Supabase RPC の呼び出しを担当する。
 *
 * 状態変更は Android からテーブルを直接 UPDATE せず、
 * PostgreSQL Function 内で行ロックを取得して実行する。
 */
class CleaningRepository {

    companion object {

        /**
         * 依頼者が選択できる清掃依頼ポイント。
         *
         * Supabase 側でも同じ値を検証するため、
         * Android 側の値だけを書き換えても不正な依頼は作成できない。
         */
        val SELECTABLE_REQUEST_POINTS =
            setOf(
                1,
                3,
                5
            )
    }


    private val supabase =
        SupabaseClientProvider.client


    suspend fun getCurrentUserId(): String? {

        supabase.auth.awaitInitialization()

        return supabase
            .auth
            .currentUserOrNull()
            ?.id
    }


    /**
     * デイリー回復を適用したうえで、現在の清掃依頼ポイントを取得する。
     */
    suspend fun loadCurrentRequestPoints(
        userId: String
    ): Int {

        requireLoggedIn(
            "清掃依頼ポイントを確認するにはログインが必要です"
        )

        /*
         * 日付判定は端末時刻ではなく Supabase 側で行う。
         * 同じ日に複数回呼んでも二重回復しない。
         */
        supabase
            .postgrest
            .rpc(
                function =
                    "refresh_daily_request_points"
            )

        return supabase
            .from("profiles")
            .select {

                filter {

                    eq(
                        "id",
                        userId
                    )
                }
            }
            .decodeSingle<UserProfile>()
            .requestPoints
    }


    suspend fun loadActiveRequests(): List<CleaningRequest> {

        /*
         * 件数が小さい段階では全件取得してから active のみを残す。
         * completed は履歴として DB に保持するが、通常画面には表示しない。
         *
         * 報酬が高い依頼を先にし、同じ報酬なら古い依頼を先にする。
         */
        return supabase
            .from("cleaning_requests")
            .select()
            .decodeList<CleaningRequest>()
            .filter {
                it.status == CleaningStatus.REQUESTED ||
                        it.status == CleaningStatus.IN_PROGRESS
            }
            .sortedWith(
                compareByDescending<CleaningRequest> {
                    it.rewardPoints
                }.thenBy {
                    it.requestedAt ?: it.createdAt.orEmpty()
                }
            )
    }


    suspend fun requestCleaning(
        toiletId: String,
        requestPoints: Int
    ) {

        requireLoggedIn(
            "清掃を依頼するにはログインが必要です"
        )

        require(
            requestPoints in SELECTABLE_REQUEST_POINTS
        ) {
            "清掃依頼ポイントは1pt・3pt・5ptから選択してください"
        }

        supabase
            .postgrest
            .rpc(
                function = "request_cleaning_with_selected_points",
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
                function = "accept_cleaning",
                parameters =
                    buildJsonObject {
                        put(
                            "p_cleaning_request_id",
                            cleaningRequestId
                        )
                    }
            )
    }


    suspend fun completeCleaning(
        cleaningRequestId: String
    ) {

        requireLoggedIn(
            "清掃を完了するにはログインが必要です"
        )

        supabase
            .postgrest
            .rpc(
                function = "complete_cleaning",
                parameters =
                    buildJsonObject {
                        put(
                            "p_cleaning_request_id",
                            cleaningRequestId
                        )
                    }
            )
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
                function = "cancel_cleaning",
                parameters =
                    buildJsonObject {
                        put(
                            "p_cleaning_request_id",
                            cleaningRequestId
                        )
                    }
            )
    }


    /**
     * 依頼者本人が、担当者決定前の清掃依頼を取り消す。
     *
     * 清掃担当者側の cancelCleaning とは別の RPC を使用する。
     */
    suspend fun cancelCleaningRequest(
        cleaningRequestId: String
    ) {

        requireLoggedIn(
            "清掃依頼を取り消すにはログインが必要です"
        )

        supabase
            .postgrest
            .rpc(
                function = "cancel_cleaning_request",
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
    ) {

        supabase
            .auth
            .awaitInitialization()

        supabase
            .auth
            .currentUserOrNull()
            ?: throw IllegalStateException(
                message
            )
    }
}
