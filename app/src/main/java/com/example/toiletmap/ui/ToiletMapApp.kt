package com.example.toiletmap.ui

import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.toiletmap.model.CleaningRequest
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import com.example.toiletmap.model.ToiletReview
import com.example.toiletmap.screen.account.AccountScreen
import com.example.toiletmap.screen.add.AddToiletScreen
import com.example.toiletmap.screen.cleaning.CleaningScreen
import com.example.toiletmap.screen.game.SecretGameScreen
import com.example.toiletmap.screen.listofuncleaned.ListOfUncleanedScreen
import com.example.toiletmap.screen.listofuncleaned.UncleanedToilet
import com.example.toiletmap.screen.map.MapScreen
import com.example.toiletmap.screen.review.ReviewDialog
import com.example.toiletmap.ui.components.BottomNavigationBar
import com.example.toiletmap.viewmodel.GameViewModel
import org.maplibre.android.maps.MapView


@Composable
fun ToiletMapApp(

    /*
     * 隠しゲーム
     */
    gameViewModel:
    GameViewModel,


    /*
     * MapLibre
     */
    mapView:
    MapView,


    /*
     * 全トイレ
     *
     * Supabaseから取得した一覧。
     * 検索にも使用する。
     */
    toilets:
    List<Toilet>,


    /*
     * 現在選択中のトイレ
     */
    selectedToilet:
    Toilet?,


    /*
     * 清掃依頼中一覧
     */
    uncleanedToilets:
    List<UncleanedToilet>,


    /*
     * 清掃依頼・担当状態
     */
    cleaningRequests:
    List<CleaningRequest>,

    currentUserId:
    String?,

    isLoadingCleaning:
    Boolean,

    cleaningActionRequestId:
    String?,


    /*
     * 選択中トイレの口コミ状態
     */
    reviews:
    List<ToiletReview>,

    isLoadingReviews:
    Boolean,

    isPostingReview:
    Boolean,

    reviewErrorMessage:
    String?,

    reviewSuccessMessage:
    String?,


    /*
     * 未清掃一覧から地図を開く
     */
    onShowUncleanedToiletOnMap:
        (UncleanedToilet) -> Unit,


    /*
     * 検索結果を選択
     */
    onSearchToiletSelected:
        (Toilet) -> Unit,


    /*
     * 詳細を閉じる
     */
    onDismissSelectedToilet:
        () -> Unit,


    /*
     * 清掃依頼
     */
    onRequestCleaning:
        (Toilet) -> Unit,


    /*
     * 清掃を引き受ける
     */
    onAcceptCleaning:
        (CleaningRequest) -> Unit,


    /*
     * 清掃完了
     */
    onCompleteCleaning:
        (CleaningRequest) -> Unit,


    /*
     * 清掃担当をキャンセル
     */
    onCancelCleaning:
        (CleaningRequest) -> Unit,


    /*
     * 清掃依頼を再読込
     */
    onReloadCleaning:
        () -> Unit,


    /*
     * 清掃画面から対象トイレを地図で開く
     */
    onShowCleaningToiletOnMap:
        (String) -> Unit,


    /*
     * 口コミ一覧取得
     */
    onLoadReviews:
        (String) -> Unit,


    /*
     * 口コミ投稿
     */
    onSubmitReview:
        (
            String,
            Int,
            String
        ) -> Unit,


    /*
     * 口コミメッセージを消す
     */
    onClearReviewMessages:
        () -> Unit,


    /*
     * トイレ追加
     */
    onAddToilet:
        (Toilet) -> Unit,


    /*
     * 現在地
     */
    onCurrentLocationRequested:
        () -> Unit

) {


    /*
     * =====================================
     * 画面番号
     * =====================================
     *
     * 0 = 未清掃一覧
     * 1 = 清掃
     * 2 = Map
     * 3 = トイレ追加
     * 4 = アカウント
     */
    var selectedScreen by
    rememberSaveable {

        mutableIntStateOf(
            2
        )
    }


    /*
     * 地図からトイレ位置を選んでいる途中か
     */
    var isSelectingLocation by
    rememberSaveable {
        mutableStateOf(false)
    }


    /*
     * 口コミ投稿画面を表示しているか
     */
    var showReviewDialog by
    rememberSaveable {
        mutableStateOf(false)
    }


    /*
     * =====================================
     * 隠しゲーム起動判定
     * =====================================
     *
     * Map画面のWCロゴを2秒以内に5回連続タップすると起動。
     * ボトムナビにはゲーム項目を表示しない。
     */
    var showSecretGame by
    rememberSaveable {
        mutableStateOf(false)
    }


    var secretTapCount by
    rememberSaveable {
        mutableIntStateOf(0)
    }


    var lastSecretTapAtMillis by
    rememberSaveable {
        mutableStateOf(0L)
    }


    fun handleSecretLogoTap() {
        val now =
            SystemClock.elapsedRealtime()

        secretTapCount =
            if (
                lastSecretTapAtMillis == 0L ||
                now - lastSecretTapAtMillis > 2_000L
            ) {
                1
            } else {
                secretTapCount + 1
            }

        lastSecretTapAtMillis =
            now

        if (secretTapCount >= 5) {
            secretTapCount =
                0

            lastSecretTapAtMillis =
                0L

            showReviewDialog =
                false

            isSelectingLocation =
                false

            onClearReviewMessages()
            onDismissSelectedToilet()

            showSecretGame =
                true
        }
    }


    /*
     * 別画面へ移動した場合は連続タップ判定をリセット。
     */
    LaunchedEffect(
        selectedScreen
    ) {
        if (!showSecretGame) {
            secretTapCount =
                0

            lastSecretTapAtMillis =
                0L
        }
    }


    /*
     * ゲーム中はToiletMap本体とボトムナビを表示しない。
     */
    if (showSecretGame) {
        SecretGameScreen(
            viewModel =
                gameViewModel,

            onExit = {
                showSecretGame =
                    false

                secretTapCount =
                    0

                lastSecretTapAtMillis =
                    0L

                selectedScreen =
                    2
            }
        )

        return
    }


    /*
     * =====================================
     * Map画面を開いたら現在地を表示
     * =====================================
     *
     * 未清掃画面と同じように、画面表示時に
     * 自動で現在地取得を開始する。
     * Mapへ戻ってきたときも位置を更新する。
     */
    LaunchedEffect(
        selectedScreen
    ) {

        when (selectedScreen) {

            0,
            1 ->
                onReloadCleaning()

            2 -> {

                onReloadCleaning()
                onCurrentLocationRequested()
            }
        }
    }


    /*
     * =====================================
     * トイレ追加画面
     * =====================================
     */
    var toiletName by
    rememberSaveable {

        mutableStateOf(
            ""
        )
    }


    var cleanliness by
    rememberSaveable {

        mutableIntStateOf(
            3
        )
    }


    var comment by
    rememberSaveable {

        mutableStateOf(
            ""
        )
    }


    var selectedLatitude by
    rememberSaveable {

        mutableStateOf<Double?>(
            null
        )
    }


    var selectedLongitude by
    rememberSaveable {

        mutableStateOf<Double?>(
            null
        )
    }


    /*
     * 別のトイレを選んだ場合は、
     * 前のトイレの口コミ画面を閉じる。
     */
    LaunchedEffect(
        selectedToilet?.id
    ) {

        showReviewDialog =
            false

        onClearReviewMessages()
    }


    Scaffold(

        bottomBar = {

            BottomNavigationBar(

                selectedScreen =
                    selectedScreen,

                onScreenSelected = {
                        screen ->


                    /*
                     * Map以外へ移動したら
                     * 場所選択を終了
                     */
                    if (
                        screen != 2
                    ) {

                        isSelectingLocation =
                            false

                        showReviewDialog =
                            false

                        onClearReviewMessages()

                        onDismissSelectedToilet()
                    }


                    selectedScreen =
                        screen
                }
            )
        }

    ) {
            innerPadding ->


        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )

        ) {


            when (
                selectedScreen
            ) {


                /*
                 * =====================================
                 * 未清掃一覧
                 * =====================================
                 */
                0 -> {

                    ListOfUncleanedScreen(

                        toilets =
                            uncleanedToilets,

                        onShowOnMap = {
                                toilet ->


                            selectedScreen =
                                2

                            onShowUncleanedToiletOnMap(
                                toilet
                            )
                        }
                    )
                }


                /*
                 * =====================================
                 * 清掃
                 * =====================================
                 */
                1 -> {

                    CleaningScreen(

                        requests =
                            cleaningRequests,

                        toilets =
                            toilets,

                        currentUserId =
                            currentUserId,

                        isLoading =
                            isLoadingCleaning,

                        actionRequestId =
                            cleaningActionRequestId,

                        onRefresh =
                            onReloadCleaning,

                        onShowOnMap = {
                                request ->


                            selectedScreen =
                                2

                            onShowCleaningToiletOnMap(
                                request.toiletId
                            )
                        },

                        onCompleteCleaning =
                            onCompleteCleaning,

                        onCancelCleaning =
                            onCancelCleaning,

                        onOpenUncleaned = {

                            selectedScreen =
                                0
                        },

                        onOpenAccount = {

                            selectedScreen =
                                4
                        }
                    )
                }


                /*
                 * =====================================
                 * Map
                 * =====================================
                 */
                2 -> {

                    MapScreen(

                        mapView =
                            mapView,


                        /*
                         * 検索対象
                         */
                        toilets =
                            toilets,


                        /*
                         * 検索結果選択
                         */
                        onSearchToiletSelected =
                            onSearchToiletSelected,


                        /*
                         * WCロゴ5回タップで隠しゲーム
                         */
                        onSecretLogoTap = {
                            handleSecretLogoTap()
                        },


                        /*
                         * 現在地
                         */
                        onCurrentLocationClick =
                            onCurrentLocationRequested,


                        /*
                         * 場所選択中
                         */
                        isSelectingLocation =
                            isSelectingLocation,


                        /*
                         * 現在選択中のトイレ
                         */
                        selectedToilet =
                            selectedToilet,


                        /*
                         * 選択中トイレの有効な清掃依頼
                         */
                        cleaningRequest =
                            selectedToilet
                                ?.let { toilet ->

                                    cleaningRequests
                                        .firstOrNull { request ->

                                            request.toiletId == toilet.id &&
                                                    (
                                                            request.status == CleaningStatus.REQUESTED ||
                                                                    request.status == CleaningStatus.IN_PROGRESS
                                                            )
                                        }
                                },

                        currentUserId =
                            currentUserId,

                        cleaningActionRequestId =
                            cleaningActionRequestId,


                        /*
                         * 詳細を閉じる
                         */
                        onDismissSelectedToilet =
                            onDismissSelectedToilet,


                        /*
                         * 清掃依頼
                         */
                        onRequestCleaning =
                            onRequestCleaning,


                        /*
                         * 清掃を引き受ける
                         */
                        onAcceptCleaning =
                            onAcceptCleaning,


                        /*
                         * 自分の担当状況を開く
                         */
                        onOpenCleaningScreen = {

                            selectedScreen =
                                1
                        },


                        /*
                         * 未ログイン時はアカウント画面へ誘導
                         */
                        onOpenAccount = {

                            showReviewDialog =
                                false

                            onDismissSelectedToilet()

                            selectedScreen =
                                4
                        },


                        /*
                         * 口コミを投稿
                         */
                        onOpenReviews = {
                                toilet ->


                            onClearReviewMessages()

                            onLoadReviews(
                                toilet.id
                            )

                            showReviewDialog =
                                true
                        },


                        /*
                         * =====================================
                         * 地図をタップして位置選択
                         * =====================================
                         */
                        onLocationSelected = {
                                latitude,
                                longitude ->


                            selectedLatitude =
                                latitude

                            selectedLongitude =
                                longitude


                            isSelectingLocation =
                                false


                            /*
                             * トイレ追加画面へ戻る
                             */
                            selectedScreen =
                                3
                        },


                        /*
                         * 場所選択キャンセル
                         */
                        onCancelLocationSelection = {

                            isSelectingLocation =
                                false

                            selectedScreen =
                                3
                        }
                    )
                }


                /*
                 * =====================================
                 * トイレ追加
                 * =====================================
                 */
                3 -> {

                    AddToiletScreen(

                        toiletName =
                            toiletName,

                        cleanliness =
                            cleanliness,

                        comment =
                            comment,

                        latitude =
                            selectedLatitude,

                        longitude =
                            selectedLongitude,


                        /*
                         * 名前
                         */
                        onToiletNameChange = {

                            toiletName =
                                it
                        },


                        /*
                         * 清潔度
                         */
                        onCleanlinessChange = {

                            cleanliness =
                                it
                        },


                        /*
                         * コメント
                         */
                        onCommentChange = {

                            comment =
                                it
                        },


                        /*
                         * =====================================
                         * 地図から場所を選択
                         * =====================================
                         */
                        onSelectLocation = {

                            /*
                             * 既存詳細を閉じる
                             */
                            onDismissSelectedToilet()


                            isSelectingLocation =
                                true


                            /*
                             * Mapへ移動
                             */
                            selectedScreen =
                                2
                        },


                        /*
                         * =====================================
                         * 登録
                         * =====================================
                         */
                        onAddToilet = {


                            val latitude =
                                selectedLatitude


                            val longitude =
                                selectedLongitude


                            /*
                             * 場所が選択されている場合のみ
                             * 登録
                             */
                            if (
                                latitude != null &&
                                longitude != null
                            ) {


                                val toilet =

                                    Toilet(

                                        name =
                                            toiletName
                                                .trim(),

                                        latitude =
                                            latitude,

                                        longitude =
                                            longitude,

                                        cleanliness =
                                            cleanliness,

                                        comment =
                                            comment
                                                .trim()
                                    )


                                /*
                                 * MainActivityへ渡す
                                 */
                                onAddToilet(
                                    toilet
                                )


                                /*
                                 * 入力内容をリセット
                                 */
                                toiletName =
                                    ""

                                cleanliness =
                                    3

                                comment =
                                    ""

                                selectedLatitude =
                                    null

                                selectedLongitude =
                                    null


                                /*
                                 * Mapへ戻る
                                 */
                                selectedScreen =
                                    2
                            }
                        }
                    )
                }


                /*
                 * =====================================
                 * アカウント
                 * =====================================
                 */
                4 -> {

                    AccountScreen()
                }
            }
        }
    }


    val reviewTarget =
        selectedToilet


    if (
        showReviewDialog &&
        reviewTarget != null
    ) {

        ReviewDialog(

            toiletName =
                reviewTarget.name,

            reviews =
                reviews,

            isLoading =
                isLoadingReviews,

            isPosting =
                isPostingReview,

            errorMessage =
                reviewErrorMessage,

            successMessage =
                reviewSuccessMessage,

            onReload = {

                onLoadReviews(
                    reviewTarget.id
                )
            },

            onSubmit = {
                    rating,
                    reviewComment ->


                onSubmitReview(
                    reviewTarget.id,
                    rating,
                    reviewComment
                )
            },

            onDismiss = {

                showReviewDialog =
                    false

                onClearReviewMessages()
            }
        )
    }
}
