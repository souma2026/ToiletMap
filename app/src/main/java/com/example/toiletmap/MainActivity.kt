package com.example.toiletmap

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.screen.listofuncleaned.UncleanedToilet
import com.example.toiletmap.screen.map.MapLibreMapController
import com.example.toiletmap.ui.ToiletMapApp
import com.example.toiletmap.ui.theme.ToiletMapTheme
import com.example.toiletmap.viewmodel.ReviewViewModel
import com.example.toiletmap.viewmodel.ToiletViewModel


class MainActivity : ComponentActivity() {

    /*
     * =====================================
     * MapLibre
     * =====================================
     */
    private lateinit var mapController:
            MapLibreMapController


    /*
     * =====================================
     * ToiletViewModel
     * =====================================
     */
    private lateinit var toiletViewModel:
            ToiletViewModel


    /*
     * =====================================
     * ReviewViewModel
     * =====================================
     */
    private lateinit var reviewViewModel:
            ReviewViewModel


    /*
     * =====================================
     * 現在選択中のトイレID
     * =====================================
     *
     * ・地図のピンを押した
     * ・検索結果を押した
     *
     * どちらの場合もここを書き換える。
     */
    private var selectedToiletId by
    mutableStateOf<String?>(
        null
    )


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        /*
         * =====================================
         * ViewModel
         * =====================================
         */
        toiletViewModel =

            ViewModelProvider(this)[
                ToiletViewModel::class.java
            ]


        reviewViewModel =

            ViewModelProvider(this)[
                ReviewViewModel::class.java
            ]


        /*
         * =====================================
         * MapLibre
         * =====================================
         */
        mapController =

            MapLibreMapController(

                activity =
                    this,

                savedInstanceState =
                    savedInstanceState
            )


        /*
         * =====================================
         * 地図上のトイレピンを押した
         * =====================================
         */
        mapController
            .setOnToiletMarkerClickListener {
                    toilet ->


                selectedToiletId =
                    toilet.id
            }


