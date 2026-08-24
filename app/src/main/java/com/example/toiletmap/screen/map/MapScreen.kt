package com.example.toiletmap.screen.map

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.heightIn

// =============================================
// マップ画面専用カラー
// =============================================

private val FinderGreen =
    Color(0xFF0B8377)

private val FinderDark =
    Color(0xFF12313A)

private val FinderMuted =
    Color(0xFF748186)

private val FinderBorder =
    Color(0xFFE2E8E6)

private val FinderPale =
    Color(0xFFF5F8F7)

private val FinderAmber =
    Color(0xFFF2B544)

private val FinderRed =
    Color(0xFFD94B4B)


@Composable
fun MapScreen(

    mapView: MapView,

    searchText: String = "",

    onSearchTextChange: (String) -> Unit = {} ,

    // トイレ追加時に
    // 地図から位置を選択しているか
    isSelectingLocation: Boolean = false,

    // 現在選択中のトイレ
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


    // =============================================
    // トイレ追加用
    // 地図タップ監視
    // =============================================

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


    // =============================================
    // 画面全体
    // =============================================

    Box(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    FinderPale
                )

    ) {


        // =============================================
        // MapLibre
        // =============================================

        AndroidView(

            factory = {

                /*
                 * 同じMapViewを再利用するため、
                 * 以前の親Viewから外す
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
                Modifier
                    .fillMaxSize()

                    /*
                     * 上のヘッダー分だけ
                     * 地図を下げる
                     */
                    .padding(
                        top = 154.dp
                    )
        )


        // =============================================
        // 上部ヘッダー
        // =============================================

        FinderHeader(

            searchText = searchText,

            onSearchTextChange = onSearchTextChange,

            modifier =
                Modifier.align(
                    Alignment.TopCenter
                )
        )


        // =============================================
        // トイレ追加位置を選択中
        // =============================================

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
                            top = 166.dp,
                            start = 14.dp,
                            end = 14.dp
                        )
            )
        }


        // =============================================
        // トイレピンを押した場合
        // =============================================

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
                            12.dp
                        )
            )
        }
    }
}


// =============================================
// 上部
// TOILET FINDER
// 近くのトイレ
// 検索バー
// 通知
// =============================================

@Composable
private fun FinderHeader(

    searchText: String,

    onSearchTextChange: (String) -> Unit,

    modifier: Modifier =
        Modifier

) {

    /*
     * 通知メニューを表示しているか
     */
    var showNotifications by
    remember {

        mutableStateOf(
            false
        )
    }


    Surface(

        modifier =
            modifier
                .fillMaxWidth()
                .zIndex(10f),

        color =
            Color(
                0xFFF9FBFA
            ),

        shadowElevation =
            5.dp

    ) {

        Column(

            modifier =
                Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 14.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )

        ) {


            // =============================================
            // タイトル部分
            // =============================================

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {


                // =============================================
                // WCロゴ
                // =============================================

                Box(

                    modifier =
                        Modifier
                            .size(
                                44.dp
                            )
                            .background(

                                color =
                                    FinderGreen,

                                shape =
                                    RoundedCornerShape(
                                        13.dp
                                    )
                            ),

                    contentAlignment =
                        Alignment.Center

                ) {

                    Text(

                        text =
                            "WC",

                        color =
                            Color.White,

                        fontSize =
                            13.sp,

                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }


                Spacer(

                    modifier =
                        Modifier.width(
                            12.dp
                        )
                )


                // =============================================
                // TOILET FINDER
                // 近くのトイレ
                // =============================================

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
                            FinderGreen,

                        fontSize =
                            10.sp,

                        fontWeight =
                            FontWeight.ExtraBold,

                        letterSpacing =
                            1.5.sp
                    )


                    Text(

                        text =
                            "近くのトイレ",

                        color =
                            FinderDark,

                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,

                        fontWeight =
                            FontWeight.Bold
                    )
                }


                // =============================================
                // 通知
                // =============================================

                Box {

                    IconButton(

                        onClick = {

                            showNotifications =
                                true
                        }

                    ) {

                        Icon(

                            imageVector =
                                Icons
                                    .Outlined
                                    .NotificationsNone,

                            contentDescription =
                                "通知",

                            tint =
                                FinderDark,

                            modifier =
                                Modifier.size(
                                    25.dp
                                )
                        )
                    }


                    /*
                     * 今は通知機能未実装なので
                     * 常にこれを表示
                     */
                    DropdownMenu(

                        expanded =
                            showNotifications,

                        onDismissRequest = {

                            showNotifications =
                                false
                        }

                    ) {

                        DropdownMenuItem(

                            text = {

                                Text(

                                    text =
                                        "新しい通知はありません",

                                    color =
                                        FinderDark
                                )
                            },

                            onClick = {

                                showNotifications =
                                    false
                            }
                        )
                    }
                }
            }


            // =============================================
            // 検索バー
            //
            // 入力可能な検索欄
            // =============================================
            OutlinedTextField(

                value = searchText,

                onValueChange = onSearchTextChange,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                placeholder = {
                    Text("場所や施設名を検索")
                },

                singleLine = true
            )
        }
    }
}

