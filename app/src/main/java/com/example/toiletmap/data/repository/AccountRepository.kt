package com.example.toiletmap.data.repository

import com.example.toiletmap.data.supabase.SupabaseClientProvider
import com.example.toiletmap.model.NewToiletEditHistory
import com.example.toiletmap.model.ToiletEditHistory
import com.example.toiletmap.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
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

            this.email = email
            this.password = password

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

            this.email = email
            this.password = password
        }
    }


    // =========================================
    // ログアウト
    // =========================================

    suspend fun signOut() {

        supabase.auth.signOut()
    }


    // =========================================
    // 現在のユーザー
    // =========================================

    fun getCurrentUser() =
        supabase.auth.currentUserOrNull()


    fun isLoggedIn(): Boolean {

        return getCurrentUser() != null
    }

// =========================================
// 保存済みログイン状態を復元
// =========================================

    suspend fun restoreLoginState(): Boolean {

        /*
         * Android端末に保存されている
         * Supabase Authセッションの読み込み完了を待つ
         */
        supabase.auth.awaitInitialization()


        /*
         * 読み込み完了後にユーザーを確認
         */
        return supabase
            .auth
            .currentUserOrNull() != null
    }

    // =========================================
    // デイリー清掃依頼ポイント更新
    // =========================================

    suspend fun refreshDailyRequestPoints() {

        /*
         * 保存済みセッションの復元完了を待つ。
         */
        supabase.auth.awaitInitialization()


        /*
         * 未ログイン時は何もしない。
         */
        if (
            supabase
                .auth
                .currentUserOrNull() == null
        ) {

            return
        }


        /*
         * 日付判定・残高更新はSupabase側で行う。
         * Android端末の時計だけを信用しない。
         */
        supabase
            .postgrest
            .rpc(
                "refresh_daily_request_points"
            )
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
    // プロフィール画像URL更新
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
    // プロフィール画像アップロード
    // =========================================

    suspend fun uploadAvatar(
        userId: String,
        imageBytes: ByteArray
    ): String {

        val bucket =
            supabase
                .storage
                .from("avatars")


        val path =
            "$userId/profile.jpg"


        bucket.upload(
            path = path,
            data = imageBytes
        ) {

            upsert = true
        }


        val publicUrl =
            bucket.publicUrl(
                path
            )


        /*
         * 同じファイル名を上書きするため、
         * Coilのキャッシュ対策として
         * URLへ現在時刻を付ける。
         */
        val url =
            "$publicUrl?v=${System.currentTimeMillis()}"


        /*
         * profiles.avatar_url に
         * 実際に表示可能なURLを保存する。
         */
        updateAvatarUrl(
            userId = userId,
            avatarUrl = url
        )


        return url
    }


    // =========================================
    // 保存済みavatar_urlから
    // 表示可能URLを取得
    //
    // 過去に Storage path だけ保存した場合にも対応
    // =========================================

    fun getAvatarDisplayUrl(
        storedAvatarValue: String?
    ): String? {

        if (storedAvatarValue.isNullOrBlank()) {

            return null
        }


        /*
         * すでにURLならそのまま使用
         */
        if (
            storedAvatarValue.startsWith("https://") ||
            storedAvatarValue.startsWith("http://")
        ) {

            return storedAvatarValue
        }


        /*
         * 過去に
         *
         * UUID/profile.jpg
         *
         * のようなStorage pathを保存していた場合
         */
        val path =
            storedAvatarValue
                .substringBefore("?")
                .trimStart('/')


        val bucket =
            supabase
                .storage
                .from("avatars")


        val publicUrl =
            bucket.publicUrl(
                path
            )


        return "$publicUrl?v=${System.currentTimeMillis()}"
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
                userId = user.id,
                toiletName = toiletName,
                action = action
            )


        supabase
            .from("toilet_edit_history")
            .insert(
                history
            )
    }
}