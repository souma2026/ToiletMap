package com.example.toiletmap.screen.account

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.toiletmap.data.repository.AccountRepository
import com.example.toiletmap.model.PointTransaction
import com.example.toiletmap.model.ToiletEditHistory
import com.example.toiletmap.model.UserProfile
import com.example.toiletmap.screen.account.components.HistorySection
import com.example.toiletmap.screen.account.components.PointCard
import com.example.toiletmap.screen.account.components.PointHistorySection
import com.example.toiletmap.screen.account.components.ProfileImageSection
import com.example.toiletmap.screen.account.components.ProfileInfoCard
import kotlinx.coroutines.launch


@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {

    val context =
        LocalContext.current


    val scope =
        rememberCoroutineScope()


    val currentUser =
        AccountRepository
            .getCurrentUser()


    if (currentUser == null) {

        LaunchedEffect(Unit) {

            onLogout()
        }

        return
    }


    val userId =
        currentUser.id


    // =========================================
    // State
    // =========================================

    var profile by remember {

        mutableStateOf<UserProfile?>(
            null
        )
    }


    /*
     * 写真を選択した直後に表示するURI。
     *
     * 画面移動後は消えてよい。
     * 次回はSupabaseに保存されたavatarUrlから
     * 復元する。
     */
    var localAvatarUri by remember {

        mutableStateOf<Uri?>(
            null
        )
    }


    /*
     * Supabaseから取得した
     * 実際に表示するプロフィール画像URL。
     */
    var avatarDisplayUrl by remember {

        mutableStateOf<String?>(
            null
        )
    }


    var history by remember {

        mutableStateOf<List<ToiletEditHistory>>(
            emptyList()
        )
    }


    var pointTransactions by remember {

        mutableStateOf<List<PointTransaction>>(
            emptyList()
        )
    }


    var editingName by remember {

        mutableStateOf("")
    }


    var editing by remember {

        mutableStateOf(false)
    }


    var showHistory by remember {

        mutableStateOf(false)
    }


    var showPointHistory by remember {

        mutableStateOf(false)
    }


    var loading by remember {

        mutableStateOf(true)
    }


    var uploading by remember {

        mutableStateOf(false)
    }


    var message by remember {

        mutableStateOf("")
    }


    // =========================================
    // プロフィール取得
    // =========================================

    suspend fun reloadProfile() {

        val loadedProfile =
            AccountRepository
                .loadProfile(
                    userId
                )


        profile =
            loadedProfile


        editingName =
            loadedProfile.username


        /*
         * ここが写真保持で重要。
         *
         * 画面移動して戻った場合でも、
         * profiles.avatar_urlから再取得する。
         */
        avatarDisplayUrl =
            AccountRepository
                .getAvatarDisplayUrl(
                    loadedProfile.avatarUrl
                )
    }


    // =========================================
    // 初回読み込み
    // =========================================

    LaunchedEffect(
        userId
    ) {

        try {

            /*
             * 第6段階:
             * アカウント画面を開いた時点で、
             * 本日の清掃依頼ポイントをSupabase側で更新する。
             *
             * 同じ日に何度呼んでも1回分しか処理されない。
             */
            AccountRepository
                .refreshDailyRequestPoints()


            /*
             * 更新後の残高を取得する。
             */
            reloadProfile()


            pointTransactions =
                AccountRepository
                    .loadPointTransactions(
                        userId
                    )


            history =
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


            message =
                "プロフィール取得に失敗しました"


        } finally {

            loading =
                false
        }
    }


    // =========================================
    // 写真選択
    // =========================================

    val photoPicker =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts
                    .PickVisualMedia()

        ) { uri: Uri? ->


            if (uri == null) {

                return@rememberLauncherForActivityResult
            }


            /*
             * 選択直後はローカル画像を表示
             */
            localAvatarUri =
                uri


            scope.launch {

                uploading =
                    true

                message =
                    ""


                try {

                    val bytes =
                        context
                            .contentResolver
                            .openInputStream(
                                uri
                            )
                            ?.use {

                                it.readBytes()
                            }


                    if (bytes == null) {

                        localAvatarUri =
                            null

                        message =
                            "写真を読み込めませんでした"

                        return@launch
                    }


                    /*
                     * Supabaseへアップロード
                     */
                    val newAvatarUrl =
                        AccountRepository
                            .uploadAvatar(
                                userId =
                                    userId,

                                imageBytes =
                                    bytes
                            )


                    /*
                     * 現在のprofileも更新
                     */
                    profile =
                        profile?.copy(
                            avatarUrl =
                                newAvatarUrl
                        )


                    /*
                     * Supabase側の表示URLも更新
                     */
                    avatarDisplayUrl =
                        newAvatarUrl


                    /*
                     * localAvatarUri は消さない。
                     *
                     * 現在の画面では端末側の写真を
                     * 確実に表示する。
                     *
                     * 一度別画面へ移動すると
                     * localAvatarUriは破棄され、
                     * avatarDisplayUrlが使われる。
                     */
                    message =
                        "写真を変更しました"


                    Log.d(
                        "AccountPhoto",
                        "Photo upload successful: $newAvatarUrl"
                    )


                } catch (e: Exception) {

                    Log.e(
                        "AccountPhoto",
                        "Photo upload failed",
                        e
                    )


                    localAvatarUri =
                        null


                    message =
                        "写真の変更に失敗しました"


                } finally {

                    uploading =
                        false
                }
            }
        }


    // =========================================
    // UI
    // =========================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .verticalScroll(
                rememberScrollState()
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {


        Text(
            text =
                "アカウント",

            style =
                MaterialTheme.typography.headlineMedium,

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 24.dp
                )
        )


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        if (loading) {

            CircularProgressIndicator()

        } else {


            // =====================================
            // プロフィール写真
            // =====================================

            ProfileImageSection(
                avatarModel =
                    localAvatarUri
                        ?: avatarDisplayUrl,

                uploading =
                    uploading,

                onChangePhoto = {

                    photoPicker.launch(

                        PickVisualMediaRequest(

                            ActivityResultContracts
                                .PickVisualMedia
                                .ImageOnly
                        )
                    )
                },

                modifier =
                    Modifier.fillMaxWidth()
            )


            Spacer(
                modifier =
                    Modifier.height(28.dp)
            )


            // =====================================
            // ユーザー情報
            // =====================================

            ProfileInfoCard(
                userName =
                    profile?.username
                        ?: "",

                email =
                    currentUser.email
                        ?: "",

                editing =
                    editing,

                editingName =
                    editingName,

                onEditingNameChange = {

                    editingName =
                        it
                },

                onStartEdit = {

                    editingName =
                        profile?.username
                            ?: ""

                    editing =
                        true
                },

                onSave = {

                    if (editingName.isBlank()) {

                        message =
                            "ユーザー名を入力してください"

                    } else {

                        scope.launch {

                            try {

                                val newName =
                                    editingName
                                        .trim()


                                AccountRepository
                                    .updateUserName(
                                        userId =
                                            userId,

                                        userName =
                                            newName
                                    )


                                profile =
                                    profile?.copy(
                                        username =
                                            newName
                                    )


                                editing =
                                    false


                                message =
                                    "ユーザー名を変更しました"


                            } catch (e: Exception) {

                                Log.e(
                                    "AccountProfile",
                                    "Username update failed",
                                    e
                                )


                                message =
                                    "ユーザー名変更に失敗しました"
                            }
                        }
                    }
                },

                onCancel = {

                    editingName =
                        profile?.username
                            ?: ""

                    editing =
                        false
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            /*
             * =====================================
             * 清掃依頼ポイント
             * =====================================
             *
             * 第6段階で追加。
             * 毎日、日本時間の0:00を基準に10ptまで回復し、
             * 清掃依頼1件につき3pt消費する。
             */
            PointCard(
                points =
                    profile?.requestPoints
                        ?: 0,

                title =
                    "清掃依頼ポイント",

                supportingText =
                    "毎日10ptまで回復・清掃依頼1件につき3pt消費",

                modifier =
                    Modifier.padding(
                        horizontal = 24.dp
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            /*
             * =====================================
             * 清掃報酬ポイント
             * =====================================
             *
             * 第5段階で追加。
             * 清掃完了時にSupabase RPCから安全に加算される。
             */
            PointCard(
                points =
                    profile?.rewardPoints
                        ?: 0,

                title =
                    "清掃報酬ポイント",

                supportingText =
                    "清掃を完了すると獲得できます",

                modifier =
                    Modifier.padding(
                        horizontal = 24.dp
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(28.dp)
            )


            // =====================================
            // ポイント履歴
            // =====================================

            PointHistorySection(
                transactions =
                    pointTransactions,

                showHistory =
                    showPointHistory,

                onToggleHistory = {

                    val newShowPointHistory =
                        !showPointHistory


                    showPointHistory =
                        newShowPointHistory


                    if (newShowPointHistory) {

                        scope.launch {

                            try {

                                pointTransactions =
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


                                message =
                                    "ポイント履歴の取得に失敗しました"
                            }
                        }
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(28.dp)
            )


            // =====================================
            // トイレ編集履歴
            // =====================================

            HistorySection(
                history =
                    history,

                showHistory =
                    showHistory,

                onToggleHistory = {

                    val newShowHistory =
                        !showHistory


                    showHistory =
                        newShowHistory


                    if (newShowHistory) {

                        scope.launch {

                            try {

                                history =
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


                                message =
                                    "履歴取得に失敗しました"
                            }
                        }
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp
                    )
            )


            // =====================================
            // メッセージ
            // =====================================

            if (message.isNotBlank()) {

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )


                Text(
                    text =
                        message,

                    modifier =
                        Modifier.padding(
                            horizontal = 24.dp
                        ),

                    color =
                        if (
                            "失敗" in message ||
                            "できません" in message
                        ) {

                            MaterialTheme.colorScheme.error

                        } else {

                            MaterialTheme.colorScheme.primary
                        }
                )
            }


            Spacer(
                modifier =
                    Modifier.height(30.dp)
            )


            // =====================================
            // ログアウト
            // =====================================

            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp
                    ),

                shape =
                    RoundedCornerShape(14.dp),

                onClick = {

                    scope.launch {

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


                            message =
                                "ログアウトに失敗しました"
                        }
                    }
                }
            ) {

                Text(
                    "ログアウト"
                )
            }


            Spacer(
                modifier =
                    Modifier.height(50.dp)
            )
        }
    }
}