package com.example.toiletmap.screen.account

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.toiletmap.model.PointTransaction
import com.example.toiletmap.model.ToiletEditHistory
import com.example.toiletmap.model.UserProfile


class ProfileState {

    var profile by
    mutableStateOf<UserProfile?>(
        null
    )


    var localAvatarUri by
    mutableStateOf<Uri?>(
        null
    )


    var avatarDisplayUrl by
    mutableStateOf<String?>(
        null
    )


    /*
     * トイレ編集履歴
     */
    var history by
    mutableStateOf<List<ToiletEditHistory>>(
        emptyList()
    )


    /*
     * =========================================
     * ポイント履歴
     * =========================================
     *
     * origin/main側の変更を保持する。
     */
    var pointTransactions by
    mutableStateOf<List<PointTransaction>>(
        emptyList()
    )


    var editingName by
    mutableStateOf("")


    var editing by
    mutableStateOf(false)


    var showHistory by
    mutableStateOf(false)


    /*
     * ポイント履歴を表示しているか
     */
    var showPointHistory by
    mutableStateOf(false)


    var loading by
    mutableStateOf(true)


    var uploading by
    mutableStateOf(false)


    var message by
    mutableStateOf("")


    var showPointInfo by
    mutableStateOf(false)
}


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