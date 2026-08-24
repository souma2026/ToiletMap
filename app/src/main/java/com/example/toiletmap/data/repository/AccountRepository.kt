package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.NewToiletEditHistory
import com.example.toiletmap.model.ToiletEditHistory
import com.example.toiletmap.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


object AccountRepository {

    private val supabase =
        SupabaseClientProvider.client


    // =========================================
    // 新規登録
    // =========================================

    suspend fun signUp(
        email: String,
        password: String,
        userName: String
    ) {

        supabase.auth.signUpWith(
            Email
        ) {

            this.email =
                email

            this.password =
                password

            data =
                buildJsonObject {

                    put(
                        "username",
                        userName
                    )
                }
        }
    }


    // =========================================
    // ログイン
    // =========================================

    suspend fun signIn(
        email: String,
        password: String
    ) {

        supabase.auth.signInWith(
            Email
        ) {

            this.email =
                email

            this.password =
                password
        }
    }


    // =========================================
    // ログアウト
    // =========================================

    suspend fun signOut() {

        supabase.auth.signOut()
    }


    // =========================================
    // 現在ユーザー
    // =========================================

    fun getCurrentUser() =
        supabase.auth.currentUserOrNull()


    fun isLoggedIn(): Boolean {

        return getCurrentUser() != null
    }


    // =========================================
    // プロフィール取得
    // =========================================

    suspend fun loadProfile(
        userId: String
    ): UserProfile {

        return supabase
            .from("profiles")
            .select {

                filter {

                    eq(
                        "id",
                        userId
                    )
                }
            }
            .decodeSingle()
    }


    // =========================================
    // ユーザー名変更
    // =========================================

    suspend fun updateUserName(
        userId: String,
        userName: String
    ) {

        supabase
            .from("profiles")
            .update(
                {
                    set(
                        "username",
                        userName
                    )
                }
            ) {

                filter {

                    eq(
                        "id",
                        userId
                    )
                }
            }
    }


    // =========================================
    // プロフィール画像URL変更
    // =========================================

    private suspend fun updateAvatarUrl(
        userId: String,
        avatarUrl: String
    ) {

        supabase
            .from("profiles")
            .update(
                {
                    set(
                        "avatar_url",
                        avatarUrl
                    )
                }
            ) {

                filter {

                    eq(
                        "id",
                        userId
                    )
                }
            }
    }


    // =========================================
    // プロフィール写真アップロード
    // =========================================

    suspend fun uploadAvatar(
        userId: String,
        imageBytes: ByteArray
    ): String {

        val bucket =
            supabase
                .storage
                .from("avatars")


        /*
         * ユーザーごとのフォルダ
         *
         * avatars/
         *   UUID/
         *     profile.jpg
         */
        val path =
            "$userId/profile.jpg"


        bucket.upload(
            path =
                path,
            data =
                imageBytes
        ) {

            upsert =
                true
        }


        val publicUrl =
            bucket.publicUrl(
                path
            )


        /*
         * 同じファイル名を上書きするので
         * Coilキャッシュ対策として時刻を付加
         */
        val url =
            "$publicUrl?v=${System.currentTimeMillis()}"


        updateAvatarUrl(
            userId =
                userId,
            avatarUrl =
                url
        )


        return url
    }


    // =========================================
    // 履歴取得
    // =========================================

    suspend fun loadHistory(
        userId: String
    ): List<ToiletEditHistory> {

        return supabase
            .from("toilet_edit_history")
            .select {

                filter {

                    eq(
                        "user_id",
                        userId
                    )
                }
            }
            .decodeList<ToiletEditHistory>()
            .sortedByDescending {
                it.editedAt
            }
    }


    // =========================================
    // 履歴追加
    //
    // 他の画面から将来使用する
    // =========================================

    suspend fun addHistory(
        toiletName: String,
        action: String
    ) {

        val user =
            getCurrentUser()
                ?: return


        val history =
            NewToiletEditHistory(
                userId =
                    user.id,

                toiletName =
                    toiletName,

                action =
                    action
            )


        supabase
            .from("toilet_edit_history")
            .insert(
                history
            )
    }
}