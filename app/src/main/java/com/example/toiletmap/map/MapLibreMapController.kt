package com.example.toiletmap.map

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.toiletmap.model.Toilet
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.module.http.HttpRequestUtil

class MapLibreMapController(
    private val activity: ComponentActivity,
    savedInstanceState: Bundle?
) : DefaultLifecycleObserver {

    val mapView: MapView


    /*
     * =====================================
     * 仮のトイレデータ
     * =====================================
     *
     * 今はコードに直接書いています。
     *
     * 将来的には
     * AddToiletScreenで登録したデータを
     * ここに持ってくるようにします。
     */
    private val toilets = listOf(

        Toilet(
            name = "東京駅トイレ",
            latitude = 35.681236,
            longitude = 139.767125,
            cleanliness = 4,
            comment = "東京駅の近くにあるトイレです"
        )
    )


    init {

        /*
         * =====================================
         * MapLibre初期化
         * =====================================
         */
        MapLibre.getInstance(activity)


        /*
         * =====================================
         * OSMへのUser-Agent設定
         * =====================================
         *
         * 403エラー対策
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
         * =====================================
         * MapViewを作る
         * =====================================
         */
        mapView = MapView(activity)

        mapView.onCreate(savedInstanceState)


        /*
         * 地図の設定
         */
        setupMap()


        /*
         * Activityのライフサイクルを監視
         */
        activity.lifecycle.addObserver(this)
    }


    /*
     * =====================================
     * 地図を設定
     * =====================================
     */
    @Suppress("DEPRECATION")
    private fun setupMap() {

        mapView.getMapAsync { map ->


            /*
             * =====================================
             * OpenStreetMap
             * ラスタータイル
             * =====================================
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


            /*
             * 地図スタイルを読み込む
             */
            map.setStyle(
                Style.Builder().fromJson(styleJson)
            ) {


                /*
                 * =====================================
                 * 最初の表示位置
                 * =====================================
                 *
                 * 東京駅周辺
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


                /*
                 * =====================================
                 * トイレのピンを追加
                 * =====================================
                 */
                toilets.forEach { toilet ->


                    /*
                     * 清潔度を★で表示
                     *
                     * 例:
                     * ★★★★☆
                     */
                    val stars =
                        "★".repeat(toilet.cleanliness) +
                                "☆".repeat(5 - toilet.cleanliness)


                    /*
                     * 地図上にピンを追加
                     */
                    map.addMarker(

                        MarkerOptions()

                            // ピンを置く場所
                            .position(
                                LatLng(
                                    toilet.latitude,
                                    toilet.longitude
                                )
                            )

                            // ピンを押した時のタイトル
                            .title(
                                toilet.name
                            )

                            // ピンを押した時の説明
                            .snippet(
                                "清潔度：$stars　${toilet.comment}"
                            )
                    )
                }
            }
        }
    }


    /*
     * =====================================
     * MapViewのライフサイクル
     * =====================================
     */

    override fun onStart(owner: LifecycleOwner) {
        mapView.onStart()
    }

    override fun onResume(owner: LifecycleOwner) {
        mapView.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        mapView.onPause()
    }

    override fun onStop(owner: LifecycleOwner) {
        mapView.onStop()
    }

    override fun onDestroy(owner: LifecycleOwner) {

        mapView.onDestroy()

        owner.lifecycle.removeObserver(this)
    }


    /*
     * =====================================
     * 状態保存
     * =====================================
     */

    fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
    }


    /*
     * =====================================
     * メモリ不足
     * =====================================
     */

    fun onLowMemory() {
        mapView.onLowMemory()
    }
}