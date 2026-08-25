package com.example.toiletmap.screen.map

import android.view.ViewGroup
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import kotlinx.coroutines.delay
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView


// =============================================
// Map screen colors
// =============================================

private val FinderGreen =
    Color(0xFF0B8377)

private val FinderDark =
    Color(0xFF12313A)

private val FinderMuted =
    Color(0xFF748186)

private val FinderPale =
    Color(0xFFF5F8F7)

private val FinderAmber =
    Color(0xFFF2B544)

private val FinderRed =
    Color(0xFFD94B4B)

private val FinderSoftGreen =
    Color(0xFFE5F4F1)


// =============================================
// Map screen
// =============================================

@Composable
fun MapScreen(

    mapView: MapView,

    searchText: String = "",

    onSearchTextChange:
        (String) -> Unit = {},

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
     * =============================================
     * 通知ダイアログ
     * =============================================
     */
    var showNotificationDialog by
    remember {

        mutableStateOf(
            false
        )
    }


    /*
     * =============================================
     * 検索文字
     * =============================================
     */
    var localSearchText by
    remember {

        mutableStateOf(
            searchText
        )
    }


    /*
     * 外部から検索文字が変わった場合
     */
    LaunchedEffect(
        searchText
    ) {

        if (
            searchText !=
            localSearchText
        ) {

            localSearchText =
                searchText
        }
    }


    /*
     * =============================================
     * 詳細カードの高さ
     * =============================================
     *
     * 詳細カードで隠れていない
     * 地図部分の中央へ
     * ピンを移動するために使う。
     *
     * 単位はpx。
     */
    var detailCardHeightPx by
    remember {

        mutableIntStateOf(
            0
        )
    }


    /*
     * =============================================
     * 選択したトイレへ
     * カメラ移動 + ズーム
     * =============================================
     *
     * selectedToiletが変わった場合、
     * 選択されたトイレへ地図を移動する。
     *
     * また、
     * 下に表示される詳細カードの高さを
     * MapLibreのbottom paddingとして設定する。
     *
     * これによって、
     *
     * 「詳細カードで隠れていない地図部分」
     *
     * の中央付近に
     * 選択したトイレのピンが表示される。
     */
    LaunchedEffect(

        selectedToilet?.id,

        detailCardHeightPx

    ) {


        val toilet =
            selectedToilet


        /*
         * トイレが選択されていて、
         * 詳細カードの高さも取得できた場合
         */
        if (
            toilet != null &&
            detailCardHeightPx > 0
        ) {


            mapView.getMapAsync {
                    map ->


                /*
                 * =============================================
                 * 現在のズーム倍率
                 * =============================================
                 */
                val currentZoom =

                    map
                        .cameraPosition
                        .zoom


                /*
                 * =============================================
                 * 移動後のズーム倍率
                 * =============================================
                 *
                 * 現在かなり遠い
                 * ↓
                 * 16.5まで拡大
                 *
                 * すでに近い
                 * ↓
                 * さらに0.8拡大
                 *
                 * 17.5以上
                 * ↓
                 * それ以上無理に拡大しない
                 */
                val targetZoom =

                    when {


                        currentZoom <
                                16.5 -> {

                            16.5
                        }


                        currentZoom <
                                17.5 -> {

                            currentZoom +
                                    0.8
                        }


                        else -> {

                            currentZoom
                        }
                    }


                /*
                 * =============================================
                 * カメラ位置を作成
                 * =============================================
                 */
                val cameraPosition =

                    CameraPosition
                        .Builder()

                        /*
                         * 選択されたトイレ
                         */
                        .target(

                            LatLng(

                                toilet.latitude,

                                toilet.longitude
                            )
                        )

                        /*
                         * ズーム
                         */
                        .zoom(
                            targetZoom
                        )

                        /*
                         * =====================================
                         * 詳細カード分を
                         * 地図表示領域から除外
                         * =====================================
                         *
                         * left
                         * top
                         * right
                         * bottom
                         */
                        .padding(

                            0.0,

                            0.0,

                            0.0,

                            detailCardHeightPx
                                .toDouble()
                        )

                        .build()


                /*
                 * =============================================
                 * アニメーション付きで移動
                 * =============================================
                 */
                map.easeCamera(

                    CameraUpdateFactory
                        .newCameraPosition(
                            cameraPosition
                        ),

                    700
                )
            }
        }
    }


    /*
     * =============================================
     * トイレ追加時
     * 地図タップ監視
     * =============================================
     *
     * 通常のMap閲覧中には
     * 地図タップを監視しない。
     *
     * トイレ追加時のみ監視する。
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


        /*
         * 場所選択中のみ
         */
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


                    /*
                     * 地図を押した場所を取得
                     */
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


        /*
         * Composableが消えた場合
         */
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


    /*
     * =============================================
     * 画面全体
     * =============================================
     */
    Box(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    FinderPale
                )

    ) {


        /*
         * =============================================
         * MapLibre
         * =============================================
         */
        AndroidView(

            factory = {


                /*
                 * 同じMapViewを再利用するため、
                 * 前の親Viewから外す
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
                     * 上部ヘッダー分
                     */
                    .padding(
                        top = 154.dp
                    )
        )


        /*
         * =============================================
         * 上部ヘッダー
         * =============================================
         */
        FinderHeader(

            searchText =
                localSearchText,


            onSearchTextChange = {
                    newValue ->


                localSearchText =
                    newValue


                onSearchTextChange(
                    newValue
                )
            },


            onNotificationClick = {

                showNotificationDialog =
                    true
            },


            modifier =
                Modifier.align(
                    Alignment.TopCenter
                )
        )


        /*
         * =============================================
         * 地図操作ボタン
         * =============================================
         */
        MapControls(


            /*
             * 拡大
             */
            onZoomIn = {


                mapView.getMapAsync {
                        map ->


                    map.animateCamera(

                        CameraUpdateFactory
                            .zoomIn()
                    )
                }
            },


            /*
             * 縮小
             */
            onZoomOut = {


                mapView.getMapAsync {
                        map ->


                    map.animateCamera(

                        CameraUpdateFactory
                            .zoomOut()
                    )
                }
            },


            /*
             * 現在地
             *
             * 現状では未実装
             */
            onCurrentLocation = {

                // 後からGPS機能を接続可能
            },


            modifier =
                Modifier
                    .align(
                        Alignment.CenterEnd
                    )
                    .padding(

                        end = 14.dp,

                        bottom = 40.dp
                    )
        )


        /*
         * =============================================
         * トイレ追加位置を選択中
         * =============================================
         */
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


        /*
         * =============================================
         * トイレピンを押した場合
         * =============================================
         */
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

                        /*
                         * 下部中央
                         */
                        .align(
                            Alignment.BottomCenter
                        )

                        .fillMaxWidth()

                        .padding(
                            12.dp
                        )

                        /*
                         * =====================================
                         * 詳細カードの高さを取得
                         * =====================================
                         *
                         * これが無いと
                         * detailCardHeightPxが0のままなので、
                         * 上のカメラ移動処理が実行されない。
                         */
                        .onGloballyPositioned {
                                coordinates ->


                            detailCardHeightPx =

                                coordinates
                                    .size
                                    .height
                        }
            )
        }
    }


    /*
     * =============================================
     * 通知ダイアログ
     * =============================================
     */
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
                            FinderGreen
                    )
                }
            }
        )
    }
}