// =============================================
// トイレ追加場所を選択中の案内
// =============================================

@Composable
private fun LocationSelectionBanner(

    onCancel: () -> Unit,

    modifier: Modifier =
        Modifier

) {

    Card(

        modifier =
            modifier
                .fillMaxWidth()
                .shadow(

                    elevation =
                        7.dp,

                    shape =
                        RoundedCornerShape(
                            20.dp
                        )
                ),

        shape =
            RoundedCornerShape(
                20.dp
            ),

        colors =
            CardDefaults
                .cardColors(

                    containerColor =
                        Color.White
                )

    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 8.dp,
                        top = 12.dp,
                        bottom = 12.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically

        ) {


            Box(

                modifier =
                    Modifier
                        .size(
                            38.dp
                        )
                        .background(

                            color =
                                Color(
                                    0xFFE5F4F1
                                ),

                            shape =
                                CircleShape
                        ),

                contentAlignment =
                    Alignment.Center

            ) {

                Icon(

                    imageVector =
                        Icons
                            .Outlined
                            .LocationOn,

                    contentDescription =
                        null,

                    tint =
                        FinderGreen
                )
            }


            Spacer(

                modifier =
                    Modifier.width(
                        10.dp
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
                        FinderDark,

                    fontWeight =
                        FontWeight.Bold
                )


                Text(

                    text =
                        "地図上の登録したい場所を1回タップしてください",

                    color =
                        FinderMuted,

                    fontSize =
                        12.sp
                )
            }


            TextButton(

                onClick =
                    onCancel

            ) {

                Text(

                    text =
                        "キャンセル",

                    color =
                        FinderGreen
                )
            }
        }
    }
}


// =============================================
// トイレ詳細カード
// =============================================

