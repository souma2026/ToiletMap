package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


/*
 * =====================================
 * 地図表示用
 * =====================================
 */
@Serializable
private data class MapToiletRow(

    val id: String,

    val name: String,

    val latitude: Double,

    val longitude: Double,

    @SerialName("cleaning_status")
    val cleaningStatus: CleaningStatus =
        CleaningStatus.NORMAL,

    @SerialName("last_cleaned_at_millis")
    val lastCleanedAtMillis: Long? =
        null,

    @SerialName("cleaning_reward_points")
    val cleaningRewardPoints: Int =
        0
) {

    fun toToilet(): Toilet {

        return Toilet(

            id = id,

            name = name,

            latitude = latitude,

            longitude = longitude,

            cleanliness = 0,

            comment = "",

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
 * 検索結果用
 * =====================================
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

            id = id,

            name = name,

            latitude = latitude,

            longitude = longitude,

            cleanliness = 0,

            comment = ""
        )
    }
}


/*
 * =====================================
 * 新規トイレ登録
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


/*
 * =====================================
 * Supabase RPCから返される結果
 * =====================================
 */
@Serializable
private data class ToiletFacilityRewardRpcResult(

    @SerialName("earned_points")
    val earnedPoints: Int,

    @SerialName("remaining_reward_points")
    val remainingRewardPoints: Int
)


/*
 * =====================================
 * Android画面へ返す設備更新結果
 * =====================================
 */
data class ToiletFacilityUpdateResult(

    val toilet: Toilet,

    val earnedPoints: Int,

    val remainingRewardPoints: Int
)


class ToiletRepository {

    private val supabase =
        SupabaseClientProvider.client


    /*
     * =====================================
     * 地図に表示するトイレ一覧
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
     * 地図の表示範囲だけ取得
     * =====================================
     *
     * 設備情報は取得しない。
     *
     * ピンタップ後に
     * loadToiletById()
     * で詳細取得する。
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
     * 名前検索
     * =====================================
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

                    limit(
                        10
                    )


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
     * トイレ詳細
     * =====================================
     *
     * ここでは設備情報も取得する。
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
     * 複数トイレ取得
     * =====================================
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
     * 設備情報更新
     *
     * ＋
     *
     * 設備情報提供ポイント
     * =====================================
     *
     * 新しく登録した設備1項目につき
     * reward_points +1
     *
     * ポイント判定はAndroidではなく
     * Supabase RPC側で行う。
     */
    suspend fun updateToiletFacilities(

        toiletId: String,

        westernToiletCount: Int?,

        japaneseToiletCount: Int?,

        hasBabyChair: Boolean?,

        hasDiaperChangingTable: Boolean?,

        hasAccessibleStall: Boolean?,

        hasOstomate: Boolean?

    ): ToiletFacilityUpdateResult {

        /*
         * =====================================
         * ログイン確認
         * =====================================
         */
        if (
            supabase
                .auth
                .currentUserOrNull() == null
        ) {

            throw IllegalStateException(
                "設備情報を編集するにはログインが必要です"
            )
        }


        /*
         * =====================================
         * ID確認
         * =====================================
         */
        val normalizedToiletId =
            toiletId.trim()


        require(
            normalizedToiletId.isNotBlank()
        ) {

            "トイレIDが不正です"
        }


        /*
         * =====================================
         * 台数確認
         * =====================================
         */
        require(

            westernToiletCount == null ||
                    westernToiletCount >= 0

        ) {

            "洋式トイレ数は0以上で入力してください"
        }


        require(

            japaneseToiletCount == null ||
                    japaneseToiletCount >= 0

        ) {

            "和式トイレ数は0以上で入力してください"
        }


        /*
         * =====================================
         * RPCパラメータ
         * =====================================
         */
        val parameters =

            buildJsonObject {

                put(
                    "p_toilet_id",
                    normalizedToiletId
                )


                if (
                    westernToiletCount == null
                ) {

                    put(
                        "p_western_toilet_count",
                        JsonNull
                    )

                } else {

                    put(
                        "p_western_toilet_count",
                        westernToiletCount
                    )
                }


                if (
                    japaneseToiletCount == null
                ) {

                    put(
                        "p_japanese_toilet_count",
                        JsonNull
                    )

                } else {

                    put(
                        "p_japanese_toilet_count",
                        japaneseToiletCount
                    )
                }


                if (
                    hasBabyChair == null
                ) {

                    put(
                        "p_has_baby_chair",
                        JsonNull
                    )

                } else {

                    put(
                        "p_has_baby_chair",
                        hasBabyChair
                    )
                }


                if (
                    hasDiaperChangingTable == null
                ) {

                    put(
                        "p_has_diaper_changing_table",
                        JsonNull
                    )

                } else {

                    put(
                        "p_has_diaper_changing_table",
                        hasDiaperChangingTable
                    )
                }


                if (
                    hasAccessibleStall == null
                ) {

                    put(
                        "p_has_accessible_stall",
                        JsonNull
                    )

                } else {

                    put(
                        "p_has_accessible_stall",
                        hasAccessibleStall
                    )
                }


                if (
                    hasOstomate == null
                ) {

                    put(
                        "p_has_ostomate",
                        JsonNull
                    )

                } else {

                    put(
                        "p_has_ostomate",
                        hasOstomate
                    )
                }
            }


        /*
         * =====================================
         * ポイント対応RPC
         * =====================================
         */
        val rewardResult =

            supabase
                .postgrest
                .rpc(

                    function =
                        "update_toilet_facilities_with_reward",

                    parameters =
                        parameters
                )
                .decodeSingle<ToiletFacilityRewardRpcResult>()


        /*
         * =====================================
         * 最新のトイレ詳細を再取得
         * =====================================
         */
        val updatedToilet =

            loadToiletById(
                normalizedToiletId
            )

                ?: throw IllegalStateException(
                    "更新後のトイレ情報を取得できませんでした"
                )


        /*
         * =====================================
         * 画面へ結果を返す
         * =====================================
         */
        return ToiletFacilityUpdateResult(

            toilet =
                updatedToilet,

            earnedPoints =
                rewardResult.earnedPoints,

            remainingRewardPoints =
                rewardResult.remainingRewardPoints
        )
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