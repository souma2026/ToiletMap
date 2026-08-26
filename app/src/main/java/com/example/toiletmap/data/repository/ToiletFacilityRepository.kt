package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.Toilet
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


data class ToiletFacilityRewardUpdateResult(
    val toilet: Toilet,
    val earnedPoints: Int,
    val remainingRewardPoints: Int
)


class ToiletFacilityRepository {

    private val supabase =
        SupabaseClientProvider.client

    private val toiletRepository =
        ToiletRepository()


    /*
     * 他ファイルと絶対に名前が重複しないように
     * Repositoryの内部クラスにしている。
     */
    @Serializable
    private data class FacilityRpcResponseV2(

        @SerialName("earned_points")
        val earnedPoints: Int,

        @SerialName("remaining_reward_points")
        val remainingRewardPoints: Int
    )


    suspend fun updateToiletFacilities(

        toiletId: String,

        maleWesternToiletCount: Int?,
        maleJapaneseToiletCount: Int?,

        femaleWesternToiletCount: Int?,
        femaleJapaneseToiletCount: Int?,

        hasBabyChair: Boolean?,
        hasDiaperChangingTable: Boolean?,
        hasAccessibleStall: Boolean?,
        hasOstomate: Boolean?

    ): ToiletFacilityRewardUpdateResult {

        /*
         * ログイン確認
         */
        if (
            supabase.auth.currentUserOrNull() == null
        ) {
            throw IllegalStateException(
                "設備情報を編集するにはログインが必要です"
            )
        }


        val normalizedToiletId =
            toiletId.trim()


        require(
            normalizedToiletId.isNotBlank()
        ) {
            "トイレIDが不正です"
        }


        /*
         * 台数チェック
         */
        require(
            maleWesternToiletCount == null ||
                    maleWesternToiletCount >= 0
        ) {
            "男子・洋式トイレ数は0以上で入力してください"
        }


        require(
            maleJapaneseToiletCount == null ||
                    maleJapaneseToiletCount >= 0
        ) {
            "男子・和式トイレ数は0以上で入力してください"
        }


        require(
            femaleWesternToiletCount == null ||
                    femaleWesternToiletCount >= 0
        ) {
            "女子・洋式トイレ数は0以上で入力してください"
        }


        require(
            femaleJapaneseToiletCount == null ||
                    femaleJapaneseToiletCount >= 0
        ) {
            "女子・和式トイレ数は0以上で入力してください"
        }


        /*
         * RPCへ渡す値
         */
        val parameters =
            buildJsonObject {

                put(
                    "p_toilet_id",
                    normalizedToiletId
                )


                if (maleWesternToiletCount == null) {
                    put(
                        "p_male_western_toilet_count",
                        JsonNull
                    )
                } else {
                    put(
                        "p_male_western_toilet_count",
                        maleWesternToiletCount
                    )
                }


                if (maleJapaneseToiletCount == null) {
                    put(
                        "p_male_japanese_toilet_count",
                        JsonNull
                    )
                } else {
                    put(
                        "p_male_japanese_toilet_count",
                        maleJapaneseToiletCount
                    )
                }


                if (femaleWesternToiletCount == null) {
                    put(
                        "p_female_western_toilet_count",
                        JsonNull
                    )
                } else {
                    put(
                        "p_female_western_toilet_count",
                        femaleWesternToiletCount
                    )
                }


                if (femaleJapaneseToiletCount == null) {
                    put(
                        "p_female_japanese_toilet_count",
                        JsonNull
                    )
                } else {
                    put(
                        "p_female_japanese_toilet_count",
                        femaleJapaneseToiletCount
                    )
                }


                if (hasBabyChair == null) {
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


                if (hasDiaperChangingTable == null) {
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


                if (hasAccessibleStall == null) {
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


                if (hasOstomate == null) {
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
         * Supabase
         *
         * 男子:
         * 洋式＋和式完成 → +1pt
         *
         * 女子:
         * 洋式＋和式完成 → +1pt
         */
        val rpcResult =
            supabase
                .postgrest
                .rpc(
                    function =
                        "update_toilet_facilities_with_reward_v2",

                    parameters =
                        parameters
                )
                .decodeSingle<FacilityRpcResponseV2>()


        /*
         * 更新後の詳細を取得
         */
        val updatedToilet =
            toiletRepository
                .loadToiletById(
                    normalizedToiletId
                )
                ?: throw IllegalStateException(
                    "更新後のトイレ情報を取得できませんでした"
                )


        return ToiletFacilityRewardUpdateResult(

            toilet =
                updatedToilet,

            earnedPoints =
                rpcResult.earnedPoints,

            remainingRewardPoints =
                rpcResult.remainingRewardPoints
        )
    }
}