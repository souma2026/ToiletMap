package com.example.toiletmap.screen.account

import android.net.Uri
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.toiletmap.data.repository.AccountRepository
import com.example.toiletmap.screen.account.components.HistorySection
import com.example.toiletmap.screen.account.components.PointHistorySection
import com.example.toiletmap.screen.account.components.PointInfoDialog
import com.example.toiletmap.screen.account.components.ProfileImageSection
import com.example.toiletmap.screen.account.components.ProfileInfoCard
import com.example.toiletmap.screen.account.components.ProfileLogoutButton
import com.example.toiletmap.screen.account.components.ProfileMessage
import com.example.toiletmap.screen.account.components.ProfilePointSection
import kotlinx.coroutines.launch


@Composable
fun ProfileScreen(

    onLogout: () -> Unit,

    /*
     * nakazono側
     * ポイント交換画面へ移動
     */
    onOpenPointExchange: () -> Unit,

    /*
     * nakazono側
     * ポイント交換履歴へ移動
     */
    onOpenPointExchangeHistory: () -> Unit
) {

    val context =
        LocalContext.current


    val scope =
        rememberCoroutineScope()


    val currentUser =
        AccountRepository
            .getCurrentUser()


    /*
     * =========================================
     * 未ログイン
     * =========================================
     */
    if (currentUser == null) {

        LaunchedEffect(Unit) {

            onLogout()
        }

        return
    }


    val userId =
        currentUser.id


    /*
     * =========================================
     * State
     * =========================================
     */
    val state =
        rememberProfileState(
            userId
        )


    val actions =
        remember(
            userId,
            state
        ) {

            ProfileActions(
                userId =
                    userId,

                state =
                    state
            )
        }


    /*
     * =========================================
     * 初回読み込み
     * =========================================
     *
     * ProfileActions側で
     *
     * ・毎日の清掃依頼ポイント更新
     * ・プロフィール
     * ・ポイント履歴
     * ・トイレ編集履歴
     *
     * を取得する。
     */
    LaunchedEffect(
        userId
    ) {

        actions
            .loadInitialData()
    }


    /*
     * =========================================
     * プロフィール写真選択
     * =========================================
     */
    val photoPicker =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts
                    .PickVisualMedia()

        ) { uri: Uri? ->


            if (uri != null) {

                scope.launch {

                    actions
                        .changeAvatar(
                            context =
                                context,

                            uri =
                                uri
                        )
                }
            }
        }


    /*
     * =========================================
     * UI
     * =========================================
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme
                    .colorScheme
                    .background
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
                MaterialTheme
                    .typography
                    .headlineMedium,

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
                Modifier.height(
                    30.dp
                )
        )


        /*
         * =====================================
         * 読み込み
         * =====================================
         */
        if (state.loading) {

            CircularProgressIndicator()

        } else {


            /*
             * =====================================
             * プロフィール画像
             * =====================================
             */
            ProfileImageSection(

                avatarModel =
                    state.localAvatarUri
                        ?: state.avatarDisplayUrl,

                uploading =
                    state.uploading,

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
                    Modifier.height(
                        28.dp
                    )
            )


            /*
             * =====================================
             * ユーザー情報
             * =====================================
             */
            ProfileInfoCard(

                userName =
                    state.profile
                        ?.username
                        ?: "",

                email =
                    currentUser.email
                        ?: "",

                editing =
                    state.editing,

                editingName =
                    state.editingName,

                onEditingNameChange =
                    actions::changeEditingName,

                onStartEdit =
                    actions::startEditingName,

                onSave = {

                    scope.launch {

                        actions
                            .saveUserName()
                    }
                },

                onCancel =
                    actions::cancelEditingName,

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            24.dp
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
                    )
            )


            /*
             * =====================================
             * ポイント
             * =====================================
             *
             * main側の変更に合わせ、
             * 清掃依頼ポイントは
             * requestPointsを使用する。
             *
             * nakazono側の
             * ポイント交換・交換履歴ボタンも残す。
             */
            ProfilePointSection(

                requestPoints =
                    state.profile
                        ?.requestPoints
                        ?: 0,

                rewardPoints =
                    state.profile
                        ?.rewardPoints
                        ?: 0,

                onInfoClick =
                    actions::openPointInfo,

                onOpenPointExchange =
                    onOpenPointExchange,

                onOpenPointExchangeHistory =
                    onOpenPointExchangeHistory,

                modifier =
                    Modifier.padding(
                        horizontal =
                            24.dp
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(
                        28.dp
                    )
            )


            /*
             * =====================================
             * ポイント履歴
             * =====================================
             *
             * origin/main側の機能。
             */
            PointHistorySection(

                transactions =
                    state.pointTransactions,

                showHistory =
                    state.showPointHistory,

                onToggleHistory = {

                    scope.launch {

                        actions
                            .togglePointHistory()
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            24.dp
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(
                        28.dp
                    )
            )


            /*
             * =====================================
             * トイレ編集履歴
             * =====================================
             */
            HistorySection(

                history =
                    state.history,

                showHistory =
                    state.showHistory,

                onToggleHistory = {

                    scope.launch {

                        actions
                            .toggleHistory()
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            24.dp
                    )
            )


            /*
             * =====================================
             * メッセージ
             * =====================================
             */
            if (
                state.message.isNotBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            20.dp
                        )
                )


                ProfileMessage(
                    message =
                        state.message
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        30.dp
                    )
            )


            /*
             * =====================================
             * ログアウト
             * =====================================
             */
            ProfileLogoutButton(

                onClick = {

                    scope.launch {

                        actions
                            .logout(
                                onLogout
                            )
                    }
                }
            )


            Spacer(
                modifier =
                    Modifier.height(
                        50.dp
                    )
            )
        }
    }


    /*
     * =========================================
     * ポイント説明
     * =========================================
     */
    PointInfoDialog(

        visible =
            state.showPointInfo,

        onDismiss =
            actions::closePointInfo
    )
}