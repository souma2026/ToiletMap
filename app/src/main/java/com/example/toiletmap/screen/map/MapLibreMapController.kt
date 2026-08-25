package com.example.toiletmap.screen.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
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
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.LocationComponentOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
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
     * 読み込み済みStyle
     * =====================================
     */
    private var loadedStyle:
            Style? =
        null


    /*
     * =====================================
     * 現在地表示を有効化する予約
     * =====================================
     */
    private var shouldEnableUserLocation =
        false


    /*
     * =====================================
     * 現在地へ移動する予約
     * =====================================
     */
    private var shouldFocusUserLocation =
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

            ) { style ->

                /*
                 * スタイル読み込み完了
                 */
                isStyleLoaded =
                    true


                loadedStyle =
                    style


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


                /*
                 * 権限取得がStyle読み込みより先に終わっていた場合、
                 * Style読み込み後に現在地表示を有効化する。
                 */
                if (
                    shouldEnableUserLocation
                ) {

                    activateUserLocation(
                        style
                    )
                }
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
     * 現在地表示を有効化
     * =====================================
     *
     * focus = false
     * → 青い現在地だけ表示
     *
     * focus = true
     * → 青い現在地を表示し、現在地へカメラ移動
     */
    fun enableUserLocation(
        focus: Boolean = false
    ) {

        if (
            !hasLocationPermission()
        ) {
            return
        }

        shouldEnableUserLocation =
            true

        if (
            focus
        ) {
            shouldFocusUserLocation =
                true
        }

        val style =
            loadedStyle

        if (
            isStyleLoaded &&
            style != null
        ) {
            activateUserLocation(
                style
            )
        }
    }


    /*
     * =====================================
     * MapLibre LocationComponentを有効化
     * =====================================
     */
    @SuppressLint("MissingPermission")
    private fun activateUserLocation(
        style: Style
    ) {

        if (
            !hasLocationPermission()
        ) {
            return
        }

        val map =
            mapLibreMap
                ?: return

        val locationComponent =
            map.locationComponent

        if (
            !locationComponent.isLocationComponentActivated
        ) {

            val googleBlue =
                Color.rgb(
                    66,
                    133,
                    244
                )

            val locationOptions =
                LocationComponentOptions
                    .builder(
                        activity
                    )
                    .foregroundTintColor(
                        googleBlue
                    )
                    .backgroundTintColor(
                        Color.WHITE
                    )
                    // Google Maps風に、現在地の周囲が広がるパルス表示は使わない。
                    .pulseEnabled(
                        false
                    )
                    // COMPASSモードで表示される方向矢印をGoogle系の青色にする。
                    .bearingTintColor(
                        googleBlue
                    )
                    // 端末を回したときに矢印が滑らかに回転するようにする。
                    .compassAnimationEnabled(
                        true
                    )
                    .build()

            val activationOptions =
                LocationComponentActivationOptions
                    .builder(
                        activity,
                        style
                    )
                    .locationComponentOptions(
                        locationOptions
                    )
                    .useDefaultLocationEngine(
                        true
                    )
                    .build()

            locationComponent
                .activateLocationComponent(
                    activationOptions
                )
        }

        locationComponent
            .isLocationComponentEnabled =
            true

        // 現在地を単なる点ではなく、端末の向いている方向を示す矢印で表示する。
        locationComponent
            .renderMode =
            RenderMode.COMPASS

        if (
            shouldFocusUserLocation
        ) {

            locationComponent
                .cameraMode =
                CameraMode.TRACKING

            val lastLocation =
                locationComponent
                    .lastKnownLocation

            if (
                lastLocation != null
            ) {
                map.animateCamera(
                    CameraUpdateFactory
                        .newLatLngZoom(
                            LatLng(
                                lastLocation.latitude,
                                lastLocation.longitude
                            ),
                            16.0
                        )
                )
            }

            shouldFocusUserLocation =
                false
        }
    }


    /*
     * =====================================
     * 最近の現在地を取得できているか
     * =====================================
     */
    fun hasRecentUserLocation(
        maxAgeMillis: Long = 120_000L
    ): Boolean {

        val map =
            mapLibreMap
                ?: return false

        val locationComponent =
            map.locationComponent

        if (
            !locationComponent.isLocationComponentActivated ||
            !locationComponent.isLocationComponentEnabled
        ) {
            return false
        }

        val location =
            locationComponent
                .lastKnownLocation
                ?: return false

        val locationTime =
            location.time

        if (
            locationTime <= 0L
        ) {
            return false
        }

        val ageMillis =
            System.currentTimeMillis() -
                    locationTime

        return ageMillis in
                0L..maxAgeMillis
    }


    /*
     * =====================================
     * 位置情報権限確認
     * =====================================
     */
    private fun hasLocationPermission(): Boolean {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return fineGranted ||
                coarseGranted
    }


    /*
     * =====================================
     * 指定したトイレへカメラ移動
     * =====================================
     *
     * zoomを指定できる。
     *
     * 指定しなかった場合は
     * 今まで通り16.0。
     * =====================================
     */
    fun focusOnToilet(

        toilet: Toilet,

        zoom: Double = 16.0

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
                    zoom
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