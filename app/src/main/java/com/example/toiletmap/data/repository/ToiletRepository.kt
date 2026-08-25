package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/*
 * =====================================
 * 地図表示用の軽量データ
 * =====================================
 *
 * comment
 * created_at
 * created_by
 * cleaning_requested_by
 *
 * などは地図表示時には取得しない。
 */
@Serializable
private data class MapToiletRow(

    val id: String,

    val name: String,

    val latitude: Double,

    val longitude: Double,

    @SerialName("cleaning_status")
    val cleaningStatus:
    CleaningStatus =
        CleaningStatus.NORMAL,

    @SerialName("last_cleaned_at_millis")
    val lastCleanedAtMillis:
    Long? =
        null,

    @SerialName("cleaning_reward_points")
    val cleaningRewardPoints:
    Int =
        0
) {

    /*
     * 既存UIとの互換性を保つため
     * Toiletへ変換する。
     *
     * 詳細情報はピンタップ後に
     * 別途Supabaseから取得する。
     */
    fun toToilet(): Toilet {

        return Toilet(

            id =
                id,

            name =
                name,

            latitude =
                latitude,

            longitude =
                longitude,

            /*
             * 地図一覧では取得しない
             */
            cleanliness =
                0,

            comment =
                "",

            cleaningStatus =
                cleaningStatus,

            lastCleanedAtMillis =
                lastCleanedAtMillis,

            cleaningRewardPoints =
                cleaningRewardPoints
        )
    }
}


/*
 * =====================================
 * 検索結果用の超軽量データ
 * =====================================
 *
 * 検索時は
 *
 * id
 * name
 * latitude
 * longitude
 *
 * だけ取得する。
 */
@Serializable
private data class SearchToiletRow(

    val id: String,

    val name: String,

    val latitude: Double,

    val longitude: Double
) {

    fun toToilet(): Toilet {

        return Toilet(

            id =
                id,

            name =
                name,

            latitude =
                latitude,

            longitude =
                longitude,

            cleanliness =
                0,

            comment =
                ""
        )
    }
}


/*
 * =====================================
 * 新規登録用
 * =====================================
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

    private val supabase =
        SupabaseClientProvider.client


    /*
     * =====================================
     * 現在地図に表示する一覧
     * =====================================
     */
    private val _toilets =
        MutableStateFlow<List<Toilet>>(
            emptyList()
        )


    val toilets:
            StateFlow<List<Toilet>> =
        _toilets.asStateFlow()


    /*
     * =====================================
     * 指定範囲だけ取得
     * =====================================
     *
     * 重要：
     *
     * select *
     *
     * ではなく必要な列だけ取得する。
     */
    suspend fun loadToiletsInBounds(

        south: Double,

        north: Double,

        west: Double,

        east: Double

    ) {

        require(
            south < north
        ) {
            "緯度範囲が不正です"
        }


        require(
            west < east
        ) {
            "経度範囲が不正です"
        }


        val rows =

            supabase
                .from(
                    "toilets"
                )
                .select(

                    columns =
                        Columns.list(
                            "id",
                            "name",
                            "latitude",
                            "longitude",
                            "cleaning_status",
                            "last_cleaned_at_millis",
                            "cleaning_reward_points"
                        )

                ) {

                    filter {

                        gte(
                            "latitude",
                            south
                        )


                        lte(
                            "latitude",
                            north
                        )


                        gte(
                            "longitude",
                            west
                        )


                        lte(
                            "longitude",
                            east
                        )
                    }
                }
                .decodeList<MapToiletRow>()


        _toilets.value =

            rows.map {
                    row ->

                row.toToilet()
            }
    }


    /*
     * =====================================
     * 名前をSupabaseで検索
     * =====================================
     *
     * 端末上のtoiletsを検索しない。
     *
     * 最大10件。
     */
    suspend fun searchToiletsByName(

        query: String

    ): List<Toilet> {

        val normalizedQuery =
            query.trim()


        if (
            normalizedQuery.isBlank()
        ) {

            return emptyList()
        }


        val rows =

            supabase
                .from(
                    "toilets"
                )
                .select(

                    columns =
                        Columns.list(
                            "id",
                            "name",
                            "latitude",
                            "longitude"
                        )

                ) {

                    /*
                     * 最大10件
                     */
                    limit(
                        10
                    )


                    /*
                     * 大文字小文字を区別しない
                     * 部分一致検索
                     *
                     * 例：
                     *
                     * 新宿
                     *
                     * ↓
                     *
                     * %新宿%
                     */
                    filter {

                        ilike(
                            "name",
                            "%$normalizedQuery%"
                        )
                    }
                }
                .decodeList<SearchToiletRow>()


        return rows.map {
                row ->

            row.toToilet()
        }
    }


    /*
     * =====================================
     * IDからトイレ詳細を1件だけ取得
     * =====================================
     *
     * ピンタップ
     * 検索結果タップ
     *
     * の後に使用する。
     *
     * ここでは詳細画面が必要なので
     * 全項目を取得する。
     */
    suspend fun loadToiletById(

        toiletId: String

    ): Toilet? {

        val id =
            toiletId.trim()


        if (
            id.isBlank()
        ) {

            return null
        }


        return supabase
            .from(
                "toilets"
            )
            .select {

                /*
                 * 念のため最大1件
                 */
                limit(
                    1
                )


                filter {

                    eq(
                        "id",
                        id
                    )
                }
            }
            .decodeList<Toilet>()
            .firstOrNull()
    }


    /*
     * =====================================
     * 複数ID指定取得
     * =====================================
     *
     * 清掃依頼一覧などで使用する。
     *
     * これは詳細情報が必要なので
     * 従来通り全項目取得。
     */
    suspend fun loadToiletsByIds(

        toiletIds:
        List<String>

    ): List<Toilet> {

        val ids =

            toiletIds
                .distinct()
                .filter {
                    it.isNotBlank()
                }


        if (
            ids.isEmpty()
        ) {

            return emptyList()
        }


        return supabase
            .from(
                "toilets"
            )
            .select {

                filter {

                    isIn(
                        "id",
                        ids
                    )
                }
            }
            .decodeList<Toilet>()
    }


    /*
     * =====================================
     * トイレ追加
     * =====================================
     */
    suspend fun addToilet(

        toilet: Toilet

    ) {

        val currentUser =

            supabase
                .auth
                .currentUserOrNull()

                ?: throw IllegalStateException(
                    "トイレを登録するにはログインが必要です"
                )


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
                    toilet.cleaningStatus.name,

                last_cleaned_at_millis =
                    toilet.lastCleanedAtMillis,

                created_by =
                    currentUser.id
            )


        supabase
            .from(
                "toilets"
            )
            .insert(
                newToilet
            )
    }
}