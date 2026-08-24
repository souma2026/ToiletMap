package com.example.toiletmap.screen.map

import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import kotlinx.coroutines.delay
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView


// ==========================================
// 色
// ==========================================

private val Teal =
    Color(0xFF008C7D)

private val DeepTeal =
    Color(0xFF006E63)

private val Ink =
    Color(0xFF18272C)

private val Muted =
    Color(0xFF7B878D)

private val SoftBackground =
    Color(0xFFF7FAF9)

private val SoftTeal =
    Color(0xFFE5F5F1)

private val Border =
    Color(0xFFE3E9E7)

private val CleaningYellow =
    Color(0xFFFFC107)


// ==========================================
// マップ画面
// ==========================================

@Composable
fun MapScreen(

    mapView: MapView,

    isSelectingLocation: Boolean = false,

    selectedToilet: Toilet? = null,

    onDismissSelectedToilet: () -> Unit = {},

    onRequestCleaning: (Toilet) -> Unit = {},

    onMarkCleaned: (Toilet) -> Unit = {},

    onLocationSelected:
        (Double, Double) -> Unit =
        { _, _ -> },

    onCancelLocationSelection:
        () -> Unit = {}

) {

    var showNotificationDialog by
    remember {

        mutableStateOf(
            false
        )
    }


    // ==========================================
    // トイレ追加時の地図タップ処理
    // ==========================================

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


    // ==========================================
    // 画面全体
    // ==========================================

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    SoftBackground
                )

    ) {


        // ==========================================
        // 上部ヘッダー
        // ==========================================

        FinderHeader(

            onNotificationClick = {

                showNotificationDialog =
                    true
            }
        )


        // ==========================================
        // 地図部分
        // ==========================================

        Box(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(
                        1f
                    )

        ) {


            // ==========================================
            // MapLibre
            // ==========================================

            AndroidView(

                factory = {


                    // 既存のMapViewを再利用
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


            // ==========================================
            // 右側マップ操作ボタン
            // ==========================================

            MapControls(

                onZoomIn = {

                    mapView.getMapAsync {
                            map ->

                        map.animateCamera(

                            CameraUpdateFactory
                                .zoomIn()
                        )
                    }
                },


                onZoomOut = {

                    mapView.getMapAsync {
                            map ->

                        map.animateCamera(

                            CameraUpdateFactory
                                .zoomOut()
                        )
                    }
                },


                onCurrentLocation = {

                    /*
                     * 現在地取得はまだ未実装。
                     *
                     * 今回は画像に近いUIだけ作成。
                     *
                     * 後でGPSとつなげることができます。
                     */
                },


                modifier =
                    Modifier
                        .align(
                            Alignment.CenterEnd
                        )
                        .padding(

                            end =
                                14.dp,

                            bottom =
                                40.dp
                        )
            )


            // ==========================================
            // トイレ場所選択中
            // ==========================================

            if (
                isSelectingLocation
            ) {

                LocationSelectionBanner(

                    onCancel =
                        onCancelLocationSelection,

                    modifier =
                        Modifier
                            .align(
                                Alignment.TopCenter
                            )
                            .padding(
                                14.dp
                            )
                )
            }


            // ==========================================
            // トイレを選択したときの詳細カード
            // ==========================================

            if (

                !isSelectingLocation &&
                selectedToilet != null

            ) {

                ToiletDetailCard(

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

                                horizontal =
                                    12.dp,

                                vertical =
                                    12.dp
                            )
                )
            }
        }
    }


    // ==========================================
    // 通知ダイアログ
    // ==========================================

    if (
        showNotificationDialog
    ) {

        AlertDialog(

            onDismissRequest = {

                showNotificationDialog =
                    false
            },


            title = {

                Text(

                    text =
                        "通知",

                    fontWeight =
                        FontWeight.Bold
                )
            },


            text = {

                Text(
                    "現在、新しい通知はありません。"
                )
            },


            confirmButton = {

                TextButton(

                    onClick = {

                        showNotificationDialog =
                            false
                    }

                ) {

                    Text(

                        text =
                            "OK",

                        color =
                            Teal
                    )
                }
            }
        )
    }
}


