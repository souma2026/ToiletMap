package com.example.toiletmap.screen.map

import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@Composable
fun MapScreen(

    mapView: MapView,

    // 新しいトイレの場所を選択中か
    isSelectingLocation: Boolean = false,

    // 現在選択されているトイレ
    selectedToilet: Toilet? = null,

    onDismissSelectedToilet: () -> Unit = {},

    onRequestCleaning: (Toilet) -> Unit = {},

    onMarkCleaned: (Toilet) -> Unit = {},

    // 地図をタップした場所
    onLocationSelected:
        (Double, Double) -> Unit =
        { _, _ -> },

    onCancelLocationSelection:
        () -> Unit = {}
) {


    /*
     * =====================================
     * 新しいトイレの場所選択
     * =====================================
     */

    DisposableEffect(

        mapView,
        isSelectingLocation

    ) {

        var disposed =
            false

        var targetMap:
                MapLibreMap? =
            null

        var clickListener:
                MapLibreMap.OnMapClickListener? =
            null


        if (
            isSelectingLocation
        ) {

            mapView.getMapAsync {
                    map ->

                if (
                    !disposed
                ) {

                    targetMap =
                        map


                    val listener =

                        MapLibreMap
                            .OnMapClickListener {
                                    point ->

                                onLocationSelected(

                                    point.latitude,

                                    point.longitude
                                )

                                true
                            }


                    clickListener =
                        listener


                    map.addOnMapClickListener(
                        listener
                    )
                }
            }
        }


        onDispose {

            disposed =
                true


            val map =
                targetMap

            val listener =
                clickListener


            if (
                map != null &&
                listener != null
            ) {

                map.removeOnMapClickListener(
                    listener
                )
            }
        }
    }


    Box(

        modifier =
            Modifier.fillMaxSize()

    ) {


        /*
         * =====================================
         * MapLibre
         * =====================================
         */

        AndroidView(

            factory = {

                /*
                 * 同じMapViewを
                 * Composeで再利用する
                 */
                (
                        mapView.parent
                                as?
                                ViewGroup
                        )
                    ?.removeView(
                        mapView
                    )

                mapView
            },

            modifier =
                Modifier.fillMaxSize()
        )


        /*
         * =====================================
         * トイレ追加用の場所選択中
         * =====================================
         */

        if (
            isSelectingLocation
        ) {

            Card(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            16.dp
                        )
                        .align(
                            Alignment.TopCenter
                        )
            ) {

                Column(

                    modifier =
                        Modifier.padding(
                            16.dp
                        )
                ) {

                    Text(

                        text =
                            "トイレの場所を選択",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium
                    )


                    Text(

                        text =
                            "ピンを置きたい場所を地図上で1回タップしてください。",

                        modifier =
                            Modifier.padding(
                                top = 4.dp
                            )
                    )


                    TextButton(

                        onClick =
                            onCancelLocationSelection

                    ) {

                        Text(
                            "キャンセル"
                        )
                    }
                }
            }
        }


        /*
         * =====================================
         * トイレピンを押した場合
         * =====================================
         */

        if (
            !isSelectingLocation &&
            selectedToilet != null
        ) {

            ToiletCleaningCard(

                toilet =
                    selectedToilet,

                onDismiss =
                    onDismissSelectedToilet,

                onRequestCleaning = {

                    onRequestCleaning(
                        selectedToilet
                    )
                },

                onMarkCleaned = {

                    onMarkCleaned(
                        selectedToilet
                    )
                },

                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .fillMaxWidth()
                        .padding(
                            16.dp
                        )
            )
        }
    }
}


/*
 * =====================================
 * トイレ詳細カード
 * =====================================
 */

@Composable
private fun ToiletCleaningCard(

    toilet: Toilet,

    onDismiss: () -> Unit,

    onRequestCleaning: () -> Unit,

    onMarkCleaned: () -> Unit,

    modifier: Modifier =
        Modifier
) {


    /*
     * 清潔度
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
     * 前回清掃からの経過時間
     */
    val elapsedSinceLastCleaning =

        formatElapsedSinceCleaning(
            toilet.lastCleanedAtMillis
        )


    Card(

        modifier =
            modifier

    ) {


        Column(

            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {


            /*
             * =====================================
             * 名前
             * =====================================
             */

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically

            ) {


                Text(

                    text =
                        toilet.name,

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge
                )


                TextButton(

                    onClick =
                        onDismiss

                ) {

                    Text(
                        "閉じる"
                    )
                }
            }


            /*
             * 清潔度
             */

            Text(
                "清潔度：$stars"
            )


            /*
             * コメント
             */

            if (
                toilet.comment
                    .isNotBlank()
            ) {

                Text(
                    toilet.comment
                )
            }


            /*
             * =====================================
             * 前回の清掃時間
             * =====================================
             */

            Text(

                text =
                    "前回の清掃完了：$elapsedSinceLastCleaning",

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )


            /*
             * =====================================
             * 状態
             * =====================================
             */

            when (
                toilet.cleaningStatus
            ) {


                /*
                 * =============================
                 * 通常
                 * ピンは赤
                 * =============================
                 */

                CleaningStatus.NORMAL -> {


                    Text(
                        "状態：通常"
                    )


                    Button(

                        onClick =
                            onRequestCleaning,

                        modifier =
                            Modifier.fillMaxWidth()

                    ) {

                        Text(
                            "清掃を依頼する"
                        )
                    }
                }


                /*
                 * =============================
                 * 清掃待ち
                 * ピンは黄色
                 * =============================
                 */

                CleaningStatus.REQUESTED -> {


                    Text(
                        "状態：清掃待ち"
                    )


                    Button(

                        onClick =
                            onMarkCleaned,

                        modifier =
                            Modifier.fillMaxWidth()

                    ) {

                        Text(
                            "清掃しました"
                        )
                    }
                }
            }
        }
    }
}


/*
 * =====================================
 * 前回の清掃完了から
 * どれくらい経ったか計算
 * =====================================
 */

private fun formatElapsedSinceCleaning(

    lastCleanedAtMillis:
    Long?

): String {


    /*
     * まだ一度も清掃完了していない
     */

    if (
        lastCleanedAtMillis == null
    ) {

        return "記録なし"
    }


    /*
     * 今の時間との差
     */

    val elapsedMillis =

        (
                System.currentTimeMillis() -
                        lastCleanedAtMillis
                )
            .coerceAtLeast(
                0L
            )


    /*
     * 分
     */

    val totalMinutes =

        elapsedMillis /
                60_000L


    /*
     * 1分未満
     */

    if (
        totalMinutes < 1L
    ) {

        return "1分未満"
    }


    /*
     * 60分未満
     */

    if (
        totalMinutes < 60L
    ) {

        return "${totalMinutes}分前"
    }


    /*
     * 時間
     */

    val totalHours =

        totalMinutes /
                60L


    val remainingMinutes =

        totalMinutes %
                60L


    /*
     * 24時間未満
     */

    if (
        totalHours < 24L
    ) {

        return if (
            remainingMinutes == 0L
        ) {

            "${totalHours}時間前"

        } else {

            "${totalHours}時間${remainingMinutes}分前"
        }
    }


    /*
     * 日数
     */

    val totalDays =

        totalHours /
                24L


    val remainingHours =

        totalHours %
                24L


    return if (
        remainingHours == 0L
    ) {

        "${totalDays}日前"

    } else {

        "${totalDays}日${remainingHours}時間前"
    }
}