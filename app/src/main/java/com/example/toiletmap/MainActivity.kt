package com.example.toiletmap

import android.Manifest
import android.content.pm.PackageManager
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
import com.example.toiletmap.screen.map.MapLibreMapController
import com.example.toiletmap.ui.ToiletMapApp
import com.example.toiletmap.ui.theme.ToiletMapTheme
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

                Toast
                    .makeText(
                        this,
                        "現在地を表示するには位置情報の許可が必要です",
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