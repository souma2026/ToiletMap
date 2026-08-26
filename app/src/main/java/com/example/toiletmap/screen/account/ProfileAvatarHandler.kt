package com.example.toiletmap.screen.account

import android.content.Context
import android.net.Uri
import com.example.toiletmap.data.repository.AccountRepository


/*
 * 画像ファイルを読み込めなかった場合専用
 */
class ProfileAvatarReadException : Exception()


object ProfileAvatarHandler {

    /*
     * =========================================
     * プロフィール画像アップロード
     * =========================================
     */
    suspend fun uploadAvatar(
        context: Context,
        userId: String,
        uri: Uri
    ): String {

        /*
         * URIから画像データを読み込む
         */
        val imageBytes =
            context
                .contentResolver
                .openInputStream(
                    uri
                )
                ?.use {
                    it.readBytes()
                }
                ?: throw ProfileAvatarReadException()


        /*
         * Supabase Storageへアップロード
         *
         * AccountRepository側で
         * profiles.avatar_url の更新も行う
         */
        return AccountRepository
            .uploadAvatar(
                userId = userId,
                imageBytes = imageBytes
            )
    }
}