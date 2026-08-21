package com.example.toiletmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.module.http.HttpRequestUtil

class MainActivity : ComponentActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MapLibreを初期化
        MapLibre.getInstance(this)

        // OSMに「ToiletMapというアプリからアクセスしています」と伝える
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

        // MapLibreの通信に上で作ったOkHttpClientを使用
        HttpRequestUtil.setOkHttpClient(okHttpClient)

        // activity_main.xmlを表示
        setContentView(R.layout.activity_main)

        // XMLのMapViewを取得
        mapView = findViewById(R.id.mapView)

        // MapViewを初期化
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { map ->

            // OpenStreetMapのラスタータイル
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

            // 東京駅を最初に表示
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
    }

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

//MainActivityとlayout/activity_mainとbuild.gradle.kts(app)とAndroidManifestを変更した