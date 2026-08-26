package com.example.toiletmap.screen.account

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.toiletmap.model.ToiletEditHistory
import com.example.toiletmap.model.UserProfile


class ProfileState {

    /*
     * Supabaseから取得したプロフィール
     */
    var profile by mutableStateOf<UserProfile?>(
        null
    )


    /*
     * 写真選択直後に使う端末側URI
     */
    var localAvatarUri by mutableStateOf<Uri?>(
        null
    )


    /*
     * Supabaseに保存された画像URL
     */
    var avatarDisplayUrl by mutableStateOf<String?>(
        null
    )


    /*
     * トイレ編集履歴
     */
    var history by mutableStateOf<List<ToiletEditHistory>>(
        emptyList()
    )


    /*
     * 編集中のユーザー名
     */
    var editingName by mutableStateOf("")


    /*
     * ユーザー名編集状態
     */
    var editing by mutableStateOf(false)


    /*
     * 履歴表示状態
     */
    var showHistory by mutableStateOf(false)


    /*
     * プロフィール読み込み中
     */
    var loading by mutableStateOf(true)


    /*
     * プロフィール画像アップロード中
     */
    var uploading by mutableStateOf(false)


    /*
     * 成功・エラーメッセージ
     */
    var message by mutableStateOf("")


    /*
     * ポイント説明ダイアログ
     */
    var showPointInfo by mutableStateOf(false)
}


/*
 * ユーザーごとにProfileStateを保持する
 */
@Composable
fun rememberProfileState(
    userId: String
): ProfileState {

    return remember(
        userId
    ) {
        ProfileState()
    }
}