// =============================================
// Header
// =============================================

@Composable
private fun FinderHeader(

    searchText:
    String,

    onSearchTextChange:
        (String) -> Unit,

    onNotificationClick:
        () -> Unit,

    modifier:
    Modifier =
        Modifier

) {


    Surface(

        modifier =
            modifier
                .fillMaxWidth()
                .zIndex(
                    10f
                ),

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

                    horizontal =
                        18.dp,

                    vertical =
                        14.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )

        ) {


            /*
             * =============================================
             * 上部タイトル
             * =============================================
             */
            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {


                /*
                 * WCロゴ
                 */
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


                /*
                 * TOILET FINDER
                 */
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


                /*
                 * 通知ボタン
                 */
                Box(

                    modifier =
                        Modifier
                            .size(
                                46.dp
                            )
                            .shadow(

                                elevation =
                                    4.dp,

                                shape =
                                    CircleShape
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                Color.White
                            )

                ) {


                    IconButton(

                        onClick =
                            onNotificationClick,

                        modifier =
                            Modifier.fillMaxSize()

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
                }
            }


            /*
             * =============================================
             * 検索欄
             * =============================================
             */
            OutlinedTextField(

                value =
                    searchText,

                onValueChange =
                    onSearchTextChange,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            56.dp
                        ),

                placeholder = {


                    Text(
                        "場所や施設名を検索"
                    )
                },

                singleLine =
                    true
            )
        }
    }
}


