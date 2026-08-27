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
     * 現在ログイン中のユーザーID
     * =====================================
     */
    suspend fun getCurrentUserId(): String? {

        supabase
            .auth
            .awaitInitialization()


        return supabase
            .auth
            .currentUserOrNull()
            ?.id
    }


    /*
     * =====================================
     * 口コミ一覧取得
     * =====================================
     */
    suspend fun loadReviews(
        toiletId: String
    ): List<ToiletReview> {

        if (
            toiletId.isBlank()
        ) {

            return emptyList()
        }


        return supabase
            .from(
                "toilet_reviews"
            )
            .select {

                filter {

                    eq(
                        "toilet_id",
                        toiletId
                    )
                }
            }
            .decodeList<ToiletReview>()
            .sortedByDescending {
                    review ->

                review.createdAt
            }
    }


    /*
     * =====================================
     * 現在のユーザーが
     * このトイレへ口コミ投稿済みか
     * =====================================
     */
    private suspend fun hasCurrentUserReview(
        toiletId: String,
        userId: String
    ): Boolean {

        return supabase
            .from(
                "toilet_reviews"
            )
            .select {

                limit(
                    1
                )


                filter {

                    eq(
                        "toilet_id",
                        toiletId
                    )

                    eq(
                        "user_id",
                        userId
                    )
                }
            }
            .decodeList<ToiletReview>()
            .isNotEmpty()
    }


    /*
     * =====================================
     * 口コミ追加
     * =====================================
     *
     * 1ユーザーにつき
     * 1トイレ1件まで。
     */
    suspend fun addReview(
        toiletId: String,
        rating: Int,
        comment: String
    ) {

        supabase
            .auth
            .awaitInitialization()


        val currentUser =
            supabase
                .auth
                .currentUserOrNull()
                ?: throw IllegalStateException(
                    "口コミを投稿するにはログインが必要です"
                )


        val normalizedToiletId =
            toiletId.trim()


        if (
            normalizedToiletId.isBlank()
        ) {

            throw IllegalArgumentException(
                "口コミを投稿するトイレを選択してください"
            )
        }


        if (
            rating !in 1..5
        ) {

            throw IllegalArgumentException(
                "評価は1～5で選択してください"
            )
        }


        val trimmedComment =
            comment.trim()


        if (
            trimmedComment.isBlank()
        ) {

            throw IllegalArgumentException(
                "口コミを入力してください"
            )
        }


        if (
            trimmedComment.length > 500
        ) {

            throw IllegalArgumentException(
                "口コミは500文字以内で入力してください"
            )
        }


        /*
         * すでに投稿済みなら拒否
         */
        if (
            hasCurrentUserReview(
                toiletId =
                    normalizedToiletId,

                userId =
                    currentUser.id
            )
        ) {

            throw IllegalStateException(
                "このトイレにはすでに口コミを投稿しています。投稿し直す場合は自分の口コミを削除してください"
            )
        }


        val newReview =
            NewToiletReview(

                id =
                    UUID
                        .randomUUID()
                        .toString(),

                toiletId =
                    normalizedToiletId,

                rating =
                    rating,

                comment =
                    trimmedComment
            )


        /*
         * DBのUNIQUE制約でも二重投稿を防ぐ。
         */
        try {

            supabase
                .from(
                    "toilet_reviews"
                )
                .insert(
                    newReview
                )

        } catch (
            e: Exception
        ) {

            val message =
                e.message
                    .orEmpty()


            if (
                message.contains(
                    "23505"
                ) ||
                message.contains(
                    "duplicate key",
                    ignoreCase = true
                ) ||
                message.contains(
                    "toilet_reviews_one_per_user_per_toilet_uq"
                )
            ) {

                throw IllegalStateException(
                    "このトイレにはすでに口コミを投稿しています。投稿し直す場合は自分の口コミを削除してください"
                )
            }


            throw e
        }
    }


    /*
     * =====================================
     * 自分の口コミを削除
     * =====================================
     */
    suspend fun deleteReview(
        reviewId: String
    ) {

        supabase
            .auth
            .awaitInitialization()


        val currentUser =
            supabase
                .auth
                .currentUserOrNull()
                ?: throw IllegalStateException(
                    "口コミを削除するにはログインが必要です"
                )


        val normalizedReviewId =
            reviewId.trim()


        if (
            normalizedReviewId.isBlank()
        ) {

            throw IllegalArgumentException(
                "削除する口コミを選択してください"
            )
        }


        /*
         * 自分の口コミであることを確認
         */
        val ownReview =
            supabase
                .from(
                    "toilet_reviews"
                )
                .select {

                    limit(
                        1
                    )


                    filter {

                        eq(
                            "id",
                            normalizedReviewId
                        )

                        eq(
                            "user_id",
                            currentUser.id
                        )
                    }
                }
                .decodeList<ToiletReview>()
                .firstOrNull()


        if (
            ownReview == null
        ) {

            throw IllegalStateException(
                "自分が投稿した口コミだけ削除できます"
            )
        }


        supabase
            .from(
                "toilet_reviews"
            )
            .delete {

                filter {

                    eq(
                        "id",
                        normalizedReviewId
                    )

                    eq(
                        "user_id",
                        currentUser.id
                    )
                }
            }
    }
}