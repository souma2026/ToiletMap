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
import kotlin.time.Duration.Companion.hours


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
    // プロフィール画像情報変更
    //
    // avatar_url というカラム名はそのまま使う。
    //
    // ただし、ここには今後
    //
    // UUID/profile.jpg
    //
    // というStorage上のパスを保存する。
    //
    // Signed URLそのものは保存しない。
    // =========================================

    private suspend fun updateAvatarUrl(
        userId: String,
        avatarPath: String
    ) {

        supabase
            .from("profiles")
            .update(
                {

                    set(
                        "avatar_url",
                        avatarPath
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
    // avatar_urlからStorageパスを取得
    //
    // 新方式:
    //
    // UUID/profile.jpg
    //
    //
    // 旧方式:
    //
    // https://xxxxx.supabase.co/
    // storage/v1/object/public/avatars/
    // UUID/profile.jpg?v=xxxx
    //
    //
    // どちらでも動くようにする
    // =========================================

    private fun extractAvatarPath(
        storedValue: String
    ): String {

        val withoutQuery =
            storedValue.substringBefore("?")

        val publicMarker =
            "/storage/v1/object/public/avatars/"

        val signedMarker =
            "/storage/v1/object/sign/avatars/"


        return when {

            publicMarker in withoutQuery -> {

                withoutQuery
                    .substringAfter(publicMarker)
            }


            signedMarker in withoutQuery -> {

                withoutQuery
                    .substringAfter(signedMarker)
            }


            else -> {

                withoutQuery
                    .trimStart('/')
            }
        }
    }


    // =========================================
    // キャッシュ対策
    // =========================================

    private fun addCacheBuster(
        url: String
    ): String {

        val separator =
            if (url.contains("?")) {
                "&"
            } else {
                "?"
            }


        return url +
                separator +
                "v=" +
                System.currentTimeMillis()
    }


    // =========================================
    // 表示用プロフィール画像URL取得
    //
    // Private Bucket
    // Public Bucket
    //
    // 両方になるべく対応する
    // =========================================

    suspend fun getAvatarDisplayUrl(
        storedAvatarValue: String?
    ): String? {

        if (storedAvatarValue.isNullOrBlank()) {

            return null
        }


        val path =
            extractAvatarPath(
                storedAvatarValue
            )


        if (path.isBlank()) {

            return null
        }


        val bucket =
            supabase
                .storage
                .from("avatars")


        /*
         * まずSigned URLを作成する。
         *
         * Private Bucketでも表示可能。
         *
         * URLは12時間だけ有効。
         *
         * URLそのものをDBには保存しないので、
         * 次回プロフィール取得時に
         * 新しいSigned URLを作ればよい。
         */

        return try {

            val signedUrl =
                bucket.createSignedUrl(
                    path = path,
                    expiresIn = 12.hours
                )


            addCacheBuster(
                signedUrl
            )


        } catch (e: Exception) {

            /*
             * Signed URL作成に失敗した場合、
             * Public Bucketの可能性を考えて
             * publicUrlへフォールバックする。
             */

            val publicUrl =
                bucket.publicUrl(
                    path
                )


            addCacheBuster(
                publicUrl
            )
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
         *
         *   ユーザーUUID/
         *
         *       profile.jpg
         */

        val path =
            "$userId/profile.jpg"


        /*
         * 同じユーザーが写真を変更した場合は
         * profile.jpgを上書きする
         */

        bucket.upload(
            path = path,
            data = imageBytes
        ) {

            upsert = true
        }


        /*
         * DBにはSigned URLではなく
         *
         * UUID/profile.jpg
         *
         * というパスだけ保存する。
         */

        updateAvatarUrl(
            userId = userId,
            avatarPath = path
        )


        return path
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