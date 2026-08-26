package com.example.toiletmap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.toiletmap.model.CleaningRequest
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.screen.listofuncleaned.UncleanedToilet
import com.example.toiletmap.screen.map.MapLibreMapController
import com.example.toiletmap.ui.ToiletMapApp
import com.example.toiletmap.ui.theme.ToiletMapTheme
import com.example.toiletmap.viewmodel.CleaningViewModel
import com.example.toiletmap.viewmodel.GameViewModel
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
     * CleaningViewModel
     * =====================================
     */
    private lateinit var cleaningViewModel:
            CleaningViewModel


    /*
     * =====================================
     * Secret Game ViewModel
     * =====================================
     */
    private lateinit var gameViewModel:
            GameViewModel


    /*
     * =====================================
     * 位置情報権限リクエスト
     * =====================================
     */
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {

                requestAndShowCurrentLocation()

            } else {

                mapController
                    .focusOnTokyoStation()


                Toast
                    .makeText(
                        this,
                        "位置情報を使用できないため東京駅を表示します",
                        Toast.LENGTH_LONG
                    )
                    .show()
            }
        }


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


    /*
     * =====================================
     * 地図範囲取得開始済みか
     * =====================================
     *
     * 初期位置が確定する前に東京駅周辺を取得し、
     * 直後に現在地周辺をもう一度取得する無駄を防ぐ。
     */
    private var viewportLoadingStarted =
        false


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


        cleaningViewModel =

            ViewModelProvider(this)[
                CleaningViewModel::class.java
            ]


        gameViewModel =

            ViewModelProvider(this)[
                GameViewModel::class.java
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
         * 地図の初期位置決定
         * =====================================
         *
         * 位置情報権限がすでにある:
         * → 現在地取得を試す
         *
         * 権限なし / 位置取得失敗:
         * → 東京駅
         *
         * 初期位置が確定してから、
         * 表示範囲のトイレ取得を開始する。
         */
        mapController
            .setOnMapReadyListener {

                focusInitialMapLocation()
            }


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
                 * 清掃完了ダイアログ
                 * =====================================
                 *
                 * 清掃完了時だけ、Toastではなく
                 * 画面中央に大きく表示する。
                 */
                var cleaningCompleteMessage by
                remember {
                    mutableStateOf<String?>(
                        null
                    )
                }


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


                /*
                 * 地図の表示範囲外でも、清掃依頼一覧などで
                 * 必要なトイレだけ別途保持する。
                 */
                val supplementalToilets by

                toiletViewModel
                    .supplementalToilets
                    .collectAsState()


                /*
                 * =====================================
                 * 選択中トイレの完全な詳細データ
                 * =====================================
                 *
                 * ピンや検索結果を選択した時だけ
                 * Supabaseから1件取得される。
                 */
                val selectedToilet by

                toiletViewModel
                    .selectedToilet
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
                 * 清掃依頼・担当状態
                 * =====================================
                 */
                val cleaningRequests by

                cleaningViewModel
                    .requests
                    .collectAsState()


                val currentUserId by

                cleaningViewModel
                    .currentUserId
                    .collectAsState()


                val requestPoints by

                cleaningViewModel
                    .requestPoints
                    .collectAsState()


                val isLoadingCleaning by

                cleaningViewModel
                    .isLoading
                    .collectAsState()


                val cleaningActionRequestId by

                cleaningViewModel
                    .actionRequestId
                    .collectAsState()


                val cleaningErrorMessage by

                cleaningViewModel
                    .errorMessage
                    .collectAsState()


                val cleaningSuccessMessage by

                cleaningViewModel
                    .successMessage
                    .collectAsState()


                LaunchedEffect(
                    cleaningErrorMessage,
                    cleaningSuccessMessage
                ) {

                    val error =
                        cleaningErrorMessage

                    val success =
                        cleaningSuccessMessage


                    /*
                     * エラーは従来どおりToast
                     */
                    if (
                        error != null
                    ) {

                        Toast
                            .makeText(
                                this@MainActivity,
                                error,
                                Toast.LENGTH_LONG
                            )
                            .show()

                        cleaningViewModel
                            .clearMessages()

                        return@LaunchedEffect
                    }


                    /*
                     * 清掃完了は大きなダイアログで表示
                     */
                    if (
                        success != null &&
                        (
                                success.contains(
                                    "清掃お疲れさまでした"
                                ) ||
                                        success.startsWith(
                                            "清掃完了"
                                        )
                                )
                    ) {

                        cleaningCompleteMessage =
                            success

                        cleaningViewModel
                            .clearMessages()

                        return@LaunchedEffect
                    }


                    /*
                     * 清掃依頼・引受・キャンセルなどは
                     * 従来どおりToast
                     */
                    if (
                        success != null
                    ) {

                        Toast
                            .makeText(
                                this@MainActivity,
                                success,
                                Toast.LENGTH_LONG
                            )
                            .show()

                        cleaningViewModel
                            .clearMessages()
                    }
                }


                /*
                 * =====================================
                 * 清掃完了ダイアログ
                 * =====================================
                 */
                if (
                    cleaningCompleteMessage != null
                ) {

                    AlertDialog(

                        onDismissRequest = {

                            cleaningCompleteMessage =
                                null
                        },

                        title = {

                            Text(
                                text =
                                    "清掃お疲れさまでした！",
                                modifier =
                                    Modifier.fillMaxWidth(),
                                fontSize =
                                    28.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                textAlign =
                                    TextAlign.Center
                            )
                        },

                        text = {

                            val originalMessage =
                                cleaningCompleteMessage
                                    .orEmpty()

                            val earnedPoints =
                                Regex(
                                    "(\\d+)\\s*pt"
                                )
                                    .find(
                                        originalMessage
                                    )
                                    ?.groupValues
                                    ?.getOrNull(
                                        1
                                    )

                            val rewardText =
                                if (
                                    earnedPoints != null
                                ) {

                                    "＋${earnedPoints}pt\n獲得しました"

                                } else {

                                    originalMessage
                                        .replace(
                                            "清掃お疲れさまでした！",
                                            ""
                                        )
                                        .replace(
                                            "清掃完了！",
                                            ""
                                        )
                                        .trim()
                                        .ifBlank {
                                            "清掃が完了しました"
                                        }
                                }

                            Text(
                                text =
                                    rewardText,
                                modifier =
                                    Modifier.fillMaxWidth(),
                                fontSize =
                                    30.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                textAlign =
                                    TextAlign.Center,
                                lineHeight =
                                    40.sp
                            )
                        },

                        confirmButton = {

                            Button(
                                onClick = {

                                    cleaningCompleteMessage =
                                        null
                                }
                            ) {

                                Text(
                                    text =
                                        "閉じる",
                                    fontSize =
                                        18.sp
                                )
                            }
                        }
                    )
                }


                /*
                 * =====================================
                 * UIで使用できるトイレ一覧
                 * =====================================
                 *
                 * 優先順位
                 *
                 * 1. 選択中の完全な詳細データ
                 * 2. 清掃用に取得した完全データ
                 * 3. 地図表示用の軽量データ
                 *
                 * 同じIDがあった場合は
                 * 上にある完全データを優先する。
                 */
                val knownToilets =
                    (
                            listOfNotNull(
                                selectedToilet
                            ) +

                                    supplementalToilets +

                                    toilets
                            )
                        .distinctBy {
                            it.id
                        }


                /*
                 * =====================================
                 * トイレごとの有効な清掃依頼
                 * =====================================
                 */
                val cleaningRequestByToiletId =

                    cleaningRequests
                        .associateBy {
                            it.toiletId
                        }


                /*
                 * =====================================
                 * 清掃待ちトイレ
                 * =====================================
                 */
                val uncleanedToilets =

                    knownToilets
                        .filter {
                                toilet ->


                            toilet.cleaningStatus ==
                                    CleaningStatus.REQUESTED
                        }
                        .map {
                                toilet ->


                            val request =
                                cleaningRequestByToiletId[
                                    toilet.id
                                ]


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
                                    request?.rewardPoints
                                        ?: toilet.cleaningRewardPoints
                            )
                        }


                /*
                 * =====================================
                 * 選択中トイレが変わった
                 * =====================================
                 */
                LaunchedEffect(
                    selectedToiletId
                ) {

                    /*
                     * 口コミ側の準備
                     */
                    reviewViewModel
                        .prepareForToilet(
                            selectedToiletId
                        )


                    val toiletId =
                        selectedToiletId


                    if (
                        toiletId == null
                    ) {

                        /*
                         * 詳細カードを閉じた
                         */
                        toiletViewModel
                            .clearSelectedToilet()

                    } else {

                        /*
                         * =====================================
                         * この1件だけSupabaseから詳細取得
                         * =====================================
                         */
                        toiletViewModel
                            .loadToiletDetail(
                                toiletId
                            )
                    }
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
                 * 清掃依頼が別端末で更新された場合も、
                 * toilets.cleaning_status を再取得して
                 * ピンと詳細表示を同期する。
                 */
                LaunchedEffect(
                    cleaningRequests
                ) {

                    /*
                     * =====================================
                     * 清掃対象だけ完全データ取得
                     * =====================================
                     */
                    toiletViewModel
                        .loadSupplementalToilets(

                            cleaningRequests
                                .map {
                                    it.toiletId
                                }
                        )

                    /*
                     * =====================================
                     * 現在表示範囲の軽量データ更新
                     * =====================================
                     */
                    toiletViewModel
                        .loadToilets()

                    /*
                     * =====================================
                     * 詳細カードを開いている場合
                     * その1件だけ再取得
                     * =====================================
                     */
                    selectedToiletId
                        ?.let {
                                toiletId ->

                            toiletViewModel
                                .loadToiletDetail(
                                    toiletId =
                                        toiletId,

                                    force =
                                        true
                                )
                        }
                }


                /*
                 * =====================================
                 * アプリ本体
                 * =====================================
                 */
                ToiletMapApp(

                    /*
                     * 隠しゲーム
                     */
                    gameViewModel =
                        gameViewModel,


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
                        knownToilets,


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
                     * 清掃依頼・担当状態
                     */
                    cleaningRequests =
                        cleaningRequests,

                    currentUserId =
                        currentUserId,

                    requestPoints =
                        requestPoints,

                    isLoadingCleaning =
                        isLoadingCleaning,

                    cleaningActionRequestId =
                        cleaningActionRequestId,


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
                            knownToilets
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
                            selectedRequestPoints ->


                        cleaningViewModel
                            .requestCleaning(
                                toiletId =
                                    toilet.id,

                                requestPoints =
                                    selectedRequestPoints
                            )
                    },


                    /*
                     * =====================================
                     * 清掃を引き受ける
                     * =====================================
                     */
                    onAcceptCleaning = {
                            request ->


                        cleaningViewModel
                            .acceptCleaning(
                                request.id
                            )
                    },


                    /*
                     * =====================================
                     * 清掃完了
                     * =====================================
                     */
                    onCompleteCleaning = {
                            request ->


                        cleaningViewModel
                            .completeCleaning(
                                request.id
                            )
                    },


                    /*
                     * =====================================
                     * 清掃担当をキャンセル
                     * =====================================
                     */
                    onCancelCleaning = {
                            request ->


                        cleaningViewModel
                            .cancelCleaning(
                                request.id
                            )
                    },


                    /*
                     * =====================================
                     * 自分が出した清掃依頼を取り消す
                     * =====================================
                     */
                    onCancelCleaningRequest = {
                            request: CleaningRequest ->

                        cleaningViewModel
                            .cancelCleaningRequest(
                                request.id
                            )
                    },


                    onReloadCleaning = {

                        cleaningViewModel
                            .loadRequests()
                    },


                    /*
                     * =====================================
                     * 清掃画面から地図で見る
                     * =====================================
                     */
                    onShowCleaningToiletOnMap = {
                            toiletId ->


                        val toilet =
                            knownToilets
                                .firstOrNull {
                                    it.id == toiletId
                                }


                        if (toilet != null) {

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
                     * 権限がある場合:
                     * → 現在位置を取得して地図へ表示
                     *
                     * 権限がない場合:
                     * → Androidの権限ダイアログを表示
                     */
                    onCurrentLocationRequested = {

                        showCurrentLocationWithPermissionCheck()
                    }
                )
            }
        }
    }


    /*
     * =====================================
     * 初期表示位置を決定
     * =====================================
     *
     * 起動時には位置情報の権限ダイアログを勝手に出さない。
     * すでに権限がある場合だけ現在地取得を試す。
     *
     * 権限がない、位置情報OFF、取得できない場合は
     * 東京駅を使用する。
     */
    private fun focusInitialMapLocation() {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


        if (
            fineGranted ||
            coarseGranted
        ) {

            mapController
                .showCurrentLocation(

                    onSuccess = {

                        startViewportLoading()
                    },

                    onError = {

                        mapController
                            .focusOnTokyoStation()

                        startViewportLoading()
                    }
                )

        } else {

            mapController
                .focusOnTokyoStation()

            startViewportLoading()
        }
    }


    /*
     * =====================================
     * 表示範囲に応じたトイレ取得を開始
     * =====================================
     */
    private fun startViewportLoading() {

        if (
            viewportLoadingStarted
        ) {
            return
        }


        viewportLoadingStarted =
            true


        mapController
            .setOnVisibleBoundsChangedListener {
                    south,
                    north,
                    west,
                    east ->

                toiletViewModel
                    .onVisibleBoundsChanged(
                        south = south,
                        north = north,
                        west = west,
                        east = east
                    )
            }
    }


    /*
     * =====================================
     * 現在地表示の権限確認
     * =====================================
     */
    private fun showCurrentLocationWithPermissionCheck() {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


        if (
            fineGranted ||
            coarseGranted
        ) {

            requestAndShowCurrentLocation()

        } else {

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    /*
     * =====================================
     * 現在位置を取得して地図へ表示
     * =====================================
     */
    private fun requestAndShowCurrentLocation() {

        mapController
            .showCurrentLocation(

                onSuccess = {

                    Toast
                        .makeText(
                            this,
                            "現在地を表示しました",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                },

                onError = { message ->

                    Toast
                        .makeText(
                            this,
                            message,
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
            )
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
