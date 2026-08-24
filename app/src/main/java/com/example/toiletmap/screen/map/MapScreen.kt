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

    mapView:
    MapView,

    isSelectingLocation:
    Boolean = false,

    selectedToilet:
    Toilet? = null,

    onDismissSelectedToilet:
        () -> Unit = {},

    onRequestCleaning:
        (Toilet) -> Unit = {},

    onMarkCleaned:
        (Toilet) -> Unit = {},

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

        var clickListener:
                MapLibreMap.OnMapClickListener? =
            null

        var targetMap:
                MapLibreMap? =
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
            Modifier
                .fillMaxSize()

    ) {

        /*
         * =====================================
         * MapLibre地図
         * =====================================
         */
        AndroidView(

            factory = {

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
                Modifier
                    .fillMaxSize()
        )

        /*
         * =====================================
         * 場所選択モード
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
                        Modifier
                            .padding(
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
                            Modifier
                                .padding(
                                    top = 4.dp
                                )
                    )

                    TextButton(

                        onClick =
                            onCancelLocationSelection

                    ) {

                        Text(
                            text =
                                "キャンセル"
                        )
                    }
                }
            }
        }

        /*
         * =====================================
         * 既存トイレを押したとき
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

                /*
                 * 清掃を依頼する
                 */
                onRequestCleaning = {

                    onRequestCleaning(
                        selectedToilet
                    )
                },

                /*
                 * 清掃しました
                 */
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

@Composable
private fun ToiletCleaningCard(

    toilet:
    Toilet,

    onDismiss:
        () -> Unit,

    onRequestCleaning:
        () -> Unit,

    onMarkCleaned:
        () -> Unit,

    modifier:
    Modifier = Modifier

) {

    val stars =

        "★".repeat(
            toilet.cleanliness
        ) +

                "☆".repeat(
                    5 -
                            toilet.cleanliness
                )

    Card(

        modifier =
            modifier

    ) {

        Column(

            modifier =
                Modifier
                    .padding(
                        16.dp
                    ),

            verticalArrangement =
                Arrangement
                    .spacedBy(
                        8.dp
                    )

        ) {

            /*
             * トイレ名
             */
            Row(

                modifier =
                    Modifier
                        .fillMaxWidth(),

                horizontalArrangement =
                    Arrangement
                        .SpaceBetween,

                verticalAlignment =
                    Alignment
                        .CenterVertically

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
                        text =
                            "閉じる"
                    )
                }
            }

            /*
             * 清潔度
             */
            Text(

                text =
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

                    text =
                        toilet.comment
                )
            }

            /*
             * =====================================
             * 状態によってボタンを変更
             * =====================================
             */
            when (
                toilet.cleaningStatus
            ) {

                /*
                 * =================================
                 * 通常
                 * =================================
                 */
                CleaningStatus.NORMAL -> {

                    Text(
                        text =
                            "状態：通常"
                    )

                    Button(

                        onClick =
                            onRequestCleaning,

                        modifier =
                            Modifier
                                .fillMaxWidth()

                    ) {

                        Text(

                            text =
                                "清掃を依頼する"
                        )
                    }
                }

                /*
                 * =================================
                 * 清掃依頼中
                 * =================================
                 */
                CleaningStatus.REQUESTED -> {

                    Text(

                        text =
                            "状態：清掃依頼中"
                    )

                    Button(

                        onClick =
                            onMarkCleaned,

                        modifier =
                            Modifier
                                .fillMaxWidth()

                    ) {

                        Text(

                            text =
                                "清掃しました"
                        )
                    }
                }

                /*
                 * =================================
                 * 清掃済み
                 * =================================
                 */
                CleaningStatus.CLEANED -> {

                    Text(

                        text =
                            "状態：清掃済み"
                    )
                }
            }
        }
    }
}