        /*
         * =====================================
         * Compose
         * =====================================
         */
        setContent {

            ToiletMapTheme {


                /*
                 * =====================================
                 * Supabaseから取得した
                 * トイレ一覧
                 * =====================================
                 */
                val toilets by

                toiletViewModel
                    .toilets
                    .collectAsState()


                val errorMessage by

                toiletViewModel
                    .errorMessage
                    .collectAsState()


                LaunchedEffect(
                    errorMessage
                ) {

                    val message =
                        errorMessage
                            ?: return@LaunchedEffect


                    Toast
                        .makeText(
                            this@MainActivity,
                            message,
                            Toast.LENGTH_LONG
                        )
                        .show()


                    toiletViewModel
                        .clearErrorMessage()
                }


                /*
                 * =====================================
                 * 選択中トイレの口コミ状態
                 * =====================================
                 */
                val reviews by

                reviewViewModel
                    .reviews
                    .collectAsState()


                val isLoadingReviews by

                reviewViewModel
                    .isLoading
                    .collectAsState()


                val isPostingReview by

                reviewViewModel
                    .isPosting
                    .collectAsState()


                val reviewErrorMessage by

                reviewViewModel
                    .errorMessage
                    .collectAsState()


                val reviewSuccessMessage by

                reviewViewModel
                    .successMessage
                    .collectAsState()


                /*
                 * =====================================
                 * 現在選択中のトイレ
                 * =====================================
                 */
                val selectedToilet =

                    toilets
                        .firstOrNull {
                                toilet ->


                            toilet.id ==
                                    selectedToiletId
                        }


                /*
                 * =====================================
                 * 清掃待ちトイレ
                 * =====================================
                 */
                val uncleanedToilets =

                    toilets
                        .filter {
                                toilet ->


                            toilet.cleaningStatus ==
                                    CleaningStatus.REQUESTED
                        }
                        .map {
                                toilet ->


                            UncleanedToilet(

                                id =
                                    toilet.id,

                                name =
                                    toilet.name,

                                latitude =
                                    toilet.latitude,

                                longitude =
                                    toilet.longitude,

                                lastCleanedAtMillis =
                                    toilet.lastCleanedAtMillis,

                                rewardPoints =
                                    toilet.cleaningRewardPoints
                            )
                        }


                /*
                 * =====================================
                 * 選択中トイレが変わったら
                 * 以前の口コミ状態をリセット
                 * =====================================
                 */
                LaunchedEffect(
                    selectedToiletId
                ) {

                    reviewViewModel
                        .prepareForToilet(
                            selectedToiletId
                        )
                }


                /*
                 * =====================================
                 * トイレ一覧が更新されたら
                 * 地図上のピンを更新
                 * =====================================
                 */
                LaunchedEffect(
                    toilets
                ) {

                    mapController
                        .showToilets(
                            toilets
                        )
                }


                /*
                 * =====================================
                 * アプリ本体
                 * =====================================
                 */
                ToiletMapApp(

                    /*
                     * MapLibre
                     */
                    mapView =
                        mapController.mapView,


                    /*
                     * =====================================
                     * 検索対象
                     * =====================================
                     */
                    toilets =
                        toilets,


                    /*
                     * 現在選択中
                     */
                    selectedToilet =
                        selectedToilet,


                    /*
                     * 清掃待ち一覧
                     */
                    uncleanedToilets =
                        uncleanedToilets,


                    /*
                     * 口コミ状態
                     */
                    reviews =
                        reviews,

                    isLoadingReviews =
                        isLoadingReviews,

                    isPostingReview =
                        isPostingReview,

                    reviewErrorMessage =
                        reviewErrorMessage,

                    reviewSuccessMessage =
                        reviewSuccessMessage,


                    /*
                     * =====================================
                     * 未清掃一覧から地図で見る
                     * =====================================
                     */
                    onShowUncleanedToiletOnMap = {
                            uncleanedToilet ->


                        val toilet =
                            toilets
                                .firstOrNull {
                                    it.id ==
                                            uncleanedToilet.id
                                }


                        if (
                            toilet != null
                        ) {

                            selectedToiletId =
                                toilet.id

                            mapController
                                .focusOnToilet(
                                    toilet
                                )
                        }
                    },


                    /*
                     * =====================================
                     * 検索結果を押した
                     * =====================================
                     */
                    onSearchToiletSelected = {
                            toilet ->


                        /*
                         * 選択中にする
                         */
                        selectedToiletId =
                            toilet.id


                        /*
                         * そのトイレへ地図移動
                         */
                        mapController
                            .focusOnToilet(
                                toilet
                            )
                    },


                    /*
                     * =====================================
                     * 詳細を閉じる
                     * =====================================
                     */
                    onDismissSelectedToilet = {


                        selectedToiletId =
                            null
                    },


                    /*
                     * =====================================
                     * 清掃依頼
                     * =====================================
                     */
                    onRequestCleaning = {
                            toilet,
                            rewardPoints ->


                        toiletViewModel
                            .requestCleaning(
                                toiletId = toilet.id,
                                rewardPoints = rewardPoints
                            )
                    },


                    /*
                     * =====================================
                     * 清掃完了
                     * =====================================
                     */
                    onMarkCleaned = {
                            toilet ->


                        toiletViewModel
                            .markCleaned(
                                toilet.id
                            )
                    },


                    /*
                     * =====================================
                     * 口コミを開く・再読込
                     * =====================================
                     */
                    onLoadReviews = {
                            toiletId ->


                        reviewViewModel
                            .loadReviews(
                                toiletId
                            )
                    },


                    /*
                     * =====================================
                     * 口コミ投稿
                     * =====================================
                     */
                    onSubmitReview = {
                            toiletId,
                            rating,
                            comment ->


                        reviewViewModel
                            .addReview(
                                toiletId =
                                    toiletId,

                                rating =
                                    rating,

                                comment =
                                    comment
                            )
                    },


                    onClearReviewMessages = {

                        reviewViewModel
                            .clearMessages()
                    },


                    /*
                     * =====================================
                     * トイレ追加
                     * =====================================
                     */
                    onAddToilet = {
                            toilet ->


                        toiletViewModel
                            .addToilet(
                                toilet
                            )


                        /*
                         * 登録したトイレを選択
                         */
                        selectedToiletId =
                            toilet.id


                        /*
                         * 登録場所へ移動
                         */
                        mapController
                            .focusOnToilet(
                                toilet
                            )
                    },


                    /*
                     * =====================================
                     * 現在地ボタン
                     * =====================================
                     *
                     * 現在は一旦無効。
                     *
                     * 検索機能を安定させた後に
                     * MapLibreMapControllerと合わせて
                     * 再度実装する。
                     */
                    onCurrentLocationRequested = {

                        // 現在地機能は一旦無効
                    }
                )
            }
        }
    }


    /*
     * =====================================
     * MapView状態保存
     * =====================================
     */
    override fun onSaveInstanceState(
        outState: Bundle
    ) {

        mapController
            .onSaveInstanceState(
                outState
            )


        super.onSaveInstanceState(
            outState
        )
    }


    /*
     * =====================================
     * メモリ不足
     * =====================================
     */
    override fun onLowMemory() {

        super.onLowMemory()


        mapController
            .onLowMemory()
    }
}