@Composable
private fun ToiletDetailCard(

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
    val cleanliness =

        toilet
            .cleanliness
            .coerceIn(
                0,
                5
            )


    /*
     * 前回の清掃からの経過時間
     */
    val elapsed =

        formatElapsedSinceCleaning(
            toilet.lastCleanedAtMillis
        )


    /*
     * 清掃待ちか
     */
    val requested =

        toilet.cleaningStatus ==
                CleaningStatus.REQUESTED


    val statusText =

        if (
            requested
        ) {

            "清掃待ち"

        } else {

            "通常"
        }


    val statusColor =

        if (
            requested
        ) {

            FinderAmber

        } else {

            FinderRed
        }


    val actionText =

        if (
            requested
        ) {

            "清掃しました"

        } else {

            "清掃を依頼する"
        }


    val actionIcon =

        if (
            requested
        ) {

            Icons
                .Outlined
                .CheckCircle

        } else {

            Icons
                .Outlined
                .NotificationsNone
        }


    Card(

        modifier =
            modifier
                .heightIn(
                    max = 380.dp
                )
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(24.dp)
                ),

        shape =
            RoundedCornerShape(
                24.dp
            ),

        colors =
            CardDefaults
                .cardColors(

                    containerColor =
                        Color.White
                ),

        elevation =
            CardDefaults
                .cardElevation(

                    defaultElevation =
                        4.dp
                )

    ) {

        Column(

            modifier =
                Modifier
                    .padding(
                        18.dp
                    )
                    .verticalScroll(
                        rememberScrollState()
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )

        ) {


            // =============================================
            // 状態 + 閉じる
            // =============================================

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {


                Surface(

                    color =
                        statusColor
                            .copy(
                                alpha = 0.13f
                            ),

                    shape =
                        RoundedCornerShape(
                            8.dp
                        )

                ) {

                    Text(

                        text =
                            statusText,

                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),

                        color =
                            statusColor,

                        fontSize =
                            12.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }


                Text(

                    text =
                        "トイレ情報",

                    modifier =
                        Modifier.padding(
                            start = 8.dp
                        ),

                    color =
                        FinderMuted,

                    fontSize =
                        13.sp
                )


                Spacer(

                    modifier =
                        Modifier.weight(
                            1f
                        )
                )


                IconButton(

                    onClick =
                        onDismiss,

                    modifier =
                        Modifier.size(
                            34.dp
                        )

                ) {

                    Icon(

                        imageVector =
                            Icons
                                .Outlined
                                .Close,

                        contentDescription =
                            "閉じる",

                        tint =
                            FinderDark
                    )
                }
            }


            // =============================================
            // トイレ名
            // =============================================

            Text(

                text =
                    toilet.name,

                color =
                    FinderDark,

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Bold
            )


            // =============================================
            // 緯度経度
            // =============================================

            Row(

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                Icon(

                    imageVector =
                        Icons
                            .Outlined
                            .LocationOn,

                    contentDescription =
                        null,

                    tint =
                        FinderMuted,

                    modifier =
                        Modifier.size(
                            18.dp
                        )
                )


                Spacer(

                    modifier =
                        Modifier.width(
                            5.dp
                        )
                )


                Text(

                    text =
                        "緯度 %.5f / 経度 %.5f".format(
                            toilet.latitude,
                            toilet.longitude
                        ),

                    color =
                        FinderMuted,

                    fontSize =
                        12.sp
                )
            }


            // =============================================
            // 情報部分
            // =============================================

            Surface(

                modifier =
                    Modifier.fillMaxWidth(),

                color =
                    Color(
                        0xFFF8FAF9
                    ),

                shape =
                    RoundedCornerShape(
                        16.dp
                    )

            ) {

                Column(

                    modifier =
                        Modifier.padding(
                            14.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            9.dp
                        )

                ) {


                    Text(

                        text =
                            "きれいさ",

                        color =
                            FinderMuted,

                        fontSize =
                            12.sp
                    )


                    // =============================================
                    // 星評価
                    // =============================================

                    Row(

                        verticalAlignment =
                            Alignment.CenterVertically

                    ) {

                        repeat(
                            5
                        ) {
                                index ->


                            Icon(

                                imageVector =
                                    Icons
                                        .Filled
                                        .Star,

                                contentDescription =
                                    null,

                                tint =

                                    if (
                                        index <
                                        cleanliness
                                    ) {

                                        FinderGreen

                                    } else {

                                        Color(
                                            0xFFD7DEDC
                                        )
                                    },

                                modifier =
                                    Modifier.size(
                                        20.dp
                                    )
                            )
                        }


                        Spacer(

                            modifier =
                                Modifier.width(
                                    7.dp
                                )
                        )


                        Text(

                            text =
                                "$cleanliness.0",

                            color =
                                FinderDark,

                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }


                    // =============================================
                    // 前回清掃
                    // =============================================

                    Text(

                        text =
                            "前回の清掃完了：$elapsed",

                        color =
                            FinderMuted,

                        fontSize =
                            12.sp
                    )


                    // =============================================
                    // コメント
                    // =============================================

                    if (
                        toilet.comment
                            .isNotBlank()
                    ) {

                        Text(

                            text =
                                toilet.comment,

                            color =
                                FinderDark,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium
                        )
                    }
                }
            }


            // =============================================
            // 清掃ボタン
            // =============================================

            Button(

                onClick =

                    if (
                        requested
                    ) {

                        onMarkCleaned

                    } else {

                        onRequestCleaning
                    },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            50.dp
                        ),

                shape =
                    RoundedCornerShape(
                        14.dp
                    ),

                colors =
                    ButtonDefaults
                        .buttonColors(

                            containerColor =
                                FinderGreen
                        )

            ) {

                Icon(

                    imageVector =
                        actionIcon,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(
                            20.dp
                        )
                )


                Spacer(

                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )


                Text(

                    text =
                        actionText,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


// =============================================
// 前回清掃からの経過時間
// =============================================

private fun formatElapsedSinceCleaning(

    lastCleanedAtMillis:
    Long?

): String {


    if (
        lastCleanedAtMillis ==
        null
    ) {

        return "記録なし"
    }


    val elapsedMillis =

        (
                System.currentTimeMillis() -
                        lastCleanedAtMillis
                )
            .coerceAtLeast(
                0L
            )


    val totalMinutes =

        elapsedMillis /
                60_000L


    if (
        totalMinutes <
        1L
    ) {

        return "1分未満"
    }


    if (
        totalMinutes <
        60L
    ) {

        return "${totalMinutes}分前"
    }


    val totalHours =

        totalMinutes /
                60L


    val remainingMinutes =

        totalMinutes %
                60L


    if (
        totalHours <
        24L
    ) {

        return if (
            remainingMinutes ==
            0L
        ) {

            "${totalHours}時間前"

        } else {

            "${totalHours}時間${remainingMinutes}分前"
        }
    }


    val totalDays =

        totalHours /
                24L


    val remainingHours =

        totalHours %
                24L


    return if (
        remainingHours ==
        0L
    ) {

        "${totalDays}日前"

    } else {

        "${totalDays}日${remainingHours}時間前"
    }
}