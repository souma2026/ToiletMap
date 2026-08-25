package com.example.toiletmap.screen.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
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
     * 現在地
     * =====================================
     *
     * トイレ一覧の再描画でmap.clear()が呼ばれても
     * 現在地マーカーを復元できるように保持する。
     */
    private var latestCurrentLocation:
            LatLng? =
        null


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


        /*
         * 現在地を取得済みなら
         * トイレピンの再描画後にも表示する。
         */
        latestCurrentLocation
            ?.let { currentLocation ->

                addCurrentLocationMarker(
                    map = map,
                    position = currentLocation
                )
            }
    }


    /*
     * =====================================
     * 現在位置を取得して表示
     * =====================================
     *
     * MainActivity側で位置情報権限を確認した後に
     * 呼び出す。
     */
    @SuppressLint("MissingPermission")
    fun showCurrentLocation(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {

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


        if (!fineGranted && !coarseGranted) {

            onError(
                "現在地を表示するには位置情報の許可が必要です"
            )

            return
        }


        val locationManager =
            activity.getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager


        /*
         * 未清掃画面と同じ取得方法。
         * 有効な全プロバイダから最後に取得された位置を集め、
         * 一番新しい位置を現在地として使用する。
         */
        val enabledProviders =
            locationManager.getProviders(true)


        val lastKnownLocation =
            enabledProviders
                .mapNotNull { provider ->

                    try {

                        locationManager
                            .getLastKnownLocation(
                                provider
                            )

                    } catch (
                        e: SecurityException
                    ) {

                        null
                    }
                }
                .maxByOrNull {
                    it.time
                }


        if (lastKnownLocation != null) {

            showLocationOnMap(
                latitude = lastKnownLocation.latitude,
                longitude = lastKnownLocation.longitude
            )

            onSuccess()

            return
        }


        /*
         * 保存済み位置がまだ無い場合だけ、
         * 新しい位置を1回取得する。
         */
        val provider =
            when {

                fineGranted &&
                        locationManager.isProviderEnabled(
                            LocationManager.GPS_PROVIDER
                        ) ->
                    LocationManager.GPS_PROVIDER

                locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                ) ->
                    LocationManager.NETWORK_PROVIDER

                else ->
                    enabledProviders.firstOrNull()
            }


        if (provider == null) {

            onError(
                "端末の位置情報がOFFです。位置情報をONにしてからもう一度お試しください"
            )

            return
        }


        fun handleLocation(
            location: Location?
        ) {

            if (location == null) {

                onError(
                    "現在位置を取得できませんでした。位置情報をONにして再度お試しください"
                )

                return
            }


            showLocationOnMap(
                latitude = location.latitude,
                longitude = location.longitude
            )

            onSuccess()
        }


        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.R
        ) {

            locationManager.getCurrentLocation(
                provider,
                null,
                activity.mainExecutor
            ) { location ->

                handleLocation(
                    location
                )
            }

        } else {

            val listener =
                object : LocationListener {

                    override fun onLocationChanged(
                        location: Location
                    ) {

                        locationManager
                            .removeUpdates(
                                this
                            )

                        handleLocation(
                            location
                        )
                    }

                    override fun onProviderDisabled(
                        provider: String
                    ) {

                        locationManager
                            .removeUpdates(
                                this
                            )

                        onError(
                            "位置情報がOFFになりました"
                        )
                    }
                }


            @Suppress("DEPRECATION")
            locationManager.requestSingleUpdate(
                provider,
                listener,
                Looper.getMainLooper()
            )
        }
    }


    /*
     * =====================================
     * 現在地を地図へ反映
     * =====================================
     */
    private fun showLocationOnMap(
        latitude: Double,
        longitude: Double
    ) {

        /*
         * 地図のStyle読み込み前でも位置を保持しておく。
         * Style読み込み完了後のrenderLatestToilets()で
         * 現在地マーカーを確実に復元できる。
         */
        latestCurrentLocation =
            LatLng(
                latitude,
                longitude
            )


        val map =
            mapLibreMap
                ?: return


        if (
            !isStyleLoaded
        ) {

            return
        }


        /*
         * トイレピン + 現在地をまとめて再描画
         */
        renderLatestToilets()


        /*
         * 現在地へカメラ移動
         */
        map.cameraPosition =
            CameraPosition
                .Builder()
                .target(
                    LatLng(
                        latitude,
                        longitude
                    )
                )
                .zoom(
                    16.5
                )
                .build()
    }


    /*
     * =====================================
     * 現在地マーカーを追加
     * =====================================
     */
    private fun addCurrentLocationMarker(
        map: MapLibreMap,
        position: LatLng
    ) {

        map.addMarker(
            MarkerOptions()
                .position(
                    position
                )
                .title(
                    "現在地"
                )
                .icon(
                    createCurrentLocationIcon()
                )
        )
    }


    /*
     * =====================================
     * 現在地マーカー画像
     * =====================================
     *
     * 青い円 + 白い縁で、
     * トイレピンと区別しやすくする。
     */
    private fun createCurrentLocationIcon(): Icon {

        val density =
            activity
                .resources
                .displayMetrics
                .density


        val size =
            (
                    28f *
                            density
                    ).toInt()


        val bitmap =
            Bitmap.createBitmap(
                size,
                size,
                Bitmap.Config.ARGB_8888
            )


        val canvas =
            Canvas(
                bitmap
            )


        val center =
            size /
                    2f


        val whitePaint =
            Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {

                color =
                    Color.WHITE

                style =
                    Paint.Style.FILL
            }


        val bluePaint =
            Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {

                color =
                    Color.rgb(
                        33,
                        150,
                        243
                    )

                style =
                    Paint.Style.FILL
            }


        canvas.drawCircle(
            center,
            center,
            13f * density,
            whitePaint
        )


        canvas.drawCircle(
            center,
            center,
            9f * density,
            bluePaint
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

                    "清掃依頼中"

                CleaningStatus.IN_PROGRESS ->

                    "清掃中"

                CleaningStatus.COMPLETED ->

                    "清掃完了"
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
     *
     * IN_PROGRESS
     * → 青
     *
     * COMPLETED
     * → 緑
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


                /*
                 * 清掃中
                 * → 青
                 */
                CleaningStatus.IN_PROGRESS ->

                    Color.rgb(
                        33,
                        150,
                        243
                    )


                /*
                 * 清掃完了
                 * → 緑
                 */
                CleaningStatus.COMPLETED ->

                    Color.rgb(
                        11,
                        131,
                        119
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