// ==========================================
// 上部ヘッダー
// ==========================================

@Composable
private fun FinderHeader(

    onNotificationClick:
        () -> Unit

) {

    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    Color.White
                )
                .padding(

                    start =
                        20.dp,

                    end =
                        20.dp,

                    top =
                        14.dp,

                    bottom =
                        14.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                14.dp
            )

    ) {


        // ==========================================
        // タイトル部分
        // ==========================================

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically

        ) {


            // ==========================================
            // WCロゴ
            // ==========================================

            Box(

                modifier =
                    Modifier
                        .size(
                            54.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                16.dp
                            )
                        )
                        .background(
                            Teal
                        ),

                contentAlignment =
                    Alignment.Center

            ) {

                Text(

                    text =
                        "WC",

                    color =
                        Color.White,

                    fontWeight =
                        FontWeight.Black,

                    fontSize =
                        17.sp
                )
            }


            Spacer(

                modifier =
                    Modifier.width(
                        14.dp
                    )
            )


            // ==========================================
            // TOILET FINDER
            // 近くのトイレ
            // ==========================================

            Column(

                modifier =
                    Modifier.weight(
                        1f
                    )

            ) {

                Text(

                    text =
                        "TOILET FINDER",

                    color =
                        Teal,

                    fontSize =
                        11.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    letterSpacing =
                        1.5.sp
                )


                Text(

                    text =
                        "近くのトイレ",

                    color =
                        Ink,

                    fontSize =
                        27.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }


            // ==========================================
            // 通知ボタン
            // ==========================================

            Box(

                modifier =
                    Modifier
                        .size(
                            48.dp
                        )
                        .shadow(

                            elevation =
                                7.dp,

                            shape =
                                CircleShape
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            Color.White
                        )
                        .clickable(

                            onClick =
                                onNotificationClick
                        ),

                contentAlignment =
                    Alignment.Center

            ) {

                BellIcon(

                    color =
                        Ink,

                    modifier =
                        Modifier.size(
                            24.dp
                        )
                )


                // 赤い通知ドット
                Box(

                    modifier =
                        Modifier
                            .align(
                                Alignment.TopEnd
                            )
                            .padding(

                                top =
                                    5.dp,

                                end =
                                    5.dp
                            )
                            .size(
                                9.dp
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                Color(
                                    0xFFFF4150
                                )
                            )
                )
            }
        }


        // ==========================================
        // 検索ボックス
        // ==========================================

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        56.dp
                    )
                    .shadow(

                        elevation =
                            5.dp,

                        shape =
                            RoundedCornerShape(
                                20.dp
                            )
                    )
                    .clip(

                        RoundedCornerShape(
                            20.dp
                        )
                    )
                    .background(
                        Color.White
                    )
                    .clickable {

                        /*
                         * 検索機能はまだ何もしない。
                         *
                         * 後から実装可能。
                         */
                    }
                    .padding(

                        horizontal =
                            18.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            SearchIcon(

                color =
                    Muted,

                modifier =
                    Modifier.size(
                        23.dp
                    )
            )


            Spacer(

                modifier =
                    Modifier.width(
                        14.dp
                    )
            )


            Text(

                text =
                    "場所や施設名を検索",

                color =
                    Color(
                        0xFF9AA3A8
                    ),

                fontSize =
                    16.sp,

                modifier =
                    Modifier.weight(
                        1f
                    )
            )


            MenuIcon(

                color =
                    Color(
                        0xFF526067
                    ),

                modifier =
                    Modifier.size(
                        22.dp
                    )
            )
        }
    }
}


// ==========================================
// 地図操作ボタン
// ==========================================

