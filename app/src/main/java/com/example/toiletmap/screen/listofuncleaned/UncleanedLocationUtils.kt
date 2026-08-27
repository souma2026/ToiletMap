package com.example.toiletmap.screen.listofuncleaned

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt


data class CurrentLocationState(

    val location: Location?,

    val hasPermission: Boolean
)


/*
 * Last Known Location は
 *
 * ・5分以内
 * ・精度200m以内
 *
 * の場合だけ再利用する。
 */
private const val
        MAX_LAST_KNOWN_LOCATION_AGE_MS =
    5 * 60 * 1000L


private const val
        MAX_LAST_KNOWN_LOCATION_ACCURACY_METERS =
    200f


@Composable
fun rememberCurrentLocationState():
        CurrentLocationState {

    val context =
        LocalContext.current


    var hasPermission by
    remember {

        mutableStateOf(
            hasLocationPermission(
                context
            )
        )
    }


    var currentLocation by
    remember {

        mutableStateOf<Location?>(
            null
        )
    }


    /*
     * ここでは権限要求をしない。
     */
    LaunchedEffect(Unit) {

        hasPermission =

            hasLocationPermission(
                context
            )


        if (
            !hasPermission
        ) {

            currentLocation =
                null


            return@LaunchedEffect
        }


        /*
         * まず保存済み位置を確認。
         */
        val lastKnownLocation =

            getUsableLastKnownLocation(
                context
            )


        if (
            lastKnownLocation != null
        ) {

            currentLocation =
                lastKnownLocation


            return@LaunchedEffect
        }


        /*
         * 保存済み位置が
         *
         * ・ない
         * ・古い
         * ・精度が悪い
         *
         * 場合はfresh locationを取得。
         *
         * Permissionダイアログは出さない。
         */
        requestFreshLocation(

            context =
                context,

            onResult = {
                    location ->


                currentLocation =
                    location
            }
        )
    }


    return CurrentLocationState(

        location =
            currentLocation,

        hasPermission =
            hasPermission
    )
}


private fun hasLocationPermission(
    context: Context
): Boolean {

    val fine =

        ContextCompat.checkSelfPermission(

            context,

            Manifest.permission
                .ACCESS_FINE_LOCATION

        ) == PackageManager.PERMISSION_GRANTED


    val coarse =

        ContextCompat.checkSelfPermission(

            context,

            Manifest.permission
                .ACCESS_COARSE_LOCATION

        ) == PackageManager.PERMISSION_GRANTED


    return fine || coarse
}


private fun hasFineLocationPermission(
    context: Context
): Boolean {

    return ContextCompat.checkSelfPermission(

        context,

        Manifest.permission
            .ACCESS_FINE_LOCATION

    ) == PackageManager.PERMISSION_GRANTED
}


/*
 * =====================================
 * 保存済み位置を使用できるか
 * =====================================
 */
private fun isUsableLastKnownLocation(
    location: Location
): Boolean {

    if (
        location.time <= 0L
    ) {

        return false
    }


    val ageMillis =

        System.currentTimeMillis() -
                location.time


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
     * または200mを超える。
     */
    if (
        !location.hasAccuracy() ||
        !location.accuracy.isFinite() ||
        location.accuracy >
        MAX_LAST_KNOWN_LOCATION_ACCURACY_METERS
    ) {

        return false
    }


    return isValidLocationCoordinate(
        location
    )
}


/*
 * =====================================
 * 緯度経度が正常か
 * =====================================
 */
private fun isValidLocationCoordinate(
    location: Location
): Boolean {

    val latitude =
        location.latitude


    val longitude =
        location.longitude


    return latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0
}


/*
 * =====================================
 * 使用できる保存済み位置を取得
 * =====================================
 */
@SuppressLint("MissingPermission")
private fun getUsableLastKnownLocation(
    context: Context
): Location? {

    if (
        !hasLocationPermission(
            context
        )
    ) {

        return null
    }


    val locationManager =

        context.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager


    val locations =

        locationManager
            .getProviders(
                true
            )
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


    return locations
        .maxByOrNull {

            it.time
        }
}


/*
 * =====================================
 * ProviderがONか確認
 * =====================================
 */
private fun isProviderEnabledSafely(

    locationManager:
    LocationManager,

    provider:
    String

): Boolean {

    return try {

        locationManager
            .isProviderEnabled(
                provider
            )

    } catch (
        e: Exception
    ) {

        false
    }
}


/*
 * =====================================
 * 新しい現在地を1回取得
 * =====================================
 *
 * ここでも権限要求は行わない。
 */
@SuppressLint("MissingPermission")
private fun requestFreshLocation(

    context:
    Context,

    onResult:
        (Location?) -> Unit

) {

    if (
        !hasLocationPermission(
            context
        )
    ) {

        onResult(
            null
        )


        return
    }


    val locationManager =

        context.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager


    val enabledProviders =

        try {

            locationManager
                .getProviders(
                    true
                )

        } catch (
            e: Exception
        ) {

            emptyList()
        }


    val provider =

        when {

            hasFineLocationPermission(
                context
            ) &&
                    isProviderEnabledSafely(

                        locationManager,

                        LocationManager.GPS_PROVIDER
                    ) ->

                LocationManager.GPS_PROVIDER


            isProviderEnabledSafely(

                locationManager,

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

        onResult(
            null
        )


        return
    }


    fun deliver(
        location: Location?
    ) {

        val validLocation =

            location
                ?.takeIf {

                    isValidLocationCoordinate(
                        it
                    )
                }


        onResult(
            validLocation
        )
    }


    try {

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

                    context.mainExecutor

                ) {
                        location ->


                    deliver(
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


                        deliver(
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


                        deliver(
                            null
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


    } catch (
        e: SecurityException
    ) {

        onResult(
            null
        )


    } catch (
        e: Exception
    ) {

        onResult(
            null
        )
    }
}


fun calculateDistance(

    currentLocation:
    Location,

    toilet:
    UncleanedToilet

): Float {

    val result =
        FloatArray(
            1
        )


    Location.distanceBetween(

        currentLocation.latitude,

        currentLocation.longitude,

        toilet.latitude,

        toilet.longitude,

        result
    )


    return result[0]
}


fun formatDistance(
    distanceMeters: Float
): String {

    return if (
        distanceMeters < 1000
    ) {

        "${distanceMeters.roundToInt()} m"

    } else {

        String.format(

            "%.1f km",

            distanceMeters / 1000f
        )
    }
}


fun formatElapsedSinceCleaning(
    lastCleanedAtMillis: Long?
): String {

    if (
        lastCleanedAtMillis == null
    ) {

        return "記録なし"
    }


    val elapsedMillis =

        (
                System.currentTimeMillis() -
                        lastCleanedAtMillis
                )
            .coerceAtLeast(
                0
            )


    val minutes =

        elapsedMillis /
                60_000


    if (
        minutes < 1
    ) {

        return "1分未満"
    }


    if (
        minutes < 60
    ) {

        return "${minutes}分前"
    }


    val hours =

        minutes /
                60


    if (
        hours < 24
    ) {

        return "${hours}時間前"
    }


    val days =

        hours /
                24


    return "${days}日前"
}