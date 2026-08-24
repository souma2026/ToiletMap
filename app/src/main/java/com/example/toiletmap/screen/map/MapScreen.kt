package com.example.toiletmap.screen.map

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@Composable
fun MapScreen(
    mapView: MapView,
    isSelectingLocation: Boolean = false,
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> },
    onCancelLocationSelection: () -> Unit = {}
) {

    /*
     * =====================================
     * 地図をタップして場所を選ぶ処理
     * =====================================
     */
    DisposableEffect(
        mapView,
        isSelectingLocation
    ) {

        var disposed = false

        var clickListener:
                MapLibreMap.OnMapClickListener? = null

        var targetMap:
                MapLibreMap? = null

        /*
         * 「場所を選択中」の場合だけ
         * 地図タップを有効にする
         */
        if (isSelectingLocation) {

            mapView.getMapAsync { map ->

                if (!disposed) {

                    targetMap = map

                    /*
                     * 地図をタップしたとき
                     */
                    val listener =
                        MapLibreMap.OnMapClickListener { point ->

                            /*
                             * タップした場所の
                             * 緯度・経度を返す
                             */
                            onLocationSelected(
                                point.latitude,
                                point.longitude
                            )

                            true
                        }

                    clickListener = listener

                    /*
                     * 地図タップを監視
                     */
                    map.addOnMapClickListener(
                        listener
                    )
                }
            }
        }

        /*
         * この画面から離れたときに
         * リスナーを削除
         */
        onDispose {

            disposed = true

            val map = targetMap

            val listener = clickListener

            if (
                map != null &&
                listener != null
            ) {

                map.removeOnMapClickListener(
                    listener
                )
            }
        }
    }

    /*
     * =====================================
     * 地図全体
     * =====================================
     */
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        /*
         * MapLibreの地図を表示
         */
        AndroidView(

            factory = {

                /*
                 * 同じMapViewを
                 * 画面切り替え後も使えるようにする
                 */
                (mapView.parent as? ViewGroup)
                    ?.removeView(mapView)

                mapView
            },

            modifier = Modifier.fillMaxSize()
        )

        /*
         * =====================================
         * 場所選択中だけ表示する案内
         * =====================================
         */
        if (isSelectingLocation) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {

                    Text(
                        text = "トイレの場所を選択",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium
                    )

                    Text(
                        text =
                            "ピンを置きたい場所を地図上で1回タップしてください。",
                        modifier =
                            Modifier.padding(
                                top = 4.dp
                            )
                    )

                    /*
                     * キャンセル
                     */
                    TextButton(
                        onClick =
                            onCancelLocationSelection,
                        modifier =
                            Modifier.padding(
                                top = 4.dp
                            )
                    ) {

                        Text(
                            text = "キャンセル"
                        )
                    }
                }
            }
        }
    }
}