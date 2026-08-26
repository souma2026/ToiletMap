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

            /*
             * =====================================
             * origin/main側の変更
             * =====================================
             *
             * 毎日の清掃依頼ポイントを更新する。
             *
             * Supabase側で同日重複実行を
             * 防止する想定。
             */
            AccountRepository
                .refreshDailyRequestPoints()


            /*
             * 更新後のプロフィールを取得
             */
            reloadProfile()


            /*
             * ポイント履歴
             */
            state.pointTransactions =
                AccountRepository
                    .loadPointTransactions(
                        userId
                    )


            /*
             * トイレ編集履歴
             */
            state.history =
                AccountRepository
                    .loadHistory(
                        userId
                    )


        } catch (e: Exception) {

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


        state.avatarDisplayUrl =
            AccountRepository
                .getAvatarDisplayUrl(
                    loadedProfile.avatarUrl
                )
    }


    /*
     * =========================================
     * ユーザー名
     * =========================================
     */
    fun changeEditingName(
        value: String
    ) {

        state.editingName =
            value
    }


    fun startEditingName() {

        state.editingName =
            state.profile
                ?.username
                ?: ""


        state.editing =
            true
    }


    fun cancelEditingName() {

        state.editingName =
            state.profile
                ?.username
                ?: ""


        state.editing =
            false
    }


    suspend fun saveUserName() {

        val newName =
            state.editingName
                .trim()


        if (newName.isBlank()) {

            state.message =
                "ユーザー名を入力してください"

            return
        }


        try {

            AccountRepository
                .updateUserName(
                    userId =
                        userId,

                    userName =
                        newName
                )


            state.profile =
                state.profile
                    ?.copy(
                        username =
                            newName
                    )


            state.editing =
                false


            state.message =
                "ユーザー名を変更しました"


        } catch (e: Exception) {

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
     * ポイント履歴
     * =========================================
     *
     * origin/main側の機能。
     */
    suspend fun togglePointHistory() {

        val newShowPointHistory =
            !state.showPointHistory


        state.showPointHistory =
            newShowPointHistory


        if (!newShowPointHistory) {

            return
        }


        try {

            state.pointTransactions =
                AccountRepository
                    .loadPointTransactions(
                        userId
                    )


        } catch (e: Exception) {

            Log.e(
                "AccountPointHistory",
                "Point history load failed",
                e
            )


            state.message =
                "ポイント履歴の取得に失敗しました"
        }
    }


    /*
     * =========================================
     * トイレ編集履歴
     * =========================================
     */
    suspend fun toggleHistory() {

        val newShowHistory =
            !state.showHistory


        state.showHistory =
            newShowHistory


        if (!newShowHistory) {

            return
        }


        try {

            state.history =
                AccountRepository
                    .loadHistory(
                        userId
                    )


        } catch (e: Exception) {

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
     * プロフィール画像
     * =========================================
     */
    suspend fun changeAvatar(

        context: Context,

        uri: Uri
    ) {

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
                        context =
                            context,

                        userId =
                            userId,

                        uri =
                            uri
                    )


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


        } catch (e: ProfileAvatarReadException) {

            state.localAvatarUri =
                null


            state.message =
                "写真を読み込めませんでした"


        } catch (e: Exception) {

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
     * ポイント説明
     * =========================================
     */
    fun openPointInfo() {

        state.showPointInfo =
            true
    }


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


        } catch (e: Exception) {

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