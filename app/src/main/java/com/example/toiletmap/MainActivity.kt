package com.example.toiletmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.screen.listofuncleaned.UncleanedToilet
import com.example.toiletmap.screen.map.MapLibreMapController
import com.example.toiletmap.ui.ToiletMapApp
import com.example.toiletmap.ui.theme.ToiletMapTheme
import com.example.toiletmap.viewmodel.ToiletViewModel


class MainActivity : ComponentActivity() {

    private lateinit var mapController: MapLibreMapController

    private lateinit var toiletViewModel: ToiletViewModel


    /*
     * 現在選択しているトイレ
     */
    private var selectedToiletId by mutableStateOf<String?>(null)


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        /*
         * =====================================
         * ToiletViewModel
         * =====================================
         */
        toiletViewModel =
            ViewModelProvider(this)[ToiletViewModel::class.java]


        /*
         * =====================================
         * MapLibre
         * =====================================
         */
        mapController =
            MapLibreMapController(
                activity = this,
                savedInstanceState = savedInstanceState
            )


        /*
         * =====================================
         * 地図上のピンを押したとき
         * =====================================
         */
        mapController.setOnToiletMarkerClickListener { toilet ->

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
                 * Supabaseから取得したトイレ一覧
                 * =====================================
                 */
                val toilets by
                toiletViewModel
                    .toilets
                    .collectAsStateWithLifecycle()


                /*
                 * =====================================
                 * 選択中のトイレ
                 * =====================================
                 */
                val selectedToilet =

                    toilets.firstOrNull { toilet ->

                        toilet.id ==
                                selectedToiletId
                    }


                /*
                 * =====================================
                 * 清掃待ちのトイレ
                 * =====================================
                 */
                val uncleanedToilets =

                    toilets
                        .filter { toilet ->

                            toilet.cleaningStatus ==
                                    CleaningStatus.REQUESTED
                        }
                        .map { toilet ->

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
                 * トイレ一覧が変化したら
                 * 地図のピンを更新
                 * =====================================
                 */
                LaunchedEffect(toilets) {

                    mapController.showToilets(
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


                    /*
                     * 検索対象
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
                     * 検索結果を押したとき
                     * =====================================
                     */
                    onSearchToiletSelected = { toilet ->


                        /*
                         * 選択中のトイレを変更
                         */
                        selectedToiletId =
                            toilet.id


                        /*
                         * 地図をそのトイレまで移動
                         */
                        mapController.focusOnToilet(
                            toilet
                        )
                    },


                    /*
                     * =====================================
                     * 詳細画面を閉じる
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
                    onRequestCleaning = { toilet ->

                        toiletViewModel.requestCleaning(
                            toilet.id
                        )
                    },


                    /*
                     * =====================================
                     * 清掃完了
                     * =====================================
                     */
                    onMarkCleaned = { toilet ->

                        toiletViewModel.markCleaned(
                            toilet.id
                        )
                    },


                    /*
                     * =====================================
                     * トイレ追加
                     * =====================================
                     */
                    onAddToilet = { toilet ->

                        toiletViewModel.addToilet(
                            toilet
                        )


                        selectedToiletId =
                            toilet.id


                        mapController.focusOnToilet(
                            toilet
                        )
                    }
                )
            }
        }
    }


    override fun onSaveInstanceState(
        outState: Bundle
    ) {

        mapController.onSaveInstanceState(
            outState
        )

        super.onSaveInstanceState(
            outState
        )
    }


    override fun onLowMemory() {

        super.onLowMemory()

        mapController.onLowMemory()
    }
}