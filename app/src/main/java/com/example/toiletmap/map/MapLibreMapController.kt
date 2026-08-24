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

    private val activity: ComponentActivity,

    savedInstanceState: Bundle?

) : DefaultLifecycleObserver {

    val mapView: MapView

    private var mapLibreMap: MapLibreMap? = null

    private var isStyleLoaded = false

    /*
     * MarkerのID
     * ↓
     * ToiletのID
     *
     * どのピンがどのトイレかを判別する
     */
    private val markerIdToToiletId =
        mutableMapOf<Long, String>()

    /*
     * ToiletのID
     * ↓
     * Marker
     *
     * ピンの色を変更するときに使用
     */
    private val toiletIdToMarker =
        mutableMapOf<String, Marker>()

    /*
     * ピンを押したことを
     * Compose側へ伝える
     */
    private var onToiletMarkerClick:
            ((Toilet) -> Unit)? = null

    /*
     * トイレ一覧
     */
    private val toilets =
        mutableListOf(

            Toilet(

                name = "東京駅トイレ",

                latitude = 35.681236,

                longitude = 139.767125,

                cleanliness = 4,

                comment =
                    "東京駅の近くにあるトイレです"
            )
        )

    init {

        MapLibre.getInstance(
            activity
        )

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

        mapView =
            MapView(activity)

        mapView.onCreate(
            savedInstanceState
        )

        setupMap()

        activity
            .lifecycle
            .addObserver(
                this
            )
    }

    /*
     * ピンが押されたときのイベントを
     * MainActivityへ渡す
     */
    fun setOnToiletMarkerClickListener(

        listener:
        ((Toilet) -> Unit)?

    ) {

        onToiletMarkerClick =
            listener
    }

    @Suppress("DEPRECATION")
    private fun setupMap() {

        mapView.getMapAsync { map ->

            mapLibreMap =
                map

            /*
             * =================================
             * ピンを押したとき
             * =================================
             */
            map.setOnMarkerClickListener { marker ->

                /*
                 * Marker IDから
                 * Toilet IDを取得
                 */
                val toiletId =

                    markerIdToToiletId[
                        marker.id
                    ]

                /*
                 * Toiletを探す
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
                     * Compose側へ通知
                     */
                    onToiletMarkerClick
                        ?.invoke(
                            toilet
                        )

                    /*
                     * trueにすると
                     * MapLibre標準の吹き出しを
                     * 表示しない
                     */
                    true

                } else {

                    false
                }
            }

            /*
             * =================================
             * OpenStreetMap
             * =================================
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
                 * 初期位置
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
                 * 登録済みトイレを表示
                 */
                toilets.forEach {
                        toilet ->

                    addMarkerToMap(

                        map = map,

                        toilet =
                            toilet
                    )
                }
            }
        }
    }

    /*
     * =================================
     * 新しいトイレを追加
     * =================================
     */
    fun addToilet(

        toilet:
        Toilet

    ) {

        toilets.add(
            toilet
        )

        val map =
            mapLibreMap

        if (
            map != null &&
            isStyleLoaded
        ) {

            addMarkerToMap(

                map = map,

                toilet =
                    toilet
            )

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
     * =================================
     * 清掃を依頼する
     *
     * NORMAL
     * ↓
     * REQUESTED
     * =================================
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
         * 通常状態以外なら
         * 何もしない
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
     * =================================
     * 清掃しました
     *
     * REQUESTED
     * ↓
     * CLEANED
     * =================================
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

        /*
         * 清掃依頼中でなければ
         * 「清掃しました」にはできない
         */
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
     * =================================
     * 清掃状態を変更する
     * =================================
     */
    @Suppress("DEPRECATION")
    private fun updateCleaningStatus(

        toiletId:
        String,

        newStatus:
        CleaningStatus

    ): Toilet? {

        /*
         * 対象のトイレを探す
         */
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
         * 新しい状態のToiletを作成
         */
        val updatedToilet =

            toilets[index]
                .copy(

                    cleaningStatus =
                        newStatus
                )

        /*
         * 一覧を書き換える
         */
        toilets[index] =
            updatedToilet

        /*
         * 対応するMarkerを取得
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
             * =================================
             * ピンの色を変更
             * =================================
             */
            marker.icon =

                createMarkerIcon(
                    newStatus
                )

            marker.snippet =

                buildDetailText(
                    updatedToilet
                )

            /*
             * 地図へ変更を反映
             */
            map.updateMarker(
                marker
            )
        }

        return updatedToilet
    }

    /*
     * =================================
     * ピンを追加
     * =================================
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

                    /*
                     * 状態によって色を変更
                     */
                    .icon(

                        createMarkerIcon(

                            toilet.cleaningStatus
                        )
                    )
            )

        /*
         * MarkerとToiletを紐づける
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
     * =================================
     * トイレ情報
     * =================================
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
            toilet.comment
                .isBlank()
        ) {

            "清潔度：$stars / 状態：$statusText"

        } else {

            "清潔度：$stars / 状態：$statusText / ${toilet.comment}"
        }
    }

    /*
     * =================================
     * 色付きピンを作成
     * =================================
     *
     * 通常
     * → 青
     *
     * 清掃依頼中
     * → 黄色
     *
     * 清掃済み
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
                    ).toInt()

        val height =

            (
                    56f *
                            density
                    ).toInt()

        /*
         * Bitmapを作成
         */
        val bitmap =

            Bitmap.createBitmap(

                width,

                height,

                Bitmap.Config.ARGB_8888
            )

        val canvas =
            Canvas(bitmap)

        /*
         * 状態によって色を変更
         */
        val pinColor =

            when (
                status
            ) {

                /*
                 * 通常
                 * 青
                 */
                CleaningStatus.NORMAL ->

                    Color.rgb(
                        33,
                        150,
                        243
                    )

                /*
                 * 清掃依頼中
                 * 黄色
                 */
                CleaningStatus.REQUESTED ->

                    Color.rgb(
                        255,
                        193,
                        7
                    )

                /*
                 * 清掃済み
                 * 赤
                 */
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
            18f * density

        val outerRadius =
            14f * density

        /*
         * ピン下部の尖っている部分
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
         * ピン下部
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
         * ピン上部の丸
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

        /*
         * MapLibre用Iconに変換
         */
        return IconFactory

            .getInstance(
                activity
            )

            .fromBitmap(
                bitmap
            )
    }

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