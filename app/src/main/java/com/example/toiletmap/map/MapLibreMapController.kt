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
import org.maplibre.android.annotations.Marker
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
     * MapLibre本体
     */
    val mapView:
            MapView

    private var mapLibreMap:
            MapLibreMap? =
        null

    private var isStyleLoaded =
        false

    /*
     * MarkerのID
     * ↓
     * ToiletのID
     */
    private val markerIdToToiletId =
        mutableMapOf<Long, String>()

    /*
     * ToiletのID
     * ↓
     * Marker
     *
     * ピンの色変更用
     */
    private val toiletIdToMarker =
        mutableMapOf<String, Marker>()

    /*
     * ピンを押したときの処理
     */
    private var onToiletMarkerClick:
            ((Toilet) -> Unit)? =
        null

    /*
     * =====================================
     * トイレ一覧
     * =====================================
     *
     * 今はメモリ上だけ。
     * アプリを終了すると追加データは消える。
     */
    private val toilets =
        mutableListOf(

            /*
             * 最初から表示するサンプル
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


    init {

        /*
         * =====================================
         * MapLibre初期化
         * =====================================
         */
        MapLibre.getInstance(
            activity
        )

        /*
         * =====================================
         * OSM 403対策
         * =====================================
         */
        val okHttpClient =

            OkHttpClient
                .Builder()

                .addNetworkInterceptor { chain ->

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
         * MapView作成
         */
        mapView =
            MapView(
                activity
            )

        mapView.onCreate(
            savedInstanceState
        )

        setupMap()

        /*
         * Activityのライフサイクルを監視
         */
        activity
            .lifecycle
            .addObserver(
                this
            )
    }


    /*
     * =====================================
     * ピンが押されたことを
     * MainActivityへ通知
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
     * 地図の初期設定
     * =====================================
     */
    @Suppress("DEPRECATION")
    private fun setupMap() {

        mapView.getMapAsync { map ->

            mapLibreMap =
                map

            /*
             * =====================================
             * ピンタップ
             * =====================================
             */
            map.setOnMarkerClickListener { marker ->

                /*
                 * Marker ID
                 * ↓
                 * Toilet ID
                 */
                val toiletId =

                    markerIdToToiletId[
                        marker.id
                    ]

                /*
                 * IDに一致するトイレを探す
                 */
                val toilet =

                    toilets
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

                    /*
                     * MapLibre標準吹き出しは
                     * 表示しない
                     */
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


            map.setStyle(

                Style
                    .Builder()
                    .fromJson(
                        styleJson
                    )

            ) {

                isStyleLoaded =
                    true

                /*
                 * 東京駅を初期表示
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
                 * 保存されているトイレを
                 * 全部ピン表示
                 */
                toilets.forEach { toilet ->

                    addMarkerToMap(
                        map =
                            map,

                        toilet =
                            toilet
                    )
                }
            }
        }
    }


    /*
     * =====================================
     * 新しいトイレを追加
     * =====================================
     */
    fun addToilet(

        toilet:
        Toilet
    ) {

        /*
         * 一覧に追加
         */
        toilets.add(
            toilet
        )

        val map =
            mapLibreMap

        if (
            map != null &&
            isStyleLoaded
        ) {

            /*
             * 地図にピン追加
             */
            addMarkerToMap(
                map =
                    map,

                toilet =
                    toilet
            )

            /*
             * 登録した場所へ移動
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
     * 清掃依頼
     *
     * NORMAL
     * ↓
     * REQUESTED
     * =====================================
     */
    fun requestCleaning(

        toiletId:
        String

    ): Toilet? {

        val toilet =

            toilets
                .firstOrNull {

                    it.id ==
                            toiletId
                }

                ?: return null


        /*
         * NORMAL以外なら変更しない
         */
        if (
            toilet.cleaningStatus !=
            CleaningStatus.NORMAL
        ) {

            return toilet
        }

        return updateCleaningStatus(

            toiletId =
                toiletId,

            newStatus =
                CleaningStatus.REQUESTED
        )
    }


    /*
     * =====================================
     * 清掃済み
     *
     * REQUESTED
     * ↓
     * CLEANED
     * =====================================
     */
    fun markCleaned(

        toiletId:
        String

    ): Toilet? {

        val toilet =

            toilets
                .firstOrNull {

                    it.id ==
                            toiletId
                }

                ?: return null


        if (
            toilet.cleaningStatus !=
            CleaningStatus.REQUESTED
        ) {

            return toilet
        }

        return updateCleaningStatus(

            toiletId =
                toiletId,

            newStatus =
                CleaningStatus.CLEANED
        )
    }


    /*
     * =====================================
     * 清掃状態変更
     * =====================================
     */
    @Suppress("DEPRECATION")
    private fun updateCleaningStatus(

        toiletId:
        String,

        newStatus:
        CleaningStatus

    ): Toilet? {

        val index =

            toilets
                .indexOfFirst {

                    it.id ==
                            toiletId
                }

        if (
            index == -1
        ) {

            return null
        }


        /*
         * Toiletはdata classなので
         * copyで新しい状態を作る
         */
        val updatedToilet =

            toilets[
                index
            ]
                .copy(

                    cleaningStatus =
                        newStatus
                )

        /*
         * 一覧を更新
         */
        toilets[
            index
        ] =
            updatedToilet


        /*
         * 該当ピン
         */
        val marker =

            toiletIdToMarker[
                toiletId
            ]

        val map =
            mapLibreMap


        if (
            marker != null &&
            map != null
        ) {

            /*
             * ピン色変更
             */
            marker.icon =

                createMarkerIcon(
                    newStatus
                )

            /*
             * 情報も更新
             */
            marker.snippet =

                buildDetailText(
                    updatedToilet
                )

            map.updateMarker(
                marker
            )
        }

        return updatedToilet
    }


    /*
     * =====================================
     * 地図にピンを追加
     * =====================================
     */
    @Suppress("DEPRECATION")
    private fun addMarkerToMap(

        map:
        MapLibreMap,

        toilet:
        Toilet
    ) {

        val marker =

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

                        buildDetailText(
                            toilet
                        )
                    )

                    .icon(

                        createMarkerIcon(
                            toilet.cleaningStatus
                        )
                    )
            )


        /*
         * MarkerとToiletを紐付け
         */
        markerIdToToiletId[
            marker.id
        ] =
            toilet.id


        toiletIdToMarker[
            toilet.id
        ] =
            marker
    }


    /*
     * =====================================
     * トイレ情報文字列
     * =====================================
     */
    private fun buildDetailText(

        toilet:
        Toilet

    ): String {

        val stars =

            "★".repeat(
                toilet.cleanliness
            ) +

                    "☆".repeat(
                        5 -
                                toilet.cleanliness
                    )


        val statusText =

            when (
                toilet.cleaningStatus
            ) {

                CleaningStatus.NORMAL ->
                    "通常"

                CleaningStatus.REQUESTED ->
                    "清掃依頼中"

                CleaningStatus.CLEANED ->
                    "清掃済み"
            }


        return if (
            toilet.comment.isBlank()
        ) {

            "清潔度：$stars / 状態：$statusText"

        } else {

            "清潔度：$stars / 状態：$statusText / ${toilet.comment}"
        }
    }


    /*
     * =====================================
     * 色付きピン作成
     * =====================================
     *
     * NORMAL
     * → 青
     *
     * REQUESTED
     * → 黄色
     *
     * CLEANED
     * → 赤
     */
    @Suppress("DEPRECATION")
    private fun createMarkerIcon(

        status:
        CleaningStatus

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
                    )
                .toInt()


        val height =

            (
                    56f *
                            density
                    )
                .toInt()


        val bitmap =

            Bitmap
                .createBitmap(

                    width,
                    height,

                    Bitmap.Config.ARGB_8888
                )


        val canvas =
            Canvas(
                bitmap
            )


        /*
         * 状態別の色
         */
        val pinColor =

            when (
                status
            ) {

                CleaningStatus.NORMAL ->

                    Color.rgb(
                        33,
                        150,
                        243
                    )


                CleaningStatus.REQUESTED ->

                    Color.rgb(
                        255,
                        193,
                        7
                    )


                CleaningStatus.CLEANED ->

                    Color.rgb(
                        244,
                        67,
                        54
                    )
            }


        val fillPaint =

            Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {

                color =
                    pinColor

                style =
                    Paint.Style.FILL
            }


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
            width / 2f


        val circleCenterY =
            18f *
                    density


        val outerRadius =
            14f *
                    density


        /*
         * ピンの尖った部分
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
         * 下側
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
         * 上側
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
            5f * density,
            whitePaint
        )


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
     * MapViewライフサイクル
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


    fun onSaveInstanceState(
        outState:
        Bundle
    ) {

        mapView
            .onSaveInstanceState(
                outState
            )
    }


    fun onLowMemory() {

        mapView.onLowMemory()
    }
}