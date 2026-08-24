@file:Suppress("DEPRECATION")

package com.example.toiletmap.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
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
     * MapLibre本体
     * =====================================
     */
    private var mapLibreMap:
            MapLibreMap? =
        null


    /*
     * =====================================
     * スタイル読み込み状態
     * =====================================
     */
    private var isStyleLoaded =
        false


    /*
     * =====================================
     * 描画用トイレ一覧
     * =====================================
     *
     * ViewModelから渡された最新状態を
     * 地図に描画するためだけに参照する。
     *
     * ここでは
     *
     * ・トイレ追加
     * ・清掃状態変更
     *
     * などのデータ処理は行わない。
     */
    private var latestToiletsForRendering:
            List<Toilet> =
        emptyList()


    /*
     * =====================================
     * Marker ID
     * ↓
     * Toilet ID
     * =====================================
     */
    private val markerIdToToiletId =
        mutableMapOf<Long, String>()


    /*
     * =====================================
     * トイレピンが押されたとき
     * =====================================
     */
    private var onToiletMarkerClick:
            ((Toilet) -> Unit)? =
        null


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
         * HTTP Client
         * =====================================
         *
         * OSMへUser-Agentを送る
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
         * MapView
         * =====================================
         */
        mapView =
            MapView(
                activity
            )

        mapView
            .onCreate(
                savedInstanceState
            )


        /*
         * 地図設定
         */
        setupMap()


        /*
         * Activity lifecycle監視
         */
        activity
            .lifecycle
            .addObserver(
                this
            )
    }


    /*
     * =====================================
     * ピンタップイベント登録
     * =====================================
     */
    fun setOnToiletMarkerClickListener(

        listener:
        ((Toilet) -> Unit)?

    ) {

        onToiletMarkerClick =
            listener
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
             * MapLibreMap保存
             */
            mapLibreMap =
                map


            /*
             * =====================================
             * トイレピンタップ
             * =====================================
             */
            map.setOnMarkerClickListener {
                    marker ->

                /*
                 * Markerから
                 * Toilet IDを取得
                 */
                val toiletId =
                    markerIdToToiletId[
                        marker.id
                    ]


                /*
                 * 最新の一覧から
                 * Toiletを検索
                 */
                val toilet =
                    latestToiletsForRendering
                        .firstOrNull {

                            it.id ==
                                    toiletId
                        }


                if (
                    toilet != null
                ) {

                    /*
                     * MainActivityへ通知
                     */
                    onToiletMarkerClick
                        ?.invoke(
                            toilet
                        )

                    true

                } else {

                    false
                }
            }


            /*
             * =====================================
             * OpenStreetMap
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
                 * 初期位置
                 * =====================================
                 *
                 * 東京駅
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
                 * ViewModelからすでに
                 * データが来ている場合
                 * 地図へ表示
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
     * この関数では
     * トイレデータそのものを
     * 変更しない。
     */
    fun showToilets(
        toilets: List<Toilet>
    ) {

        latestToiletsForRendering =
            toilets

        renderLatestToilets()
    }


    /*
     * =====================================
     * 最新トイレ一覧を描画
     * =====================================
     */
    private fun renderLatestToilets() {

        val map =
            mapLibreMap
                ?: return


        if (
            !isStyleLoaded
        ) {

            return
        }


        /*
         * =====================================
         * 現在のMarkerを全削除
         * =====================================
         */
        map.clear()


        /*
         * Marker ID対応表も削除
         */
        markerIdToToiletId
            .clear()


        /*
         * =====================================
         * 最新一覧から再描画
         * =====================================
         */
        latestToiletsForRendering
            .forEach {
                    toilet ->

                addMarkerToMap(

                    map =
                        map,

                    toilet =
                        toilet
                )
            }
    }


    /*
     * =====================================
     * 指定したトイレへカメラ移動
     * =====================================
     */
    fun focusOnToilet(
        toilet: Toilet
    ) {

        val map =
            mapLibreMap
                ?: return


        if (
            !isStyleLoaded
        ) {

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
     * トイレ1件をMarkerとして追加
     * =====================================
     */
    private fun addMarkerToMap(

        map:
        MapLibreMap,

        toilet:
        Toilet

    ) {

        /*
         * =====================================
         * Marker作成
         * =====================================
         */
        val marker =
            map.addMarker(

                MarkerOptions()

                    /*
                     * 位置
                     */
                    .position(

                        LatLng(

                            toilet.latitude,

                            toilet.longitude
                        )
                    )

                    /*
                     * 名前
                     */
                    .title(
                        toilet.name
                    )

                    /*
                     * 詳細
                     */
                    .snippet(
                        buildDetailText(
                            toilet
                        )
                    )

                    /*
                     * 清掃状態に応じた色
                     */
                    .icon(
                        createMarkerIcon(
                            toilet.cleaningStatus
                        )
                    )
            )


        /*
         * Marker ID
         * ↓
         * Toilet ID
         */
        markerIdToToiletId[
            marker.id
        ] =
            toilet.id
    }


    /*
     * =====================================
     * Marker詳細テキスト
     * =====================================
     */
    private fun buildDetailText(
        toilet: Toilet
    ): String {

        /*
         * 清潔度
         *
         * 4
         * ↓
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
         * 清掃状態
         */
        val statusText =

            when (
                toilet.cleaningStatus
            ) {

                CleaningStatus.NORMAL ->

                    "通常"

                CleaningStatus.REQUESTED ->

                    "清掃待ち"
            }


        /*
         * コメントなし
         */
        return if (
            toilet.comment
                .isBlank()
        ) {

            "清潔度：$stars / 状態：$statusText"

        } else {

            /*
             * コメントあり
             */
            "清潔度：$stars / 状態：$statusText / ${toilet.comment}"
        }
    }


    /*
     * =====================================
     * ピン画像作成
     * =====================================
     *
     * NORMAL
     * → 赤
     *
     * REQUESTED
     * → 黄色
     */
    private fun createMarkerIcon(
        status: CleaningStatus
    ): Icon {

        val density =
            activity
                .resources
                .displayMetrics
                .density


        val width =
            (
                    42f *
                            density
                    ).toInt()


        val height =
            (
                    56f *
                            density
                    ).toInt()


        /*
         * Bitmap
         */
        val bitmap =
            Bitmap.createBitmap(

                width,

                height,

                Bitmap.Config.ARGB_8888
            )


        val canvas =
            Canvas(
                bitmap
            )


        /*
         * =====================================
         * 清掃状態による色
         * =====================================
         */
        val pinColor =

            when (
                status
            ) {

                /*
                 * 通常
                 * → 赤
                 */
                CleaningStatus.NORMAL ->

                    Color.rgb(
                        244,
                        67,
                        54
                    )


                /*
                 * 清掃依頼
                 * → 黄色
                 */
                CleaningStatus.REQUESTED ->

                    Color.rgb(
                        255,
                        193,
                        7
                    )
            }


        /*
         * ピン本体
         */
        val fillPaint =
            Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {

                color =
                    pinColor

                style =
                    Paint.Style.FILL
            }


        /*
         * 外枠
         */
        val strokePaint =
            Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {

                color =
                    Color.rgb(
                        80,
                        80,
                        80
                    )

                style =
                    Paint.Style.STROKE

                strokeWidth =
                    1.5f *
                            density
            }


        /*
         * 中央の白丸
         */
        val whitePaint =
            Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {

                color =
                    Color.WHITE

                style =
                    Paint.Style.FILL
            }


        val centerX =
            width /
                    2f


        val circleCenterY =
            18f *
                    density


        val outerRadius =
            14f *
                    density


        /*
         * =====================================
         * ピンの下部分
         * =====================================
         */
        val pointPath =
            Path().apply {

                moveTo(

                    centerX -
                            9f *
                            density,

                    28f *
                            density
                )

                lineTo(

                    centerX,

                    52f *
                            density
                )

                lineTo(

                    centerX +
                            9f *
                            density,

                    28f *
                            density
                )

                close()
            }


        /*
         * 下部分描画
         */
        canvas.drawPath(

            pointPath,

            fillPaint
        )


        canvas.drawPath(

            pointPath,

            strokePaint
        )


        /*
         * =====================================
         * ピン上部の円
         * =====================================
         */
        canvas.drawCircle(

            centerX,

            circleCenterY,

            outerRadius,

            fillPaint
        )


        canvas.drawCircle(

            centerX,

            circleCenterY,

            outerRadius,

            strokePaint
        )


        /*
         * 中央の白丸
         */
        canvas.drawCircle(

            centerX,

            circleCenterY,

            5f *
                    density,

            whitePaint
        )


        /*
         * =====================================
         * MapLibre Iconへ変換
         * =====================================
         */
        return IconFactory
            .getInstance(
                activity
            )
            .fromBitmap(
                bitmap
            )
    }


    /*
     * =====================================
     * MapView lifecycle
     * =====================================
     */
    override fun onStart(
        owner: LifecycleOwner
    ) {

        mapView
            .onStart()
    }


    override fun onResume(
        owner: LifecycleOwner
    ) {

        mapView
            .onResume()
    }


    override fun onPause(
        owner: LifecycleOwner
    ) {

        mapView
            .onPause()
    }


    override fun onStop(
        owner: LifecycleOwner
    ) {

        mapView
            .onStop()
    }


    override fun onDestroy(
        owner: LifecycleOwner
    ) {

        mapView
            .onDestroy()

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

        mapView
            .onLowMemory()
    }
}