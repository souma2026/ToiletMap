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

    val mapView: MapView

    private var mapLibreMap:
            MapLibreMap? =
        null

    private var isStyleLoaded =
        false

    private var latestToiletsForRendering:
            List<Toilet> =
        emptyList()

    private var latestCurrentLocation:
            LatLng? =
        null

    private val markerIdToToiletId =
        mutableMapOf<Long, String>()

    private var onToiletMarkerClick:
            ((Toilet) -> Unit)? =
        null

    private var onMapReady:
            (() -> Unit)? =
        null

    private var isMapReady =
        false

    private var onVisibleBoundsChanged:
            ((Double, Double, Double, Double) -> Unit)? =
        null


    companion object {

        const val TOKYO_STATION_LATITUDE =
            35.681236

        const val TOKYO_STATION_LONGITUDE =
            139.767125

        const val DEFAULT_ZOOM =
            15.0


        /*
         * Last Known Location は
         * 5分以内のものだけ再利用する。
         */
        private const val
                MAX_LAST_KNOWN_LOCATION_AGE_MS =
            5 * 60 * 1000L


        /*
         * 誤差半径200m以内だけ再利用する。
         */
        private const val
                MAX_LAST_KNOWN_LOCATION_ACCURACY_METERS =
            200f
    }


    init {

        MapLibre.getInstance(
            activity
        )


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


        mapView =

            MapView(
                activity
            )


        mapView
            .onCreate(
                savedInstanceState
            )


        setupMap()


        activity
            .lifecycle
            .addObserver(
                this
            )
    }


    fun setOnToiletMarkerClickListener(

        listener:
        ((Toilet) -> Unit)?

    ) {

        onToiletMarkerClick =
            listener
    }


    fun setOnMapReadyListener(
        listener: (() -> Unit)?
    ) {

        onMapReady =
            listener


        if (
            listener != null &&
            isMapReady
        ) {

            mapView.post {

                listener()
            }
        }
    }


    fun setOnVisibleBoundsChangedListener(

        listener:
        ((Double, Double, Double, Double) -> Unit)?

    ) {

        onVisibleBoundsChanged =
            listener


        if (
            listener != null &&
            isMapReady
        ) {

            mapView.post {

                notifyVisibleBoundsChanged()
            }
        }
    }


    private fun setupMap() {

        mapView.getMapAsync {
                map ->


            mapLibreMap =
                map


            map.addOnCameraIdleListener {

                notifyVisibleBoundsChanged()
            }


            map.setOnMarkerClickListener {
                    marker ->


                val toiletId =

                    markerIdToToiletId[
                        marker.id
                    ]


                val toilet =

                    latestToiletsForRendering
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


                    true

                } else {

                    false
                }
            }


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


                focusOnTokyoStation()


                renderLatestToilets()


                isMapReady =
                    true


                onMapReady
                    ?.invoke()
            }
        }
    }


    private fun notifyVisibleBoundsChanged() {

        if (
            !isMapReady ||
            !isStyleLoaded
        ) {

            return
        }


        val listener =

            onVisibleBoundsChanged
                ?: return


        val map =

            mapLibreMap
                ?: return


        val bounds =

            map.projection
                .visibleRegion
                .latLngBounds


        val south =
            bounds.latitudeSouth

        val north =
            bounds.latitudeNorth

        val west =
            bounds.longitudeWest

        val east =
            bounds.longitudeEast


        if (
            !south.isFinite() ||
            !north.isFinite() ||
            !west.isFinite() ||
            !east.isFinite() ||
            south >= north ||
            west >= east
        ) {

            return
        }


        listener(
            south,
            north,
            west,
            east
        )
    }


    fun focusOnTokyoStation() {

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

                        TOKYO_STATION_LATITUDE,

                        TOKYO_STATION_LONGITUDE
                    )
                )
                .zoom(
                    DEFAULT_ZOOM
                )
                .build()
    }


    fun showToilets(
        toilets: List<Toilet>
    ) {

        latestToiletsForRendering =
            toilets


        renderLatestToilets()
    }


    private fun renderLatestToilets() {

        val map =

            mapLibreMap
                ?: return


        if (
            !isStyleLoaded
        ) {

            return
        }


        map.clear()


        markerIdToToiletId
            .clear()


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


        latestCurrentLocation
            ?.let {
                    currentLocation ->


                addCurrentLocationMarker(

                    map =
                        map,

                    position =
                        currentLocation
                )
            }
    }


    /*
     * =====================================
     * Last Known Locationが使用可能か
     * =====================================
     */
    private fun isUsableLastKnownLocation(
        location: Location
    ): Boolean {

        val locationTime =
            location.time


        /*
         * 時刻が入っていない。
         */
        if (
            locationTime <= 0L
        ) {

            return false
        }


        val ageMillis =

            System.currentTimeMillis() -
                    locationTime


        /*
         * 未来時刻または5分以上前。
         */
        if (
            ageMillis < 0L ||
            ageMillis >
            MAX_LAST_KNOWN_LOCATION_AGE_MS
        ) {

            return false
        }


        /*
         * 精度情報なし、
         * または誤差200m超。
         */
        if (
            !location.hasAccuracy() ||
            !location.accuracy.isFinite() ||
            location.accuracy >
            MAX_LAST_KNOWN_LOCATION_ACCURACY_METERS
        ) {

            return false
        }


        val latitude =
            location.latitude


        val longitude =
            location.longitude


        /*
         * 不正な緯度経度。
         */
        if (
            !latitude.isFinite() ||
            !longitude.isFinite() ||
            latitude !in -90.0..90.0 ||
            longitude !in -180.0..180.0
        ) {

            return false
        }


        return true
    }


    @SuppressLint("MissingPermission")
    fun showCurrentLocation(

        onSuccess:
            () -> Unit = {},

        onError:
            (String) -> Unit = {}

    ) {

        val fineGranted =

            ContextCompat.checkSelfPermission(

                activity,

                Manifest.permission
                    .ACCESS_FINE_LOCATION

            ) == PackageManager.PERMISSION_GRANTED


        val coarseGranted =

            ContextCompat.checkSelfPermission(

                activity,

                Manifest.permission
                    .ACCESS_COARSE_LOCATION

            ) == PackageManager.PERMISSION_GRANTED


        if (
            !fineGranted &&
            !coarseGranted
        ) {

            onError(
                "現在地を表示するには位置情報の許可が必要です"
            )


            return
        }


        val locationManager =

            activity.getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager


        val enabledProviders =

            locationManager
                .getProviders(
                    true
                )


        /*
         * =====================================
         * 保存済み位置を確認
         * =====================================
         *
         * 取得済みの中で
         *
         * ・5分以内
         * ・精度200m以内
         *
         * の位置だけ候補にする。
         */
        val lastKnownLocation =

            enabledProviders
                .mapNotNull {
                        provider ->


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
                .filter {
                        location ->


                    isUsableLastKnownLocation(
                        location
                    )
                }
                .maxByOrNull {

                    it.time
                }


        /*
         * 使用できる保存済み位置があれば使用。
         */
        if (
            lastKnownLocation != null
        ) {

            showLocationOnMap(

                latitude =
                    lastKnownLocation.latitude,

                longitude =
                    lastKnownLocation.longitude
            )


            onSuccess()


            return
        }


        /*
         * =====================================
         * 保存済み位置が使えない
         * =====================================
         *
         * 新しい現在地を取得する。
         */
        val provider =

            when {

                fineGranted &&
                        locationManager
                            .isProviderEnabled(
                                LocationManager.GPS_PROVIDER
                            ) ->

                    LocationManager.GPS_PROVIDER


                locationManager
                    .isProviderEnabled(
                        LocationManager.NETWORK_PROVIDER
                    ) ->

                    LocationManager.NETWORK_PROVIDER


                else ->

                    enabledProviders
                        .firstOrNull()
            }


        if (
            provider == null
        ) {

            onError(
                "端末の位置情報がOFFです。位置情報をONにしてからもう一度お試しください"
            )


            return
        }


        fun handleLocation(
            location: Location?
        ) {

            if (
                location == null
            ) {

                onError(
                    "現在位置を取得できませんでした。位置情報をONにして再度お試しください"
                )


                return
            }


            showLocationOnMap(

                latitude =
                    location.latitude,

                longitude =
                    location.longitude
            )


            onSuccess()
        }


        /*
         * Android 11以降。
         */
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.R
        ) {

            locationManager
                .getCurrentLocation(

                    provider,

                    null,

                    activity.mainExecutor

                ) {
                        location ->


                    handleLocation(
                        location
                    )
                }


        } else {

            /*
             * Android 10以前。
             */
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
            locationManager
                .requestSingleUpdate(

                    provider,

                    listener,

                    Looper.getMainLooper()
                )
        }
    }


    private fun showLocationOnMap(

        latitude: Double,

        longitude: Double

    ) {

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


        renderLatestToilets()


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


    private fun addCurrentLocationMarker(

        map:
        MapLibreMap,

        position:
        LatLng

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


    private fun createCurrentLocationIcon():
            Icon {

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


    fun focusOnToilet(

        toilet:
        Toilet,

        zoom:
        Double = 16.0

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


        markerIdToToiletId[
            marker.id
        ] =
            toilet.id
    }


    private fun buildDetailText(
        toilet: Toilet
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


                CleaningStatus.IN_PROGRESS ->
                    "清掃中"


                CleaningStatus.COMPLETED ->
                    "清掃完了"
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


        val pinColor =

            when (
                status
            ) {

                CleaningStatus.NORMAL ->

                    Color.rgb(
                        244,
                        67,
                        54
                    )


                CleaningStatus.REQUESTED ->

                    Color.rgb(
                        255,
                        193,
                        7
                    )


                CleaningStatus.IN_PROGRESS ->

                    Color.rgb(
                        33,
                        150,
                        243
                    )


                CleaningStatus.COMPLETED ->

                    Color.rgb(
                        11,
                        131,
                        119
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

            width /
                    2f


        val circleCenterY =

            18f *
                    density


        val outerRadius =

            14f *
                    density


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


        canvas.drawPath(

            pointPath,

            fillPaint
        )


        canvas.drawPath(

            pointPath,

            strokePaint
        )


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


    fun onSaveInstanceState(
        outState: Bundle
    ) {

        mapView
            .onSaveInstanceState(
                outState
            )
    }


    fun onLowMemory() {

        mapView
            .onLowMemory()
    }
}