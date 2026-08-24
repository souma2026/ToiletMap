package com.example.toiletmap.screen.listofuncleaned

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt


/*
 * =====================================
 * 現在地の状態
 * =====================================
 */
data class CurrentLocationState(

    val location: Location?,

    val hasPermission: Boolean
)


/*
 * =====================================
 * 現在地を取得する
 * =====================================
 */
@Composable
fun rememberCurrentLocationState():
        CurrentLocationState {

    val context =
        LocalContext.current


    var currentLocation by
    remember {

        mutableStateOf<Location?>(
            null
        )
    }


    var hasPermission by
    remember {

        mutableStateOf(
            hasLocationPermission(
                context
            )
        )
    }


    /*
     * 位置情報権限を要求
     */
    val permissionLauncher =

        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts
                    .RequestMultiplePermissions()

        ) { permissions ->


            hasPermission =

                permissions[
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ] == true ||

                        permissions[
                            Manifest.permission
                                .ACCESS_COARSE_LOCATION
                        ] == true


            if (
                hasPermission
            ) {

                currentLocation =
                    getLastKnownLocation(
                        context
                    )
            }
        }


    /*
     * 画面を開いたとき
     */
    LaunchedEffect(Unit) {

        if (
            hasPermission
        ) {

            currentLocation =
                getLastKnownLocation(
                    context
                )

        } else {

            permissionLauncher.launch(

                arrayOf(

                    Manifest.permission
                        .ACCESS_FINE_LOCATION,

                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    return CurrentLocationState(

        location =
            currentLocation,

        hasPermission =
            hasPermission
    )
}


/*
 * =====================================
 * 位置情報権限確認
 * =====================================
 */
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


/*
 * =====================================
 * 最後に取得された現在地
 * =====================================
 */
@SuppressLint("MissingPermission")
private fun getLastKnownLocation(
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
            .getProviders(true)
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


    /*
     * 一番新しい位置情報を使用
     */
    return locations
        .maxByOrNull {

            it.time
        }
}


/*
 * =====================================
 * 現在地 → トイレの距離
 * =====================================
 */
fun calculateDistance(
    currentLocation: Location,
    toilet: UncleanedToilet
): Float {

    val result =
        FloatArray(1)


    Location.distanceBetween(

        currentLocation.latitude,

        currentLocation.longitude,

        toilet.latitude,

        toilet.longitude,

        result
    )


    return result[0]
}


/*
 * =====================================
 * 距離表示
 *
 * 例
 *
 * 350 m
 * 1.2 km
 * =====================================
 */
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


/*
 * =====================================
 * 前回清掃からの経過時間
 * =====================================
 */
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
            .coerceAtLeast(0)


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

        minutes / 60


    if (
        hours < 24
    ) {

        return "${hours}時間前"
    }


    val days =

        hours / 24


    return "${days}日前"
}