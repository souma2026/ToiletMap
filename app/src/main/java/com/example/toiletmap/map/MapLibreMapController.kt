@file:Suppress("DEPRECATION")

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

    activity:
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
     */
    private var mapLibreMap:
            MapLibreMap? = null


    /*
     * =====================================
     * スタイル読み込み状態
     * =====================================
     */
    private var isStyleLoaded =
        false


    /*
     * =====================================
     * 描画用の最新データ
     * =====================================
     *
     * これはトイレデータの管理場所ではない。
     *
     * ViewModelから渡された最新状態を
     * 地図が準備できるまで一時的に
     * 覚えておくためだけに使用する。
     *
     * 追加・削除などのデータ変更は
     * このControllerでは行わない。
     */
    private var latestToiletsForRendering:
            List<Toilet> =
        emptyList()


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
         * User-Agent設定
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
         * MapView作成
         * =====================================
         */
        mapView =
            MapView(
                activity
            )

        mapView.onCreate(
            savedInstanceState
        )


        /*
         * 地図設定
         */
        setupMap()


        /*
         * Activityの
         * ライフサイクル監視
         */
        activity
            .lifecycle
            .addObserver(
                this
            )
    }


    /*
     * =====================================
     * 地図設定
     * =====================================
     */
    private fun setupMap() {

        mapView.getMapAsync {
                map ->


            /*
             * MapLibreMapを保存
             */
            mapLibreMap =
                map


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
             * =====================================
             * 地図スタイル設定
             * =====================================
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
                isStyleLoaded =
                    true


                /*
                 * =====================================
                 * 最初に表示する場所
                 * =====================================
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
                 * ViewModelから既にデータが
                 * 渡されていれば表示
                 * =====================================
                 */
                renderLatestToilets()
            }
        }
    }


    /*
     * =====================================
     * ViewModelのトイレ一覧を受け取る
     * =====================================
     *
     * このControllerでは
     * トイレを追加・削除しない。
     *
     * 渡された一覧を
     * 地図へ描画するだけ。
     */
    fun showToilets(
        toilets: List<Toilet>
    ) {

        /*
         * 最新の描画対象を保存
         */
        latestToiletsForRendering =
            toilets


        /*
         * 地図が準備できていれば
         * 描画する
         */
        renderLatestToilets()
    }


    /*
     * =====================================
     * 最新のトイレ一覧を
     * 地図へ描画
     * =====================================
     */
    @Suppress("DEPRECATION")
    private fun renderLatestToilets() {

        val map =
            mapLibreMap
                ?: return


        /*
         * スタイルがまだなら
         * 描画しない
         */
        if (!isStyleLoaded) {
            return
        }


        /*
         * =====================================
         * 一度すべてのマーカーを削除
         * =====================================
         *
         * その後ViewModelから来た
         * 最新の一覧を描画し直す
         */
        map.clear()


        /*
         * =====================================
         * 最新一覧をすべて表示
         * =====================================
         */
        latestToiletsForRendering
            .forEach {
                    toilet ->

                addMarkerToMap(

                    map = map,

                    toilet =
                        toilet
                )
            }
    }


    /*
     * =====================================
     * 登録したトイレへ
     * カメラ移動
     * =====================================
     *
     * データ管理ではなく
     * 地図表示だけを担当する
     */
    fun focusOnToilet(
        toilet: Toilet
    ) {

        val map =
            mapLibreMap
                ?: return


        if (!isStyleLoaded) {
            return
        }


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


    /*
     * =====================================
     * トイレ1件を
     * マーカーとして表示
     * =====================================
     */
    @Suppress("DEPRECATION")
    private fun addMarkerToMap(

        map: MapLibreMap,

        toilet: Toilet
    ) {


        /*
         * =====================================
         * 清潔度を★表示へ変換
         * =====================================
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
         * ピンを押したときの詳細
         * =====================================
         */
        val detailText =

            if (
                toilet.comment
                    .isBlank()
            ) {

                "清潔度：$stars"

            } else {

                "清潔度：$stars　${toilet.comment}"
            }


        /*
         * =====================================
         * マーカー追加
         * =====================================
         */
        map.addMarker(

            MarkerOptions()

                .position(

                    LatLng(

                        toilet.latitude,

                        toilet.longitude
                    )
                )

                .title(
                    toilet.name
                )

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
        owner: LifecycleOwner
    ) {

        mapView.onStart()
    }


    override fun onResume(
        owner: LifecycleOwner
    ) {

        mapView.onResume()
    }


    override fun onPause(
        owner: LifecycleOwner
    ) {

        mapView.onPause()
    }


    override fun onStop(
        owner: LifecycleOwner
    ) {

        mapView.onStop()
    }


    override fun onDestroy(
        owner: LifecycleOwner
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
        outState: Bundle
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