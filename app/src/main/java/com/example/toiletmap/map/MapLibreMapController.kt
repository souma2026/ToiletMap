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
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.module.http.HttpRequestUtil

class MapLibreMapController(

    private val activity:
    ComponentActivity,

    savedInstanceState:
    Bundle?

) : DefaultLifecycleObserver {

    /*
     * =====================================
     * MapView
     * =====================================
     */
    val mapView: MapView

    /*
     * =====================================
     * MapLibreの地図本体
     * =====================================
     *
     * 地図読み込みが終わったら
     * MapLibreMapがここに入る
     */
    private var mapLibreMap:
            MapLibreMap? = null

    /*
     * =====================================
     * スタイルが読み込まれたか
     * =====================================
     */
    private var isStyleLoaded =
        false

    /*
     * =====================================
     * トイレ一覧
     * =====================================
     *
     * mutableListOfなので
     *
     * toilets.add(...)
     *
     * で後からトイレを追加できる
     *
     * 注意：
     * 現在はアプリを完全終了すると
     * ユーザーが追加したトイレは消える
     */
    private val toilets =
        mutableListOf(

            /*
             * 最初から表示している
             * サンプルトイレ
             */
            Toilet(

                name =
                    "東京駅トイレ",

                latitude =
                    35.681236,

                longitude =
                    139.767125,

                cleanliness =
                    4,

                comment =
                    "東京駅の近くにあるトイレです"
            )
        )

    /*
     * =====================================
     * 初期化
     * =====================================
     */
    init {

        /*
         * MapLibre初期化
         */
        MapLibre.getInstance(
            activity
        )

        /*
         * =====================================
         * OpenStreetMapへ送る
         * User-Agentを設定
         * =====================================
         */
        val okHttpClient =
            OkHttpClient
                .Builder()

                .addNetworkInterceptor {
                        chain ->

                    val request =
                        chain
                            .request()

                            .newBuilder()

                            .header(
                                "User-Agent",
                                "ToiletMap/1.0 (Android; com.example.toiletmap)"
                            )

                            .build()

                    chain.proceed(
                        request
                    )
                }

                .build()

        HttpRequestUtil
            .setOkHttpClient(
                okHttpClient
            )

        /*
         * =====================================
         * MapViewを作成
         * =====================================
         */
        mapView =
            MapView(activity)

        mapView.onCreate(
            savedInstanceState
        )

        /*
         * 地図設定
         */
        setupMap()

        /*
         * Activityのライフサイクルを監視
         */
        activity
            .lifecycle
            .addObserver(this)
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
             * 後から使えるように保存
             */
            mapLibreMap = map

            /*
             * =====================================
             * OpenStreetMap
             * ラスタータイル
             * =====================================
             */
            val styleJson =
                """
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
             * 地図スタイルを設定
             */
            map.setStyle(

                Style
                    .Builder()
                    .fromJson(
                        styleJson
                    )

            ) {

                /*
                 * スタイル読み込み完了
                 */
                isStyleLoaded = true

                /*
                 * =====================================
                 * 最初に表示する位置
                 * =====================================
                 *
                 * 東京駅周辺
                 */
                map.cameraPosition =

                    CameraPosition
                        .Builder()

                        .target(

                            LatLng(
                                35.681236,
                                139.767125
                            )
                        )

                        .zoom(
                            14.0
                        )

                        .build()

                /*
                 * =====================================
                 * 登録済みトイレを
                 * 全部地図に表示
                 * =====================================
                 */
                toilets.forEach {
                        toilet ->

                    addMarkerToMap(

                        map = map,

                        toilet = toilet
                    )
                }
            }
        }
    }

    /*
     * =====================================
     * 新しいトイレを追加
     * =====================================
     *
     * AddToiletScreen
     *
     * ↓
     *
     * ToiletMapApp
     *
     * ↓
     *
     * MainActivity
     *
     * ↓
     *
     * この関数
     */
    fun addToilet(
        toilet: Toilet
    ) {

        /*
         * トイレ一覧へ追加
         */
        toilets.add(
            toilet
        )

        /*
         * 現在の地図を取得
         */
        val map =
            mapLibreMap

        /*
         * 地図とスタイルの準備が
         * 完了している場合
         */
        if (
            map != null &&
            isStyleLoaded
        ) {

            /*
             * 地図へピン追加
             */
            addMarkerToMap(

                map = map,

                toilet = toilet
            )

            /*
             * =====================================
             * 登録した場所へ
             * 地図を移動
             * =====================================
             */
            map.cameraPosition =

                CameraPosition
                    .Builder()

                    .target(

                        LatLng(

                            toilet.latitude,

                            toilet.longitude
                        )
                    )

                    .zoom(
                        16.0
                    )

                    .build()
        }
    }

    /*
     * =====================================
     * トイレを1個
     * 地図上のピンとして表示
     * =====================================
     */
    @Suppress("DEPRECATION")
    private fun addMarkerToMap(

        map: MapLibreMap,

        toilet: Toilet
    ) {

        /*
         * =====================================
         * 清潔度を★にする
         * =====================================
         *
         * cleanliness = 4なら
         *
         * ★★★★☆
         */
        val stars =

            "★".repeat(
                toilet.cleanliness
            ) +

                    "☆".repeat(
                        5 -
                                toilet.cleanliness
                    )

        /*
         * =====================================
         * ピンを押したときに表示する情報
         * =====================================
         */

        val detailText =

            /*
             * コメントが空の場合
             */
            if (
                toilet.comment
                    .isBlank()
            ) {

                "清潔度：$stars"

            } else {

                /*
                 * コメントがある場合
                 */
                "清潔度：$stars　${toilet.comment}"
            }

        /*
         * =====================================
         * 実際にピンを追加
         * =====================================
         */
        map.addMarker(

            MarkerOptions()

                /*
                 * ピンの位置
                 */
                .position(

                    LatLng(

                        toilet.latitude,

                        toilet.longitude
                    )
                )

                /*
                 * ピンを押したときのタイトル
                 */
                .title(
                    toilet.name
                )

                /*
                 * ピンを押したときの詳細
                 */
                .snippet(
                    detailText
                )
        )
    }

    /*
     * =====================================
     * MapView
     * ライフサイクル
     * =====================================
     */

    override fun onStart(
        owner:
        LifecycleOwner
    ) {

        mapView.onStart()
    }

    override fun onResume(
        owner:
        LifecycleOwner
    ) {

        mapView.onResume()
    }

    override fun onPause(
        owner:
        LifecycleOwner
    ) {

        mapView.onPause()
    }

    override fun onStop(
        owner:
        LifecycleOwner
    ) {

        mapView.onStop()
    }

    override fun onDestroy(
        owner:
        LifecycleOwner
    ) {

        mapView.onDestroy()

        owner
            .lifecycle
            .removeObserver(
                this
            )
    }

    /*
     * =====================================
     * 状態保存
     * =====================================
     */
    fun onSaveInstanceState(
        outState:
        Bundle
    ) {

        mapView
            .onSaveInstanceState(
                outState
            )
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