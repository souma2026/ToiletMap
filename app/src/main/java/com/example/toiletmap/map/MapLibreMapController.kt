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
import org.maplibre.android.annotations.Marker
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
     * MapLibre
     * =====================================
     */

    val mapView:
            MapView


    private var mapLibreMap:
            MapLibreMap? =
        null


    private var isStyleLoaded =
        false



    /*
     * Marker ID
     * ↓
     * Toilet ID
     */

    private val markerIdToToiletId =

        mutableMapOf<
                Long,
                String
                >()



    /*
     * Toilet ID
     * ↓
     * Marker
     */

    private val toiletIdToMarker =

        mutableMapOf<
                String,
                Marker
                >()


    /*
     * ピンを押したとき
     */

    private var onToiletMarkerClick:
            ((Toilet) -> Unit)? =
        null



    /*
     * =====================================
     * 描画用の最新データ
     * =====================================
     *
     * 現在はメモリ上のみ
     */

    private val toilets =

        mutableListOf(


            /*
             * 動作確認用トイレ
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
                    "東京駅の近くにあるトイレです",

                /*
                 * 初期状態は赤
                 */
                cleaningStatus =
                    CleaningStatus.NORMAL,

                /*
                 * 動作確認しやすいよう
                 * 前回の清掃を2時間前にしている
                 */
                lastCleanedAtMillis =

                    System.currentTimeMillis() -

                            (
                                    2L *
                                            60L *
                                            60L *
                                            1000L
                                    )
            )
        )


    /*
     * =====================================
     * 初期化
     * =====================================
     */

    init {


        /*
         * MapLibre
         */

        MapLibre.getInstance(
            activity
        )


        /*
         * =====================================
         * OpenStreetMap
         * 403対策
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
         * MapView
         */

        mapView =

            MapView(
                activity
            )


        mapView.onCreate(
            savedInstanceState
        )


        /*
         * 地図準備
         */

        setupMap()


        /*
         * ライフサイクル
         */

        activity
            .lifecycle
            .addObserver(
                this
            )
    }


    /*
     * =====================================
     * トイレピンタップ
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

    @Suppress(
        "DEPRECATION"
    )
    private fun setupMap() {


        mapView.getMapAsync {
                map ->


            mapLibreMap =
                map


            /*
             * =====================================
             * ピンを押した
             * =====================================
             */

            map.setOnMarkerClickListener {
                    marker ->


                val toiletId =

                    markerIdToToiletId[
                        marker.id
                    ]


                val toilet =

                    toilets
                        .firstOrNull {

                            it.id ==
                                    toiletId
                        }


                if (
                    toilet != null
                ) {


                    onToiletMarkerClick
                        ?.invoke(
                            toilet
                        )


                    /*
                     * MapLibre標準の
                     * 吹き出しは表示しない
                     */

                    true

                } else {

                    false
                }
            }


            /*
             * =====================================
             * OSMラスタータイル
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
                 * トイレを全部表示
                 */

                toilets.forEach {
                        toilet ->


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
     * 新しいトイレを登録
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
                ?: return



        if (
            map != null &&
            isStyleLoaded
        ) {


            /*
             * ピン追加
             */

            addMarkerToMap(

                map =
                    map,

                toilet =
                    toilet
            )


            /*
             * 登録地点へ移動
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
     * 清掃依頼
     *
     * 赤
     * NORMAL
     *
     * ↓
     *
     * 黄色
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
         * NORMALの時だけ
         * 清掃依頼できる
         */

        if (
            toilet.cleaningStatus !=
            CleaningStatus.NORMAL
        ) {

            return toilet
        }


        return updateToilet(

            toiletId =
                toiletId,

            cleaningStatus =
                CleaningStatus.REQUESTED,

            /*
             * 清掃依頼しても
             * 前回清掃時間は変えない
             */
            lastCleanedAtMillis =
                toilet.lastCleanedAtMillis
        )
    }


    /*
     * =====================================
     * 清掃しました
     *
     * 黄色
     * REQUESTED
     *
     * ↓
     *
     * 赤
     * NORMAL
     *
     * ＋
     *
     * 現在時刻を
     * 前回清掃完了時間として保存
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


        /*
         * 清掃待ち以外なら
         * 処理しない
         */

        if (
            toilet.cleaningStatus !=
            CleaningStatus.REQUESTED
        ) {

            return toilet
        }


        return updateToilet(

            toiletId =
                toiletId,

            /*
             * 清掃完了したら
             * 通常状態へ戻す
             */
            cleaningStatus =
                CleaningStatus.NORMAL,

            /*
             * 今の時間を保存
             */
            lastCleanedAtMillis =
                System.currentTimeMillis()
        )
    }


    /*
     * =====================================
     * トイレ状態更新
     * =====================================
     */

    @Suppress(
        "DEPRECATION"
    )
    private fun updateToilet(

        toiletId:
        String,

        cleaningStatus:
        CleaningStatus,

        lastCleanedAtMillis:
        Long?

    ): Toilet? {


        /*
         * 対象を探す
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
         * 新しい状態を作る
         */

        val updatedToilet =

            toilets[
                index
            ]
                .copy(

                    cleaningStatus =
                        cleaningStatus,

                    lastCleanedAtMillis =
                        lastCleanedAtMillis
                )


        /*
         * 一覧更新
         */

        toilets[
            index
        ] =
            updatedToilet


        /*
         * 地図のピンも更新
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
             * 色変更
             */

            marker.icon =

                createMarkerIcon(
                    cleaningStatus
                )


            /*
             * 情報更新
             */

            marker.snippet =

                buildDetailText(
                    updatedToilet
                )


            /*
             * MapLibreへ反映
             */

            map.updateMarker(
                marker
            )
        }


        return updatedToilet
    }


    /*
     * =====================================
     * ピン追加
     * =====================================
     */

    @Suppress(
        "DEPRECATION"
    )
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
         * MarkerとToiletを関連付ける
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
     * トイレ情報
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

                    "清掃待ち"
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
     * =====================================
     * ピン画像作成
     * =====================================
     *
     * NORMAL
     * → 赤
     *
     * REQUESTED
     * → 黄色
     * =====================================
     */

    @Suppress(
        "DEPRECATION"
    )
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

                    Bitmap.Config
                        .ARGB_8888
                )


        val canvas =

            Canvas(
                bitmap
            )


        /*
         * =====================================
         * 状態別ピン色
         * =====================================
         */

        val pinColor =

            when (
                status
            ) {


                /*
                 * 通常
                 * 赤
                 */

                CleaningStatus.NORMAL ->

                    Color.rgb(
                        244,
                        67,
                        54
                    )


                /*
                 * 清掃待ち
                 * 黄色
                 */

                CleaningStatus.REQUESTED ->

                    Color.rgb(
                        255,
                        193,
                        7
                    )
            }


        /*
         * 塗り
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
         * 中央白丸
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


        /*
         * =====================================
         * ピン描画
         * =====================================
         */

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
         * ピン下側
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
         * 下部分
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
         * 丸部分
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