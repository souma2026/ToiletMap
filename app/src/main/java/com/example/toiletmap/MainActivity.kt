package com.example.toiletmap

import android.os.Bundle
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
import com.example.toiletmap.viewmodel.ToiletViewModel


class MainActivity : ComponentActivity() {

    private lateinit var mapController:
            MapLibreMapController

    private lateinit var toiletViewModel:
            ToiletViewModel


    /*
     * =====================================
     * 選択中トイレID
     * =====================================
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
         * 地図のトイレピンが押されたとき
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


                /*
                 * =====================================
                 * 選択中トイレ
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
                 *
                 * CleaningStatus.REQUESTED
                 * のトイレだけ取得
                 *
                 * Toilet
                 * ↓
                 * UncleanedToilet
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
                                    toilet.lastCleanedAtMillis
                            )
                        }


                /*
                 * =====================================
                 * トイレ一覧更新
                 * ↓
                 * 地図更新
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

                    mapView =
                        mapController.mapView,


                    selectedToilet =
                        selectedToilet,


                    uncleanedToilets =
                        uncleanedToilets,


                    /*
                     * =====================================
                     * 未清掃一覧
                     * ↓
                     * 地図で見る
                     * =====================================
                     */
                    onShowUncleanedToiletOnMap = {
                            uncleanedToilet ->


                        /*
                         * =====================================
                         * 最新のToiletをIDから取得
                         * =====================================
                         */
                        val toilet =

                            toilets
                                .firstOrNull {

                                    it.id ==
                                            uncleanedToilet.id
                                }


                        if (
                            toilet != null
                        ) {


                            /*
                             * =====================================
                             * 詳細表示するトイレに設定
                             * =====================================
                             */
                            selectedToiletId =
                                toilet.id


                            /*
                             * =====================================
                             * トイレを画面中央へ
                             *
                             * 15.0なので
                             * 少し周辺も見える
                             * =====================================
                             */
                            mapController
                                .focusOnToilet(

                                    toilet =
                                        toilet,

                                    zoom =
                                        15.0
                                )
                        }
                    },


                    /*
                     * =====================================
                     * トイレ詳細を閉じる
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
                            toilet ->

                        toiletViewModel
                            .requestCleaning(
                                toilet.id
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
                     * 新しいトイレ登録
                     * =====================================
                     */
                    onAddToilet = {
                            toilet ->


                        toiletViewModel
                            .addToilet(
                                toilet
                            )


                        selectedToiletId =
                            toilet.id


                        mapController
                            .focusOnToilet(
                                toilet
                            )
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