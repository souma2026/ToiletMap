package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.Toilet
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable


/*
 * Supabaseへ新しいトイレを登録するときのデータ。
 * created_at はSupabase側で自動作成する。
 * source_type はSupabase側の default 'USER' を使用する。
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


    private val _toilets =
        MutableStateFlow<List<Toilet>>(
            emptyList()
        )

    val toilets: StateFlow<List<Toilet>> =
        _toilets.asStateFlow()


    /*
     * =====================================
     * 指定範囲のトイレだけ取得
     * =====================================
     *
     * 以前のように toilets テーブル全件を取得せず、
     * 地図で必要な緯度・経度範囲だけSupabaseへ要求する。
     */
    suspend fun loadToiletsInBounds(
        south: Double,
        north: Double,
        west: Double,
        east: Double
    ) {

        require(south < north) {
            "緯度範囲が不正です"
        }

        require(west < east) {
            "経度範囲が不正です"
        }

        _toilets.value =
            supabase
                .from("toilets")
                .select {
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
                .decodeList<Toilet>()
    }


    /*
     * =====================================
     * 指定IDのトイレを取得
     * =====================================
     *
     * 清掃依頼一覧など、地図の表示範囲外でも
     * 必要なトイレだけを追加取得するために使う。
     */
    suspend fun loadToiletsByIds(
        toiletIds: List<String>
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
            .from("toilets")
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
     *
     * 登録後の再読込はViewModel側で、
     * 現在表示中の地図範囲だけに対して行う。
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
            .from("toilets")
            .insert(
                newToilet
            )
    }
}