@Composable
private fun MapControls(

    onZoomIn:
        () -> Unit,

    onZoomOut:
        () -> Unit,

    onCurrentLocation:
        () -> Unit,

    modifier:
    Modifier =
        Modifier

) {

    Column(

        modifier =
            modifier,

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            )

    ) {


        // ==========================================
        // 現在地ボタン
        // ==========================================

        Box(

            modifier =
                Modifier
                    .size(
                        54.dp
                    )
                    .shadow(

                        elevation =
                            7.dp,

                        shape =
                            RoundedCornerShape(
                                18.dp
                            )
                    )
                    .clip(

                        RoundedCornerShape(
                            18.dp
                        )
                    )
                    .background(
                        Color.White
                    )
                    .clickable(

                        onClick =
                            onCurrentLocation
                    ),

            contentAlignment =
                Alignment.Center

        ) {

            TargetIcon(

                color =
                    Teal,

                modifier =
                    Modifier.size(
                        28.dp
                    )
            )
        }


        // ==========================================
        // 拡大縮小
        // ==========================================

        Column(

            modifier =
                Modifier
                    .shadow(

                        elevation =
                            7.dp,

                        shape =
                            RoundedCornerShape(
                                18.dp
                            )
                    )
                    .clip(

                        RoundedCornerShape(
                            18.dp
                        )
                    )
                    .background(
                        Color.White
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally

        ) {


            // ＋
            Box(

                modifier =
                    Modifier
                        .size(

                            width =
                                54.dp,

                            height =
                                52.dp
                        )
                        .clickable(

                            onClick =
                                onZoomIn
                        ),

                contentAlignment =
                    Alignment.Center

            ) {

                Text(

                    text =
                        "+",

                    color =
                        Ink,

                    fontSize =
                        31.sp,

                    fontWeight =
                        FontWeight.Light
                )
            }


            Box(

                modifier =
                    Modifier
                        .width(
                            28.dp
                        )
                        .height(
                            1.dp
                        )
                        .background(
                            Border
                        )
            )


            // −
            Box(

                modifier =
                    Modifier
                        .size(

                            width =
                                54.dp,

                            height =
                                52.dp
                        )
                        .clickable(

                            onClick =
                                onZoomOut
                        ),

                contentAlignment =
                    Alignment.Center

            ) {

                Text(

                    text =
                        "−",

                    color =
                        Ink,

                    fontSize =
                        31.sp,

                    fontWeight =
                        FontWeight.Light
                )
            }
        }
    }
}


// ==========================================
// トイレ追加場所選択
// ==========================================

@Composable
private fun LocationSelectionBanner(

    onCancel:
        () -> Unit,

    modifier:
    Modifier =
        Modifier

) {

    Surface(

        modifier =
            modifier
                .fillMaxWidth(),

        shape =
            RoundedCornerShape(
                22.dp
            ),

        color =
            Color.White,

        shadowElevation =
            10.dp

    ) {

        Row(

            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            Box(

                modifier =
                    Modifier
                        .size(
                            42.dp
                        )
                        .clip(

                            RoundedCornerShape(
                                14.dp
                            )
                        )
                        .background(
                            SoftTeal
                        ),

                contentAlignment =
                    Alignment.Center

            ) {

                TargetIcon(

                    color =
                        Teal,

                    modifier =
                        Modifier.size(
                            24.dp
                        )
                )
            }


            Spacer(

                modifier =
                    Modifier.width(
                        12.dp
                    )
            )


            Column(

                modifier =
                    Modifier.weight(
                        1f
                    )

            ) {

                Text(

                    text =
                        "トイレの場所を選択",

                    color =
                        Ink,

                    fontWeight =
                        FontWeight.Bold,

                    fontSize =
                        16.sp
                )


                Text(

                    text =
                        "地図上の登録したい場所を1回タップ",

                    color =
                        Muted,

                    fontSize =
                        13.sp
                )
            }


            Text(

                text =
                    "キャンセル",

                color =
                    Teal,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    13.sp,

                modifier =
                    Modifier
                        .clip(

                            RoundedCornerShape(
                                12.dp
                            )
                        )
                        .clickable(

                            onClick =
                                onCancel
                        )
                        .padding(

                            horizontal =
                                10.dp,

                            vertical =
                                8.dp
                        )
            )
        }
    }
}


