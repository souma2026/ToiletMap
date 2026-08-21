package com.example.toiletmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.toiletmap.screen.account.AccountScreen
import com.example.toiletmap.ui.screen.AddToiletScreen
import com.example.toiletmap.ui.theme.ToiletMapTheme
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.module.http.HttpRequestUtil

class MainActivity : ComponentActivity() {

    // MapLibreの地図本体
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
         * ==============================
         * MapLibreを初期化
         * ==============================
         */
        MapLibre.getInstance(this)

        /*
         * ==============================
         * OSMにUser-Agentを送る設定
         * ==============================
         *
         * これがないとOSMから
         * 403 Access blocked
         * になる場合があります。
         */
        val okHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor { chain ->

                val request = chain.request()
                    .newBuilder()
                    .header(
                        "User-Agent",
                        "ToiletMap/1.0 (Android; com.example.toiletmap)"
                    )
                    .build()

                chain.proceed(request)
            }
            .build()

        HttpRequestUtil.setOkHttpClient(okHttpClient)

        /*
         * ==============================
         * MapViewを作成
         * ==============================
         */
        mapView = MapView(this)

        mapView.onCreate(savedInstanceState)

        /*
         * ==============================
         * 地図の設定
         * ==============================
         */
        mapView.getMapAsync { map ->

            /*
             * OpenStreetMapの
             * ラスタータイルを使用
             */
            val styleJson = """
                {
                  "version": 8,
                  "sources": {
                    "osm": {
                      "type": "raster",
                      "tiles": [
                        "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
                      ],
                      "tileSize": 256,
                      "attribution": "© OpenStreetMap contributors"
                    }
                  },
                  "layers": [
                    {
                      "id": "osm-layer",
                      "type": "raster",
                      "source": "osm"
                    }
                  ]
                }
            """.trimIndent()

            map.setStyle(
                Style.Builder().fromJson(styleJson)
            )

            /*
             * 最初に東京駅を表示
             */
            map.cameraPosition = CameraPosition.Builder()
                .target(
                    LatLng(
                        35.681236,
                        139.767125
                    )
                )
                .zoom(14.0)
                .build()
        }

        /*
         * ==============================
         * Compose画面を表示
         * ==============================
         */
        setContent {

            ToiletMapTheme {

                ToiletMapApp(
                    mapView = mapView
                )
            }
        }
    }

    /*
     * ==============================
     * MapViewのライフサイクル
     * ==============================
     */

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
}


/*
 * =====================================
 * アプリ全体
 * =====================================
 */

@Composable
fun ToiletMapApp(
    mapView: MapView
) {

    /*
     * 現在選択している画面
     *
     * 0 = マップ
     * 1 = アカウント
     * 2 = 追加
     */
    var selectedScreen by rememberSaveable {
        mutableIntStateOf(0)
    }

    Scaffold(

        /*
         * =============================
         * 下のボトムメニュー
         * =============================
         */
        bottomBar = {

            NavigationBar {

                /*
                 * -------------------------
                 * マップ
                 * -------------------------
                 */
                NavigationBarItem(

                    selected = selectedScreen == 0,

                    onClick = {
                        selectedScreen = 0
                    },

                    icon = {
                        Text("🗺")
                    },

                    label = {
                        Text("マップ")
                    }
                )


                /*
                 * -------------------------
                 * アカウント
                 * -------------------------
                 */
                NavigationBarItem(

                    selected = selectedScreen == 1,

                    onClick = {
                        selectedScreen = 1
                    },

                    icon = {
                        Text("👤")
                    },

                    label = {
                        Text("アカウント")
                    }
                )


                /*
                 * -------------------------
                 * 追加
                 * -------------------------
                 */
                NavigationBarItem(

                    selected = selectedScreen == 2,

                    onClick = {
                        selectedScreen = 2
                    },

                    icon = {
                        Text("＋")
                    },

                    label = {
                        Text("追加")
                    }
                )
            }
        }

    ) { innerPadding ->

        /*
         * =============================
         * ボトムバーより上の画面
         * =============================
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            /*
             * 選択された画面を表示
             */
            when (selectedScreen) {

                // マップ画面
                0 -> {
                    MapScreen(
                        mapView = mapView
                    )
                }

                // アカウント画面
                1 -> {
                    AccountScreen()
                }

                // トイレ追加画面
                2 -> {
                    AddToiletScreen()
                }
            }
        }
    }
}


/*
 * =====================================
 * マップ画面
 * =====================================
 */

@Composable
fun MapScreen(
    mapView: MapView
) {

    /*
     * MapLibreのMapViewは
     * 普通のAndroid Viewです。
     *
     * AndroidViewを使うことで
     * Composeの中に表示できます。
     */
    AndroidView(

        factory = {
            mapView
        },

        modifier = Modifier.fillMaxSize()
    )
}