// =============================================
// Map controls
// =============================================

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


        /*
         * =============================================
         * 現在地ボタン
         * =============================================
         */
        Box(

            modifier =
                Modifier
                    .size(
                        52.dp
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


            Icon(

                imageVector =
                    Icons
                        .Outlined
                        .LocationOn,

                contentDescription =
                    "現在地",

                tint =
                    FinderGreen,

                modifier =
                    Modifier.size(
                        25.dp
                    )
            )
        }


        /*
         * =============================================
         * 拡大・縮小
         * =============================================
         */
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


            /*
             * +
             */
            Box(

                modifier =
                    Modifier
                        .size(

                            width =
                                52.dp,

                            height =
                                50.dp
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
                        FinderDark,

                    fontSize =
                        30.sp,

                    fontWeight =
                        FontWeight.Light
                )
            }


            /*
             * 区切り
             */
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

                            Color(
                                0xFFE3E9E7
                            )
                        )
            )


            /*
             * -
             */
            Box(

                modifier =
                    Modifier
                        .size(

                            width =
                                52.dp,

                            height =
                                50.dp
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
                        FinderDark,

                    fontSize =
                        30.sp,

                    fontWeight =
                        FontWeight.Light
                )
            }
        }
    }
}


// =============================================
// Location selection banner
// =============================================

@Composable
private fun LocationSelectionBanner(

    onCancel:
        () -> Unit,

    modifier:
    Modifier =
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

                        start =
                            16.dp,

                        end =
                            8.dp,

                        top =
                            12.dp,

                        bottom =
                            12.dp
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
                                FinderSoftGreen,

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
// Toilet detail card
// =============================================

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
     * =============================================
     * 現在時刻
     * =============================================
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


    /*
     * 1分ごとに更新
     */
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
     * 前回清掃からの時間
     */
    val elapsed =

        formatElapsedSinceCleaning(

            lastCleanedAtMillis =
                toilet.lastCleanedAtMillis,

            nowMillis =
                nowMillis
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


    /*
     * =============================================
     * 詳細カード
     * =============================================
     */
    Card(

        modifier =
            modifier

                /*
                 * 地図が見えるように
                 * 最大380dp
                 */
                .heightIn(
                    max = 380.dp
                )

                .shadow(

                    elevation =
                        14.dp,

                    shape =
                        RoundedCornerShape(
                            24.dp
                        )
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
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        18.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )

        ) {


            /*
             * 上部ハンドル
             */
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


            /*
             * =============================================
             * 状態 + 閉じる
             * =============================================
             */
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

                                horizontal =
                                    10.dp,

                                vertical =
                                    6.dp
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


            /*
             * =============================================
             * トイレ名
             * =============================================
             */
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
                    FontWeight.Bold,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )


            /*
             * =============================================
             * 緯度経度
             * =============================================
             */
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
                        "緯度 %.5f / 経度 %.5f"
                            .format(

                                toilet.latitude,

                                toilet.longitude
                            ),

                    color =
                        FinderMuted,

                    fontSize =
                        12.sp
                )
            }


            /*
             * =============================================
             * コメント
             * =============================================
             */
            if (
                toilet.comment
                    .isNotBlank()
            ) {


                Surface(

                    modifier =
                        Modifier.fillMaxWidth(),

                    color =
                        Color(
                            0xFFF3F5F4
                        ),

                    shape =
                        RoundedCornerShape(
                            11.dp
                        )

                ) {


                    Text(

                        text =
                            toilet.comment,

                        modifier =
                            Modifier.padding(

                                horizontal =
                                    11.dp,

                                vertical =
                                    8.dp
                            ),

                        color =
                            FinderDark,

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
                    )
                }
            }


            /*
             * =============================================
             * 清潔度・前回清掃
             * =============================================
             */
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


                    /*
                     * 星
                     */
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


                    /*
                     * 前回清掃
                     */
                    Text(

                        text =
                            "前回の清掃完了：$elapsed",

                        color =
                            FinderMuted,

                        fontSize =
                            12.sp
                    )
                }
            }


            /*
             * =============================================
             * 清掃依頼中の案内
             * =============================================
             */
            if (
                requested
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


            /*
             * =============================================
             * 清掃ボタン
             * =============================================
             */
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

                                if (
                                    requested
                                ) {

                                    FinderAmber

                                } else {

                                    FinderGreen
                                },

                            contentColor =

                                if (
                                    requested
                                ) {

                                    FinderDark

                                } else {

                                    Color.White
                                }
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
// Elapsed time since last cleaning
// =============================================

private fun formatElapsedSinceCleaning(

    lastCleanedAtMillis:
    Long?,

    nowMillis:
    Long

): String {


    /*
     * 記録なし
     */
    if (
        lastCleanedAtMillis ==
        null
    ) {

        return "記録なし"
    }


    /*
     * 経過時間
     */
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


    /*
     * 1分未満
     */
    if (
        totalMinutes <
        1L
    ) {

        return "1分未満"
    }


    /*
     * 60分未満
     */
    if (
        totalMinutes <
        60L
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


    /*
     * 日
     */
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