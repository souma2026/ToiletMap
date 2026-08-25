package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.CleaningRequest
import com.example.toiletmap.model.CleaningStatus
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

    private val supabase =
        SupabaseClientProvider.client


    suspend fun getCurrentUserId(): String? {

        supabase.auth.awaitInitialization()

        return supabase
            .auth
            .currentUserOrNull()
            ?.id
    }


    suspend fun loadActiveRequests(): List<CleaningRequest> {

        /*
         * 件数が小さい段階では全件取得してから active のみを残す。
         * completed は履歴として DB に保持するが、通常画面には表示しない。
         */
        return supabase
            .from("cleaning_requests")
            .select()
            .decodeList<CleaningRequest>()
            .filter {
                it.status == CleaningStatus.REQUESTED ||
                        it.status == CleaningStatus.IN_PROGRESS
            }
            .sortedByDescending {
                it.requestedAt ?: it.createdAt.orEmpty()
            }
    }


    suspend fun requestCleaning(
        toiletId: String
    ) {

        requireLoggedIn(
            "清掃を依頼するにはログインが必要です"
        )

        supabase
            .postgrest
            .rpc(
                function = "request_cleaning",
                parameters =
                    buildJsonObject {
                        put(
                            "p_toilet_id",
                            toiletId
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