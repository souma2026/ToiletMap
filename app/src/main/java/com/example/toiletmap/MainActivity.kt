package com.example.toiletmap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.screen.listofuncleaned.UncleanedToilet
import com.example.toiletmap.screen.map.DeviceLocationStatus
import com.example.toiletmap.screen.map.MapLibreMapController
import com.example.toiletmap.ui.ToiletMapApp
import com.example.toiletmap.ui.theme.ToiletMapTheme
import com.example.toiletmap.viewmodel.CleaningViewModel
import com.example.toiletmap.viewmodel.ReviewViewModel
import com.example.toiletmap.viewmodel.ToiletViewModel
import kotlinx.coroutines.delay


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
     * 端末の現在地取得状態
     * =====================================
     */
    private var deviceLocationStatus by
    mutableStateOf(
        DeviceLocationStatus.CHECKING
    )

    private val locationCheckStartedAtMillis =
        System.currentTimeMillis()

    private var focusCurrentLocationAfterPermission =
        false


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

            if (
                granted &&
                ::mapController.isInitialized
            ) {
                mapController.enableUserLocation(
                    focus = focusCurrentLocationAfterPermission
                )
            } else if (!granted) {
                Toast
                    .makeText(
                        this,
                        "現在地を表示するには位置情報の許可が必要です",
                        Toast.LENGTH_LONG
                    )
                    .show()
            }

            refreshDeviceLocationStatus()
            focusCurrentLocationAfterPermission =
                false
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

                LaunchedEffect(Unit) {
                    while (true) {
                        refreshDeviceLocationStatus()
                        delay(2_000L)
                    }
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

                    val message =
                        cleaningErrorMessage
                            ?: cleaningSuccessMessage
                            ?: return@LaunchedEffect


                    Toast
                        .makeText(
                            this@MainActivity,
                            message,
                            Toast.LENGTH_LONG
                        )
                        .show()


                    cleaningViewModel
                        .clearMessages()
                }


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

                    toilets
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
                 * 清掃依頼が別端末で更新された場合も、
                 * toilets.cleaning_status を再取得して
                 * ピンと詳細表示を同期する。
                 */
                LaunchedEffect(
                    cleaningRequests
                ) {

                    toiletViewModel
                        .loadToilets()
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
                     * 清掃依頼・担当状態
                     */
                    cleaningRequests =
                        cleaningRequests,

                    currentUserId =
                        currentUserId,

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


                        cleaningViewModel
                            .requestCleaning(
                                toiletId = toilet.id,
                                rewardPoints = rewardPoints
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
                     * 清掃完了・報酬受取
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
                            toilets
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
                     * トイレ削除
                     * =====================================
                     */
                    onDeleteToilet = {
                            toilet ->

                        toiletViewModel
                            .deleteToilet(
                                toiletId = toilet.id
                            ) {
                                if (selectedToiletId == toilet.id) {
                                    selectedToiletId = null
                                }

                                cleaningViewModel.loadRequests()

                                Toast
                                    .makeText(
                                        this@MainActivity,
                                        "トイレを削除しました",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
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
                     * 端末の現在地取得状態
                     * =====================================
                     */
                    locationStatus =
                        deviceLocationStatus,


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

                        enableUserLocation(
                            focus = true
                        )
                    }
                )
            }
        }
    }


    /*
     * =====================================
     * 現在地表示を有効化
     * =====================================
     */
    private fun enableUserLocation(
        focus: Boolean
    ) {

        if (hasLocationPermission()) {
            mapController.enableUserLocation(
                focus = focus
            )
            return
        }

        focusCurrentLocationAfterPermission =
            focus

        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }


    private fun refreshDeviceLocationStatus() {

        if (!hasLocationPermission()) {
            deviceLocationStatus =
                DeviceLocationStatus.PERMISSION_DENIED
            return
        }

        if (!isDeviceLocationServiceEnabled()) {
            deviceLocationStatus =
                DeviceLocationStatus.DEVICE_LOCATION_OFF
            return
        }

        if (
            ::mapController.isInitialized &&
            mapController.hasRecentUserLocation()
        ) {
            deviceLocationStatus =
                DeviceLocationStatus.AVAILABLE
            return
        }

        val elapsedMillis =
            System.currentTimeMillis() -
                    locationCheckStartedAtMillis

        deviceLocationStatus =
            if (elapsedMillis < 8_000L) {
                DeviceLocationStatus.CHECKING
            } else {
                DeviceLocationStatus.WAITING_FOR_SIGNAL
            }
    }


    private fun hasLocationPermission(): Boolean {

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

        return fineGranted ||
                coarseGranted
    }


    private fun isDeviceLocationServiceEnabled(): Boolean {

        val locationManager =
            getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            val gpsEnabled =
                try {
                    locationManager.isProviderEnabled(
                        LocationManager.GPS_PROVIDER
                    )
                } catch (_: Exception) {
                    false
                }

            val networkEnabled =
                try {
                    locationManager.isProviderEnabled(
                        LocationManager.NETWORK_PROVIDER
                    )
                } catch (_: Exception) {
                    false
                }

            gpsEnabled || networkEnabled
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