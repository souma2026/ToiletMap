package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable


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
     */
    suspend fun requestCleaning(
        toiletId: String
    ) {

        /*
         * ログイン確認
         */
        supabase
            .auth
            .currentUserOrNull()
            ?: throw IllegalStateException(
                "清掃を依頼するにはログインが必要です"
            )


        /*
         * =====================================
         * NORMAL
         * ↓
         * REQUESTED
         * =====================================
         */
        supabase
            .from("toilets")
            .update(
                {

                    set(
                        "cleaning_status",
                        CleaningStatus
                            .REQUESTED
                            .name
                    )
                }
            ) {

                filter {

                    eq(
                        "id",
                        toiletId
                    )
                }
            }


        /*
         * 更新後の最新データ取得
         */
        loadToilets()
    }


    /*
     * =====================================
     * 「清掃しました」
     * =====================================
     */
    suspend fun markCleaned(
        toiletId: String
    ) {

        /*
         * ログイン確認
         */
        supabase
            .auth
            .currentUserOrNull()
            ?: throw IllegalStateException(
                "清掃状態を変更するにはログインが必要です"
            )


        /*
         * 現在時刻
         */
        val now =
            System.currentTimeMillis()


        /*
         * =====================================
         * REQUESTED
         * ↓
         * NORMAL
         *
         * 前回清掃時間も更新
         * =====================================
         */
        supabase
            .from("toilets")
            .update(
                {

                    set(
                        "cleaning_status",
                        CleaningStatus
                            .NORMAL
                            .name
                    )


                    set(
                        "last_cleaned_at_millis",
                        now
                    )
                }
            ) {

                filter {

                    eq(
                        "id",
                        toiletId
                    )
                }
            }


        /*
         * 更新後の最新データ取得
         */
        loadToilets()
    }
}