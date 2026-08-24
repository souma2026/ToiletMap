package com.example.toiletmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.example.toiletmap.map.MapLibreMapController
import com.example.toiletmap.ui.ToiletMapApp
import com.example.toiletmap.ui.theme.ToiletMapTheme
import com.example.toiletmap.viewmodel.ToiletViewModel

class MainActivity : ComponentActivity() {

    /*
     * =====================================
     * 地図Controller
     * =====================================
     */
    private lateinit var mapController:
            MapLibreMapController

    /*
     * =====================================
     * トイレデータ管理
     * =====================================
     */
    private lateinit var toiletViewModel:
            ToiletViewModel


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        /*
         * =====================================
         * ViewModel取得
         * =====================================
         */
        toiletViewModel =
            ViewModelProvider(this)[
                ToiletViewModel::class.java
            ]


        /*
         * =====================================
         * MapLibre準備
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
         * Compose画面
         * =====================================
         */
        setContent {

            ToiletMapTheme {

                /*
                 * =====================================
                 * ViewModelのトイレ一覧を監視
                 * =====================================
                 *
                 * toiletsが変更されると
                 * Compose側も変更を検知する
                 */
                val toilets by
                toiletViewModel
                    .toilets
                    .collectAsState()


                /*
                 * =====================================
                 * トイレ一覧が変更されたら
                 * 地図を更新
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

                    /*
                     * =====================================
                     * 新しいトイレが登録された
                     * =====================================
                     */
                    onAddToilet = { toilet ->

                        /*
                         * ViewModelだけに追加する
                         */
                        toiletViewModel
                            .addToilet(
                                toilet
                            )

                        /*
                         * 登録した場所へ
                         * カメラを移動
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