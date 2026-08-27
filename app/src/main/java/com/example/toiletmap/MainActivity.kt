package com.example.toiletmap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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

    private lateinit var mapController:
            MapLibreMapController

    private lateinit var toiletViewModel:
            ToiletViewModel

    private lateinit var reviewViewModel:
            ReviewViewModel

    private lateinit var cleaningViewModel:
            CleaningViewModel

    private lateinit var gameViewModel:
            GameViewModel


    /*
     * =====================================
     * 位置情報権限
     * =====================================
     */
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
                permissions ->


            val granted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||
                        permissions[
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ] == true


            if (
                granted
            ) {

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


    private var selectedToiletId by
    mutableStateOf<String?>(
        null
    )


    private var viewportLoadingStarted =
        false


    /*
     * =====================================
     * 初期位置取得タイムアウト
     * =====================================
     */
    private val mainHandler =
        Handler(
            Looper.getMainLooper()
        )


    private var initialLocationTimeoutRunnable:
            Runnable? =
        null


    private var initialLocationTimedOut =
        false


    private val initialLocationTimeoutMillis =
        4_000L


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


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


        mapController =
            MapLibreMapController(

                activity =
                    this,

                savedInstanceState =
                    savedInstanceState
            )


        mapController
            .setOnMapReadyListener {

                focusInitialMapLocation()
            }


        mapController
            .setOnToiletMarkerClickListener {
                    toilet ->


                selectedToiletId =
                    toilet.id
            }


        setContent {

            ToiletMapTheme {

                var cleaningCompleteMessage by
                remember {

                    mutableStateOf<String?>(
                        null
                    )
                }


                /*
                 * =====================================
                 * トイレ
                 * =====================================
                 */
                val toilets by
                toiletViewModel
                    .toilets
                    .collectAsState()


                val searchResults by
                toiletViewModel
                    .searchResults
                    .collectAsState()


                val isSearchingToilets by
                toiletViewModel
                    .isSearching
                    .collectAsState()


                val supplementalToilets by
                toiletViewModel
                    .supplementalToilets
                    .collectAsState()


                val selectedToilet by
                toiletViewModel
                    .selectedToilet
                    .collectAsState()


                val isAddingToilet by
                toiletViewModel
                    .isAdding
                    .collectAsState()


                val addedToilet by
                toiletViewModel
                    .addedToilet
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
                 * レビュー
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


                /*
                 * レビュー機能専用の
                 * ログイン中ユーザーID。
                 */
                val reviewCurrentUserId by
                reviewViewModel
                    .currentUserId
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
                 * 清掃
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
                    cleaningCompleteMessage !=
                    null
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
                 * UIで使用するトイレ一覧
                 * =====================================
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


                val cleaningRequestByToiletId =
                    cleaningRequests
                        .associateBy {

                            it.toiletId
                        }


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
                 * 選択トイレ変更
                 * =====================================
                 */
                LaunchedEffect(
                    selectedToiletId
                ) {

                    reviewViewModel
                        .prepareForToilet(
                            selectedToiletId
                        )


                    val toiletId =
                        selectedToiletId


                    if (
                        toiletId ==
                        null
                    ) {

                        toiletViewModel
                            .clearSelectedToilet()

                    } else {

                        toiletViewModel
                            .loadToiletDetail(
                                toiletId
                            )
                    }
                }


                /*
                 * =====================================
                 * 地図ピン更新
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
                 * 清掃依頼更新
                 * =====================================
                 */
                LaunchedEffect(
                    cleaningRequests
                ) {

                    toiletViewModel
                        .loadSupplementalToilets(

                            cleaningRequests
                                .map {

                                    it.toiletId
                                }
                        )


                    toiletViewModel
                        .loadToilets()


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

                    gameViewModel =
                        gameViewModel,

                    mapView =
                        mapController.mapView,

                    toilets =
                        knownToilets,

                    searchResults =
                        searchResults,

                    isSearchingToilets =
                        isSearchingToilets,

                    selectedToilet =
                        selectedToilet,

                    isAddingToilet =
                        isAddingToilet,

                    addedToilet =
                        addedToilet,

                    uncleanedToilets =
                        uncleanedToilets,

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

                    reviews =
                        reviews,

                    isLoadingReviews =
                        isLoadingReviews,

                    isPostingReview =
                        isPostingReview,

                    /*
                     * 今回追加
                     */
                    reviewCurrentUserId =
                        reviewCurrentUserId,

                    reviewErrorMessage =
                        reviewErrorMessage,

                    reviewSuccessMessage =
                        reviewSuccessMessage,


                    onShowUncleanedToiletOnMap = {
                            uncleanedToilet ->


                        val toilet =
                            knownToilets
                                .firstOrNull {

                                    it.id ==
                                            uncleanedToilet.id
                                }


                        if (
                            toilet !=
                            null
                        ) {

                            selectedToiletId =
                                toilet.id


                            mapController
                                .focusOnToilet(
                                    toilet
                                )
                        }
                    },


                    onSearchQueryChanged = {
                            query ->


                        toiletViewModel
                            .searchToilets(
                                query
                            )
                    },


                    onSearchToiletSelected = {
                            toilet ->


                        selectedToiletId =
                            toilet.id


                        mapController
                            .focusOnToilet(
                                toilet
                            )
                    },


                    onDismissSelectedToilet = {

                        selectedToiletId =
                            null
                    },


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


                    onAcceptCleaning = {
                            request ->


                        cleaningViewModel
                            .acceptCleaning(
                                request.id
                            )
                    },


                    onCompleteCleaning = {
                            request ->


                        cleaningViewModel
                            .completeCleaning(
                                request.id
                            )
                    },


                    onCancelCleaning = {
                            request ->


                        cleaningViewModel
                            .cancelCleaning(
                                request.id
                            )
                    },


                    onCancelCleaningRequest = {
                            request:
                            CleaningRequest ->


                        cleaningViewModel
                            .cancelCleaningRequest(
                                request.id
                            )
                    },


                    onReloadCleaning = {

                        cleaningViewModel
                            .loadRequests()
                    },


                    onShowCleaningToiletOnMap = {
                            toiletId ->


                        val toilet =
                            knownToilets
                                .firstOrNull {

                                    it.id ==
                                            toiletId
                                }


                        if (
                            toilet !=
                            null
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
                     * 口コミ取得
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


                    /*
                     * =====================================
                     * 口コミ削除
                     * =====================================
                     */
                    onDeleteReview = {
                            toiletId,
                            reviewId ->


                        reviewViewModel
                            .deleteReview(

                                toiletId =
                                    toiletId,

                                reviewId =
                                    reviewId
                            )
                    },


                    onClearReviewMessages = {

                        reviewViewModel
                            .clearMessages()
                    },


                    onAddToilet = {
                            toilet ->


                        toiletViewModel
                            .addToilet(
                                toilet
                            )
                    },


                    onAddSuccessHandled = {
                            toilet ->


                        selectedToiletId =
                            toilet.id


                        mapController
                            .focusOnToilet(
                                toilet
                            )


                        toiletViewModel
                            .consumeAddSuccess()
                    },


                    onCurrentLocationRequested = {

                        showCurrentLocationWithPermissionCheck()
                    }
                )
            }
        }
    }


    /*
     * =====================================
     * 初期地図位置
     * =====================================
     */
    private fun focusInitialMapLocation() {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED


        val coarseGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED


        if (
            fineGranted ||
            coarseGranted
        ) {

            initialLocationTimedOut =
                false


            cancelInitialLocationTimeout()


            val timeoutRunnable =
                Runnable {

                    initialLocationTimeoutRunnable =
                        null


                    initialLocationTimedOut =
                        true


                    mapController
                        .focusOnTokyoStation()


                    startViewportLoading()
                }


            initialLocationTimeoutRunnable =
                timeoutRunnable


            mainHandler
                .postDelayed(

                    timeoutRunnable,

                    initialLocationTimeoutMillis
                )


            mapController
                .showCurrentLocation(

                    onSuccess = {

                        cancelInitialLocationTimeout()


                        initialLocationTimedOut =
                            false


                        startViewportLoading()
                    },

                    onError = {

                        if (
                            initialLocationTimedOut
                        ) {

                            return@showCurrentLocation
                        }


                        cancelInitialLocationTimeout()


                        mapController
                            .focusOnTokyoStation()


                        startViewportLoading()
                    }
                )

        } else {

            cancelInitialLocationTimeout()


            mapController
                .focusOnTokyoStation()


            startViewportLoading()
        }
    }


    private fun cancelInitialLocationTimeout() {

        val runnable =
            initialLocationTimeoutRunnable
                ?: return


        mainHandler
            .removeCallbacks(
                runnable
            )


        initialLocationTimeoutRunnable =
            null
    }


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

                        south =
                            south,

                        north =
                            north,

                        west =
                            west,

                        east =
                            east
                    )
            }
    }


    /*
     * =====================================
     * 現在地ボタン
     * =====================================
     */
    private fun showCurrentLocationWithPermissionCheck() {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED


        val coarseGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED


        if (
            fineGranted ||
            coarseGranted
        ) {

            requestAndShowCurrentLocation()

        } else {

            locationPermissionLauncher
                .launch(

                    arrayOf(

                        Manifest.permission.ACCESS_FINE_LOCATION,

                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
        }
    }


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

                onError = {
                        message ->


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


    override fun onDestroy() {

        cancelInitialLocationTimeout()


        super.onDestroy()
    }


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


    override fun onLowMemory() {

        super.onLowMemory()


        mapController
            .onLowMemory()
    }
}