package com.example.toiletmap.screen.account

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.toiletmap.data.repository.AccountRepository


class ProfileActions(

    private val userId: String,

    private val state: ProfileState
) {

    /*
     * =========================================
     * 初回読み込み
     * =========================================
     */
    suspend fun loadInitialData() {

        state.loading =
            true


        try {

            reloadProfile()


            state.history =
                AccountRepository
                    .loadHistory(
                        userId
                    )


        } catch (
            e: Exception
        ) {

            Log.e(
                "AccountProfile",
                "Profile load failed",
                e
            )


            state.message =
                "プロフィール取得に失敗しました"


        } finally {

            state.loading =
                false
        }
    }


    /*
     * =========================================
     * プロフィール再取得
     * =========================================
     */
    private suspend fun reloadProfile() {

        val loadedProfile =
            AccountRepository
                .loadProfile(
                    userId
                )


        state.profile =
            loadedProfile


        state.editingName =
            loadedProfile.username


        /*
         * 現在の実装と同じ方法で
         * プロフィール画像URLを取得
         */
        state.avatarDisplayUrl =
            AccountRepository
                .getAvatarDisplayUrl(
                    loadedProfile.avatarUrl
                )
    }


    /*
     * =========================================
     * ユーザー名入力
     * =========================================
     */
    fun changeEditingName(
        value: String
    ) {

        state.editingName =
            value
    }


    /*
     * =========================================
     * ユーザー名編集開始
     * =========================================
     */
    fun startEditingName() {

        state.editingName =
            state.profile
                ?.username
                ?: ""


        state.editing =
            true
    }


    /*
     * =========================================
     * ユーザー名編集キャンセル
     * =========================================
     */
    fun cancelEditingName() {

        state.editingName =
            state.profile
                ?.username
                ?: ""


        state.editing =
            false
    }


    /*
     * =========================================
     * ユーザー名保存
     * =========================================
     */
    suspend fun saveUserName() {

        val newName =
            state.editingName
                .trim()


        if (
            newName.isBlank()
        ) {

            state.message =
                "ユーザー名を入力してください"

            return
        }


        try {

            AccountRepository
                .updateUserName(
                    userId = userId,
                    userName = newName
                )


            state.profile =
                state.profile
                    ?.copy(
                        username = newName
                    )


            state.editing =
                false


            state.message =
                "ユーザー名を変更しました"


        } catch (
            e: Exception
        ) {

            Log.e(
                "AccountProfile",
                "Username update failed",
                e
            )


            state.message =
                "ユーザー名変更に失敗しました"
        }
    }


    /*
     * =========================================
     * 履歴表示切り替え
     * =========================================
     */
    suspend fun toggleHistory() {

        val newShowHistory =
            !state.showHistory


        state.showHistory =
            newShowHistory


        /*
         * 閉じるだけなら
         * Supabaseへ問い合わせない
         */
        if (
            !newShowHistory
        ) {

            return
        }


        /*
         * 開くときは最新履歴を取得
         */
        try {

            state.history =
                AccountRepository
                    .loadHistory(
                        userId
                    )


        } catch (
            e: Exception
        ) {

            Log.e(
                "AccountHistory",
                "History load failed",
                e
            )


            state.message =
                "履歴取得に失敗しました"
        }
    }


    /*
     * =========================================
     * プロフィール画像変更
     * =========================================
     */
    suspend fun changeAvatar(
        context: Context,
        uri: Uri
    ) {

        /*
         * 選んだ瞬間に画面へ表示
         */
        state.localAvatarUri =
            uri


        state.uploading =
            true


        state.message =
            ""


        try {

            val newAvatarUrl =
                ProfileAvatarHandler
                    .uploadAvatar(
                        context = context,
                        userId = userId,
                        uri = uri
                    )


            /*
             * ProfileStateも更新
             */
            state.profile =
                state.profile
                    ?.copy(
                        avatarUrl =
                            newAvatarUrl
                    )


            state.avatarDisplayUrl =
                newAvatarUrl


            state.message =
                "写真を変更しました"


            Log.d(
                "AccountPhoto",
                "Photo upload successful: $newAvatarUrl"
            )


        } catch (
            e: ProfileAvatarReadException
        ) {

            state.localAvatarUri =
                null


            state.message =
                "写真を読み込めませんでした"


        } catch (
            e: Exception
        ) {

            Log.e(
                "AccountPhoto",
                "Photo upload failed",
                e
            )


            state.localAvatarUri =
                null


            state.message =
                "写真の変更に失敗しました"


        } finally {

            state.uploading =
                false
        }
    }


    /*
     * =========================================
     * ポイント説明を開く
     * =========================================
     */
    fun openPointInfo() {

        state.showPointInfo =
            true
    }


    /*
     * =========================================
     * ポイント説明を閉じる
     * =========================================
     */
    fun closePointInfo() {

        state.showPointInfo =
            false
    }


    /*
     * =========================================
     * ログアウト
     * =========================================
     */
    suspend fun logout(
        onLogout: () -> Unit
    ) {

        try {

            AccountRepository
                .signOut()


            onLogout()


        } catch (
            e: Exception
        ) {

            Log.e(
                "AccountAuth",
                "Logout failed",
                e
            )


            state.message =
                "ログアウトに失敗しました"
        }
    }
}