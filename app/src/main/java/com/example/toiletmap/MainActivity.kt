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
import com.example.toiletmap.map.MapLibreMapController
import com.example.toiletmap.ui.ToiletMapApp
import com.example.toiletmap.ui.theme.ToiletMapTheme
import com.example.toiletmap.viewmodel.ToiletViewModel

class MainActivity : ComponentActivity() {

    private lateinit var mapController: MapLibreMapController

    private lateinit var toiletViewModel: ToiletViewModel

    /*
     * 選択中のトイレそのものではなく
     * トイレのIDだけを保持する。
     *
     * Repositoryのトイレ情報が更新された場合でも
     * 最新のToiletを一覧から取得できる。
     */
    private var selectedToiletId by
    mutableStateOf<String?>(null)

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

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
                activity = this,
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
                 * ViewModelのトイレ一覧を監視
                 */
                val toilets by
                toiletViewModel
                    .toilets
                    .collectAsState()

                /*
                 * 選択されているトイレの
                 * 最新データを取得
                 */
                val selectedToilet =
                    toilets.firstOrNull {
                            toilet ->

                        toilet.id ==
                                selectedToiletId
                    }

                /*
                 * =====================================
                 * トイレ一覧が更新されたら
                 * 地図も更新
                 * =====================================
                 *
                 * MapLibreMapController自身では
                 * トイレデータを管理しない。
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

                    /*
                     * 詳細を閉じる
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

                        /*
                         * Repositoryへ追加
                         */
                        toiletViewModel
                            .addToilet(
                                toilet
                            )

                        /*
                         * 登録したトイレを選択状態にする
                         */
                        selectedToiletId =
                            toilet.id

                        /*
                         * 登録した位置へ地図移動
                         */
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