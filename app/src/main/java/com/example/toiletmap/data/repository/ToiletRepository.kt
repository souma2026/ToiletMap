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
 * Supabaseへ新しいトイレを登録するときのデータ。
 * created_at はSupabase側で自動作成する。
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


    suspend fun loadToilets() {

        _toilets.value =
            supabase
                .from("toilets")
                .select()
                .decodeList<Toilet>()
    }


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

        loadToilets()
    }

    suspend fun deleteToilet(
        toiletId: String
    ) {

        supabase
            .auth
            .currentUserOrNull()
            ?: throw IllegalStateException(
                "トイレを削除するにはログインが必要です"
            )

        require(toiletId.isNotBlank()) {
            "削除するトイレが選択されていません"
        }

        supabase
            .postgrest
            .rpc(
                function = "delete_own_toilet",
                parameters =
                    buildJsonObject {
                        put(
                            "p_toilet_id",
                            toiletId
                        )
                    }
            )

        loadToilets()
    }

}