// ==========================================
// トイレ詳細カード
// ==========================================

@Composable
private fun ToiletDetailCard(

    toilet:
    Toilet,

    onDismiss:
        () -> Unit,

    onRequestCleaning:
        () -> Unit,

    onMarkCleaned:
        () -> Unit,

    modifier:
    Modifier =
        Modifier

) {


    /*
     * 現在時刻
     *
     * 1分ごとに更新することで
     * 「前回の清掃から○分」が
     * 自動更新される。
     */
    var nowMillis by

    remember(

        toilet.id,
        toilet.lastCleanedAtMillis

    ) {

        mutableLongStateOf(
            System.currentTimeMillis()
        )
    }


    LaunchedEffect(

        toilet.id,
        toilet.lastCleanedAtMillis

    ) {

        while (
            true
        ) {

            nowMillis =
                System.currentTimeMillis()


            delay(
                60_000L
            )
        }
    }


    // ==========================================
    // 星
    // ==========================================

    val stars =

        "★".repeat(
            toilet.cleanliness
        ) +

                "☆".repeat(

                    (
                            5 -
                                    toilet.cleanliness
                            )
                        .coerceAtLeast(
                            0
                        )
                )


    // ==========================================
    // 前回の清掃からの経過時間
    // ==========================================

    val elapsed =

        formatElapsedSinceCleaning(

            lastCleanedAtMillis =
                toilet.lastCleanedAtMillis,

            nowMillis =
                nowMillis
        )


    val isCleaningRequested =

        toilet.cleaningStatus ==
                CleaningStatus.REQUESTED


    Surface(

        modifier =
            modifier,

        shape =
            RoundedCornerShape(
                28.dp
            ),

        color =
            Color.White,

        shadowElevation =
            14.dp

    ) {

        Column(

            modifier =
                Modifier.padding(

                    horizontal =
                        20.dp,

                    vertical =
                        16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    13.dp
                )

        ) {


            // ==========================================
            // 上のつまみ
            // ==========================================

            Box(

                modifier =
                    Modifier
                        .align(
                            Alignment.CenterHorizontally
                        )
                        .width(
                            38.dp
                        )
                        .height(
                            4.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(

                            Color(
                                0xFFD8DEDC
                            )
                        )
            )


            // ==========================================
            // 状態
            // ==========================================

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                StatusPill(

                    text =

                        if (
                            isCleaningRequested
                        ) {

                            "清掃待ち"

                        } else {

                            "通常"
                        },


                    backgroundColor =

                        if (
                            isCleaningRequested
                        ) {

                            Color(
                                0xFFFFF4D6
                            )

                        } else {

                            SoftTeal
                        },


                    textColor =

                        if (
                            isCleaningRequested
                        ) {

                            Color(
                                0xFF8A6500
                            )

                        } else {

                            DeepTeal
                        }
                )


                Spacer(

                    modifier =
                        Modifier.width(
                            10.dp
                        )
                )


                Text(

                    text =
                        "公共トイレ",

                    color =
                        Muted,

                    fontSize =
                        13.sp
                )


                Spacer(

                    modifier =
                        Modifier.weight(
                            1f
                        )
                )


                // ==========================================
                // 閉じる
                // ==========================================

                Box(

                    modifier =
                        Modifier
                            .size(
                                34.dp
                            )
                            .clip(
                                CircleShape
                            )
                            .clickable(

                                onClick =
                                    onDismiss
                            ),

                    contentAlignment =
                        Alignment.Center

                ) {

                    Text(

                        text =
                            "×",

                        color =
                            Ink,

                        fontSize =
                            26.sp,

                        fontWeight =
                            FontWeight.Light
                    )
                }
            }


            // ==========================================
            // トイレ名
            // ==========================================

            Text(

                text =
                    toilet.name,

                color =
                    Ink,

                fontSize =
                    23.sp,

                fontWeight =
                    FontWeight.Bold,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )


            // ==========================================
            // コメント
            // ==========================================

            if (
                toilet.comment
                    .isNotBlank()
            ) {

                Box(

                    modifier =
                        Modifier
                            .clip(

                                RoundedCornerShape(
                                    11.dp
                                )
                            )
                            .background(

                                Color(
                                    0xFFF3F5F4
                                )
                            )
                            .padding(

                                horizontal =
                                    11.dp,

                                vertical =
                                    7.dp
                            )
                ) {

                    Text(

                        text =
                            toilet.comment,

                        color =
                            Color(
                                0xFF566369
                            ),

                        fontSize =
                            13.sp,

                        maxLines =
                            2,

                        overflow =
                            TextOverflow.Ellipsis
                    )
                }
            }


            // ==========================================
            // 区切り線
            // ==========================================

            Box(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            1.dp
                        )
                        .background(

                            Color(
                                0xFFEEF1F0
                            )
                        )
            )


            // ==========================================
            // 前回の清掃
            // 清潔度
            // ==========================================

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {


                // ==========================================
                // 前回清掃から
                // ==========================================

                DetailMetric(

                    title =
                        "前回の清掃から",

                    value =
                        elapsed,

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    leading = {

                        ClockIcon(

                            color =
                                Muted,

                            modifier =
                                Modifier.size(
                                    21.dp
                                )
                        )
                    }
                )


                // 縦線
                Box(

                    modifier =
                        Modifier
                            .width(
                                1.dp
                            )
                            .height(
                                48.dp
                            )
                            .background(

                                Color(
                                    0xFFEDF0EF
                                )
                            )
                )


                // ==========================================
                // きれいさ
                // ==========================================

                DetailMetric(

                    title =
                        "きれいさ",

                    value =
                        "$stars  ${toilet.cleanliness}.0",

                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                            .padding(

                                start =
                                    16.dp
                            ),

                    leading = {

                        Text(

                            text =
                                "✦",

                            color =
                                Muted,

                            fontSize =
                                20.sp
                        )
                    }
                )
            }


            // ==========================================
            // 清掃依頼中のメッセージ
            // ==========================================

            if (
                isCleaningRequested
            ) {

                Text(

                    text =
                        "このトイレは清掃依頼中です。清掃したら下のボタンを押してください。",

                    color =
                        Color(
                            0xFF7A6200
                        ),

                    fontSize =
                        12.sp,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(

                                RoundedCornerShape(
                                    12.dp
                                )
                            )
                            .background(

                                Color(
                                    0xFFFFF8E6
                                )
                            )
                            .padding(
                                10.dp
                            )
                )
            }


            // ==========================================
            // 清掃ボタン
            // ==========================================

            Box(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            54.dp
                        )
                        .clip(

                            RoundedCornerShape(
                                18.dp
                            )
                        )
                        .background(

                            if (
                                isCleaningRequested
                            ) {

                                CleaningYellow

                            } else {

                                Teal
                            }
                        )
                        .clickable {

                            if (
                                isCleaningRequested
                            ) {

                                onMarkCleaned()

                            } else {

                                onRequestCleaning()
                            }
                        },

                contentAlignment =
                    Alignment.Center

            ) {

                Text(

                    text =

                        if (
                            isCleaningRequested
                        ) {

                            "清掃しました"

                        } else {

                            "清掃を依頼する"
                        },


                    color =

                        if (
                            isCleaningRequested
                        ) {

                            Ink

                        } else {

                            Color.White
                        },


                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


// ==========================================
// 状態表示
// ==========================================

@Composable
private fun StatusPill(

    text:
    String,

    backgroundColor:
    Color,

    textColor:
    Color

) {

    Box(

        modifier =
            Modifier
                .clip(

                    RoundedCornerShape(
                        10.dp
                    )
                )
                .background(
                    backgroundColor
                )
                .padding(

                    horizontal =
                        10.dp,

                    vertical =
                        6.dp
                )

    ) {

        Text(

            text =
                text,

            color =
                textColor,

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}


// ==========================================
// 詳細情報1項目
// ==========================================

@Composable
private fun DetailMetric(

    title:
    String,

    value:
    String,

    modifier:
    Modifier =
        Modifier,

    leading:
    @Composable () -> Unit

) {

    Column(

        modifier =
            modifier,

        verticalArrangement =
            Arrangement.spacedBy(
                5.dp
            )

    ) {

        Row(

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            leading()


            Spacer(

                modifier =
                    Modifier.width(
                        7.dp
                    )
            )


            Text(

                text =
                    title,

                color =
                    Muted,

                fontSize =
                    12.sp
            )
        }


        Text(

            text =
                value,

            color =
                Ink,

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.Medium,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}


// ==========================================
// 前回の清掃からどれだけ経過したか
// ==========================================

private fun formatElapsedSinceCleaning(

    lastCleanedAtMillis:
    Long?,

    nowMillis:
    Long

): String {


    // 清掃記録がない
    if (
        lastCleanedAtMillis == null
    ) {

        return "記録なし"
    }


    val elapsedMillis =

        (
                nowMillis -
                        lastCleanedAtMillis
                )
            .coerceAtLeast(
                0L
            )


    val totalMinutes =

        elapsedMillis /
                60_000L


    // 1分未満
    if (
        totalMinutes < 1L
    ) {

        return "1分未満"
    }


    // 60分未満
    if (
        totalMinutes < 60L
    ) {

        return "${totalMinutes}分経過"
    }


    val totalHours =

        totalMinutes /
                60L


    val remainingMinutes =

        totalMinutes %
                60L


    // 24時間未満
    if (
        totalHours < 24L
    ) {

        return if (
            remainingMinutes == 0L
        ) {

            "${totalHours}時間経過"

        } else {

            "${totalHours}時間${remainingMinutes}分"
        }
    }


    val totalDays =

        totalHours /
                24L


    val remainingHours =

        totalHours %
                24L


    return if (
        remainingHours == 0L
    ) {

        "${totalDays}日経過"

    } else {

        "${totalDays}日${remainingHours}時間"
    }
}


// ==========================================
// 検索アイコン
// ==========================================

@Composable
private fun SearchIcon(

    color:
    Color,

    modifier:
    Modifier =
        Modifier

) {

    Canvas(

        modifier =
            modifier

    ) {

        val strokeWidth =
            size.minDimension *
                    0.09f


        val radius =
            size.minDimension *
                    0.28f


        val center =

            Offset(

                size.width *
                        0.42f,

                size.height *
                        0.42f
            )


        drawCircle(

            color =
                color,

            radius =
                radius,

            center =
                center,

            style =
                Stroke(

                    width =
                        strokeWidth
                )
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    size.width *
                            0.62f,

                    size.height *
                            0.62f
                ),

            end =
                Offset(

                    size.width *
                            0.86f,

                    size.height *
                            0.86f
                ),

            strokeWidth =
                strokeWidth,

            cap =
                StrokeCap.Round
        )
    }
}


// ==========================================
// メニューアイコン
// ==========================================

@Composable
private fun MenuIcon(

    color:
    Color,

    modifier:
    Modifier =
        Modifier

) {

    Canvas(

        modifier =
            modifier

    ) {

        val strokeWidth =

            size.minDimension *
                    0.09f


        listOf(

            0.25f,
            0.5f,
            0.75f

        ).forEach {
                fraction ->

            drawLine(

                color =
                    color,

                start =
                    Offset(

                        size.width *
                                0.15f,

                        size.height *
                                fraction
                    ),

                end =
                    Offset(

                        size.width *
                                0.85f,

                        size.height *
                                fraction
                    ),

                strokeWidth =
                    strokeWidth,

                cap =
                    StrokeCap.Round
            )
        }
    }
}


// ==========================================
// ベルアイコン
// ==========================================

@Composable
private fun BellIcon(

    color:
    Color,

    modifier:
    Modifier =
        Modifier

) {

    Canvas(

        modifier =
            modifier

    ) {

        val strokeWidth =

            size.minDimension *
                    0.085f


        drawArc(

            color =
                color,

            startAngle =
                200f,

            sweepAngle =
                140f,

            useCenter =
                false,

            topLeft =
                Offset(

                    size.width *
                            0.22f,

                    size.height *
                            0.18f
                ),

            size =
                Size(

                    size.width *
                            0.56f,

                    size.height *
                            0.60f
                ),

            style =
                Stroke(

                    width =
                        strokeWidth,

                    cap =
                        StrokeCap.Round
                )
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    size.width *
                            0.23f,

                    size.height *
                            0.66f
                ),

            end =
                Offset(

                    size.width *
                            0.77f,

                    size.height *
                            0.66f
                ),

            strokeWidth =
                strokeWidth,

            cap =
                StrokeCap.Round
        )


        drawCircle(

            color =
                color,

            radius =
                strokeWidth *
                        0.65f,

            center =
                Offset(

                    size.width *
                            0.5f,

                    size.height *
                            0.82f
                )
        )
    }
}


// ==========================================
// 現在地アイコン
// ==========================================

@Composable
private fun TargetIcon(

    color:
    Color,

    modifier:
    Modifier =
        Modifier

) {

    Canvas(

        modifier =
            modifier

    ) {

        val strokeWidth =

            size.minDimension *
                    0.075f


        val center =

            Offset(

                size.width /
                        2f,

                size.height /
                        2f
            )


        drawCircle(

            color =
                color,

            radius =
                size.minDimension *
                        0.28f,

            center =
                center,

            style =
                Stroke(

                    width =
                        strokeWidth
                )
        )


        drawCircle(

            color =
                color,

            radius =
                size.minDimension *
                        0.07f,

            center =
                center
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    center.x,
                    0f
                ),

            end =
                Offset(

                    center.x,

                    size.height *
                            0.18f
                ),

            strokeWidth =
                strokeWidth,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    center.x,

                    size.height *
                            0.82f
                ),

            end =
                Offset(

                    center.x,
                    size.height
                ),

            strokeWidth =
                strokeWidth,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    0f,
                    center.y
                ),

            end =
                Offset(

                    size.width *
                            0.18f,

                    center.y
                ),

            strokeWidth =
                strokeWidth,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    size.width *
                            0.82f,

                    center.y
                ),

            end =
                Offset(

                    size.width,

                    center.y
                ),

            strokeWidth =
                strokeWidth,

            cap =
                StrokeCap.Round
        )
    }
}


// ==========================================
// 時計アイコン
// ==========================================

@Composable
private fun ClockIcon(

    color:
    Color,

    modifier:
    Modifier =
        Modifier

) {

    Canvas(

        modifier =
            modifier

    ) {

        val strokeWidth =

            size.minDimension *
                    0.08f


        val center =

            Offset(

                size.width /
                        2f,

                size.height /
                        2f
            )


        drawCircle(

            color =
                color,

            radius =
                size.minDimension *
                        0.38f,

            center =
                center,

            style =
                Stroke(

                    width =
                        strokeWidth
                )
        )


        drawLine(

            color =
                color,

            start =
                center,

            end =
                Offset(

                    center.x,

                    size.height *
                            0.28f
                ),

            strokeWidth =
                strokeWidth,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                center,

            end =
                Offset(

                    size.width *
                            0.67f,

                    size.height *
                            0.58f
                ),

            strokeWidth =
                strokeWidth,

            cap =
                StrokeCap.Round
        )
    }
}