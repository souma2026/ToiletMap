package com.example.toiletmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.toiletmap.map.MapLibreMapController
import com.example.toiletmap.model.Toilet
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

    // 現在タップされているトイレ
    private var selectedToilet by mutableStateOf<Toilet?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        // 地図上のピンが押されたとき
        mapController.setOnToiletMarkerClickListener { toilet ->
            selectedToilet = toilet
        }

        setContent {

            ToiletMapTheme {

                ToiletMapApp(

                    mapView = mapController.mapView,

                    selectedToilet = selectedToilet,

                    // 詳細画面を閉じる
                    onDismissSelectedToilet = {
                        selectedToilet = null
                    },

                    // 清掃を依頼する
                    onRequestCleaning = { toilet ->

                        selectedToilet =
                            mapController.requestCleaning(
                                toilet.id
                            )
                    },

                    // 清掃済みにする
                    onMarkCleaned = { toilet ->

                        selectedToilet =
                            mapController.markCleaned(
                                toilet.id
                            )
                    },

                    // 新しいトイレを登録
                    onAddToilet = { toilet ->

                        mapController.addToilet(
                            toilet
                        )

                        // 登録直後にそのトイレを選択状態にする
                        selectedToilet = toilet
                    }
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        mapController.onSaveInstanceState(
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

        mapController.onLowMemory()
    }
}