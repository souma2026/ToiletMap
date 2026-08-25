package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.ToiletReview
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/*
 * Supabaseへ新しい口コミを登録するときに使うデータ
 *
 * user_id
 * username
 * created_at
 *
 * はSupabase側で設定する想定。
 */
@Serializable
private data class NewToiletReview(

    val id: String,

    @SerialName("toilet_id")
    val toiletId: String,

    val rating: Int,

    val comment: String
)

class ReviewRepository {

    private val supabase =
        SupabaseClientProvider.client


    /*
     * =====================================
     * 口コミ一覧取得
     * =====================================
     */
    suspend fun loadReviews(
        toiletId: String
    ): List<ToiletReview> {

        if (toiletId.isBlank()) {
            return emptyList()
        }

        return supabase
            .from("toilet_reviews")
            .select {

                filter {

                    eq(
                        "toilet_id",
                        toiletId
                    )
                }
            }
            .decodeList<ToiletReview>()
            .sortedByDescending { review ->

                review.createdAt
            }
    }


    /*
     * =====================================
     * 口コミ追加
     * =====================================
     */
    suspend fun addReview(
        toiletId: String,
        rating: Int,
        comment: String
    ) {

        /*
         * ログインしているか確認
         */
        supabase
            .auth
            .currentUserOrNull()
            ?: throw IllegalStateException(
                "口コミを投稿するにはログインが必要です"
            )


        /*
         * トイレID確認
         */
        if (toiletId.isBlank()) {

            throw IllegalArgumentException(
                "口コミを投稿するトイレを選択してください"
            )
        }


        /*
         * 評価確認
         */
        if (rating !in 1..5) {

            throw IllegalArgumentException(
                "評価は1～5で選択してください"
            )
        }


        val trimmedComment =
            comment.trim()


        /*
         * 空コメント防止
         */
        if (trimmedComment.isBlank()) {

            throw IllegalArgumentException(
                "口コミを入力してください"
            )
        }


        /*
         * 最大文字数
         */
        if (trimmedComment.length > 500) {

            throw IllegalArgumentException(
                "口コミは500文字以内で入力してください"
            )
        }


        /*
         * Supabaseへ送るデータ
         */
        val newReview =
            NewToiletReview(

                id =
                    UUID
                        .randomUUID()
                        .toString(),

                toiletId =
                    toiletId,

                rating =
                    rating,

                comment =
                    trimmedComment
            )


        /*
         * Supabaseへ登録
         */
        supabase
            .from("toilet_reviews")
            .insert(
                newReview
            )
    }
}