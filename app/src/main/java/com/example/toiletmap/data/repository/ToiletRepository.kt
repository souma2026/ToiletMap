package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.Toilet
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


/*
 * =====================================
 * Supabaseへ新しいトイレを登録するとき用
 * =====================================
 *
 * ToiletをそのままINSERTすると
 * created_at = null まで送ってしまう可能性があるため、
 * 登録専用のデータを分けている。
 *
 * created_at はSupabase側で自動作成される。
 */
@Serializable
private data class NewToilet(

    val id: String,

    val name: String,

    val latitude: Double,

    val longitude: Double,

    val cleanliness: Int,

    val comment: String,

    val cleaning_status: String,

    val last_cleaned_at_millis: Long?,

    val created_by: String
)


class ToiletRepository {

    /*
     * =====================================
     * Supabase
     * =====================================
     */
    private val supabase =
        SupabaseClientProvider.client


    /*
     * =====================================
     * アプリ内で使用するトイレ一覧
     * =====================================
     *
     * Supabaseから取得した内容を
     * ここに保存する。
     */
    private val _toilets =
        MutableStateFlow<List<Toilet>>(
            emptyList()
        )


    /*
     * =====================================
     * ViewModelへ公開するデータ
     * =====================================
     */
    val toilets:
            StateFlow<List<Toilet>> =
        _toilets.asStateFlow()


    /*
     * =====================================
     * Supabaseからトイレ一覧を取得
     * =====================================
     */
    suspend fun loadToilets() {

        val loadedToilets =
            supabase
                .from("toilets")
                .select()
                .decodeList<Toilet>()


        /*
         * StateFlowを更新
         */
        _toilets.value =
            loadedToilets
    }


    /*
     * =====================================
     * 新しいトイレを登録
     * =====================================
     */
    suspend fun addToilet(
        toilet: Toilet
    ) {

        /*
         * 現在ログインしているユーザーを取得
         */
        val currentUser =
            supabase
                .auth
                .currentUserOrNull()
                ?: throw IllegalStateException(
                    "トイレを登録するにはログインが必要です"
                )


        /*
         * =====================================
         * Supabase登録用データ
         * =====================================
         */
        val newToilet =
            NewToilet(

                id =
                    toilet.id,

                name =
                    toilet.name,

                latitude =
                    toilet.latitude,

                longitude =
                    toilet.longitude,

                cleanliness =
                    toilet.cleanliness,

                comment =
                    toilet.comment,

                cleaning_status =
                    toilet
                        .cleaningStatus
                        .name,

                last_cleaned_at_millis =
                    toilet.lastCleanedAtMillis,

                /*
                 * 登録したユーザーのUUID
                 */
                created_by =
                    currentUser.id
            )


        /*
         * =====================================
         * SupabaseへINSERT
         * =====================================
         */
        supabase
            .from("toilets")
            .insert(
                newToilet
            )


        /*
         * =====================================
         * 登録後に最新一覧を取得
         * =====================================
         */
        loadToilets()
    }


    /*
     * =====================================
     * 清掃を依頼する
     * =====================================
     *
     * SupabaseのRPC内で、
     * 1. 依頼者のポイントを減らす
     * 2. トイレを清掃待ちへ変更する
     * 3. 報酬ポイントを保存する
     *
     * を1つのトランザクションとして実行する。
     */
    suspend fun requestCleaning(
        toiletId: String,
        rewardPoints: Int
    ) {

        /*
         * =====================================
         * ポイントの入力チェック
         * =====================================
         */
        if (rewardPoints !in 1..10_000) {

            throw IllegalArgumentException(
                "支払うポイントは1～10000ptで指定してください"
            )
        }


        /*
         * =====================================
         * ログイン確認
         * =====================================
         */
        supabase
            .auth
            .currentUserOrNull()
            ?: throw IllegalStateException(
                "清掃を依頼するにはログインが必要です"
            )


        /*
         * =====================================
         * RPCへ渡すJSONを作成
         * =====================================
         *
         * Supabase側の関数
         *
         * request_cleaning_with_points(
         *     p_toilet_id,
         *     p_reward_points
         * )
         *
         * に合わせている。
         */
        val parameters =
            buildJsonObject {

                put(
                    "p_toilet_id",
                    toiletId
                )

                put(
                    "p_reward_points",
                    rewardPoints
                )
            }


        /*
         * =====================================
         * Supabase RPC実行
         * =====================================
         */
        supabase
            .postgrest
            .rpc(
                function =
                    "request_cleaning_with_points",

                parameters =
                    parameters
            )


        /*
         * =====================================
         * 最新状態を再取得
         * =====================================
         */
        loadToilets()
    }


    /*
     * =====================================
     * 「清掃しました」
     * =====================================
     *
     * SupabaseのRPC内で、
     *
     * 1. 清掃者へ報酬ポイントを加算する
     * 2. トイレを通常状態へ戻す
     * 3. 清掃時刻を更新する
     * 4. 使用済み報酬を0へ戻す
     *
     * を1つのトランザクションとして実行する。
     */
    suspend fun markCleaned(
        toiletId: String
    ) {

        /*
         * =====================================
         * ログイン確認
         * =====================================
         */
        supabase
            .auth
            .currentUserOrNull()
            ?: throw IllegalStateException(
                "清掃状態を変更するにはログインが必要です"
            )


        /*
         * =====================================
         * RPCへ渡すJSONを作成
         * =====================================
         *
         * Supabase側の関数
         *
         * mark_toilet_cleaned_with_points(
         *     p_toilet_id
         * )
         *
         * に合わせている。
         */
        val parameters =
            buildJsonObject {

                put(
                    "p_toilet_id",
                    toiletId
                )
            }


        /*
         * =====================================
         * Supabase RPC実行
         * =====================================
         */
        supabase
            .postgrest
            .rpc(
                function =
                    "mark_toilet_cleaned_with_points",

                parameters =
                    parameters
            )


        /*
         * =====================================
         * 最新状態を再取得
         * =====================================
         */
        loadToilets()
    }

    /*
     * =====================================
     * トイレ削除
     * =====================================
     *
     * SupabaseのRPC内で、
     * ・ログイン確認
     * ・清掃依頼中なら報酬ポイントを依頼者へ返金
     * ・関連する口コミを削除
     * ・トイレ本体を削除
     *
     * を1つのトランザクションとして実行する。
     */
    suspend fun deleteToilet(
        toiletId: String
    ) {

        supabase
            .auth
            .currentUserOrNull()
            ?: throw IllegalStateException(
                "トイレを削除するにはログインが必要です"
            )

        if (toiletId.isBlank()) {
            throw IllegalArgumentException(
                "削除するトイレが選択されていません"
            )
        }

        val parameters =
            buildJsonObject {
                put(
                    "p_toilet_id",
                    toiletId
                )
            }

        supabase
            .postgrest
            .rpc(
                function =
                    "delete_own_toilet",

                parameters =
                    parameters
            )

        loadToilets()
    }

}