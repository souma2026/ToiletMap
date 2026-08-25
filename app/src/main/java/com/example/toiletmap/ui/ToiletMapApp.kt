package com.example.toiletmap.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.toiletmap.model.Toilet
import com.example.toiletmap.model.ToiletReview
import com.example.toiletmap.screen.account.AccountScreen
import com.example.toiletmap.screen.add.AddToiletScreen
import com.example.toiletmap.screen.listofuncleaned.ListOfUncleanedScreen
import com.example.toiletmap.screen.listofuncleaned.UncleanedToilet
import com.example.toiletmap.screen.map.MapScreen
import com.example.toiletmap.screen.review.ReviewDialog
import com.example.toiletmap.ui.components.BottomNavigationBar
import org.maplibre.android.maps.MapView


@Composable
fun ToiletMapApp(

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
     * 清掃完了
     */
    onMarkCleaned:
        (Toilet) -> Unit,


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
     * 1 = 状態更新
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
     * 地図からトイレ位置を
     * 選んでいる途中か
     */
    var isSelectingLocation by
    rememberSaveable {

        mutableStateOf(
            false
        )
    }


    /*
     * 口コミ投稿画面を表示しているか
     */
    var showReviewDialog by
    rememberSaveable {

        mutableStateOf(
            false
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
                 * 状態更新
                 * =====================================
                 */
                1 -> {

                    StatusPlaceholderScreen()
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
                         * 清掃完了
                         */
                        onMarkCleaned =
                            onMarkCleaned,


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


/*
 * =====================================
 * 仮の状態更新画面
 * =====================================
 */
@Composable
private fun StatusPlaceholderScreen() {

    Box(

        modifier =
            Modifier
                .fillMaxSize(),

        contentAlignment =
            Alignment.Center

    ) {

        Text(

            text =
                "トイレのレビュー・状態更新\n\nこの画面は後で実装します",

            textAlign =
                TextAlign.Center,

            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )
    }
}