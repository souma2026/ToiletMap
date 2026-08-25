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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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


/*
 * =====================================
 * 端末の現在地取得状態
 * =====================================
 */
enum class DeviceLocationStatus {
    CHECKING,
    AVAILABLE,
    PERMISSION_DENIED,
    DEVICE_LOCATION_OFF,
    WAITING_FOR_SIGNAL
}


/*
 * =====================================
 * 色
 * =====================================
 */
private val FinderGreen = Color(0xFF0B8377)
private val FinderDark = Color(0xFF12313A)
private val FinderMuted = Color(0xFF748186)
private val FinderPale = Color(0xFFF5F8F7)
private val FinderAmber = Color(0xFFF2B544)
private val FinderRed = Color(0xFFD94B4B)
private val FinderSoftGreen = Color(0xFFE5F4F1)
private val FinderBorder = Color(0xFFD7DEDC)


/*
 * =====================================
 * Map画面
 * =====================================
 */
@Composable
fun MapScreen(
    mapView: MapView,

    /*
     * 検索対象
     */
    toilets: List<Toilet> = emptyList(),

    /*
     * 検索結果を選択
     */
    onSearchToiletSelected: (Toilet) -> Unit = {},

    /*
     * 端末の現在地取得状態
     */
    locationStatus:
    DeviceLocationStatus =
        DeviceLocationStatus.CHECKING,

    /*
     * 現在地
     */
    onCurrentLocationClick: () -> Unit = {},

    /*
     * トイレ追加位置選択中
     */
    isSelectingLocation: Boolean = false,

    /*
     * 選択中トイレ
     */
    selectedToilet: Toilet? = null,

    /*
     * 自分が登録したトイレのみ削除可能
     */
    canDeleteSelectedToilet: Boolean = false,

    onDismissSelectedToilet: () -> Unit = {},

    onRequestCleaning: (Toilet, Int) -> Unit = { _, _ -> },

    onMarkCleaned: (Toilet) -> Unit = {},

    onDeleteToilet: (Toilet) -> Unit = {},

    /*
     * 口コミ投稿画面を開く
     */
    onOpenReviews: (Toilet) -> Unit = {},

    onLocationSelected: (Double, Double) -> Unit = { _, _ -> },

    onCancelLocationSelection: () -> Unit = {}
) {

    /*
     * 通知
     */
    var showNotificationDialog by remember {
        mutableStateOf(false)
    }

    var showLocationStatusDialog by remember {
        mutableStateOf(false)
    }

    /*
     * 詳細カード高さ
     */
    var detailCardHeightPx by remember {
        mutableIntStateOf(0)
    }


    /*
     * =====================================
     * 選択したトイレへカメラ移動
     * =====================================
     */
    LaunchedEffect(
        selectedToilet?.id,
        detailCardHeightPx
    ) {

        val toilet =
            selectedToilet
                ?: return@LaunchedEffect

        if (detailCardHeightPx <= 0) {
            return@LaunchedEffect
        }

        mapView.getMapAsync { map ->

            val currentZoom =
                map.cameraPosition.zoom

            val targetZoom =
                when {
                    currentZoom < 16.5 -> 16.5
                    currentZoom < 17.5 -> currentZoom + 0.8
                    else -> currentZoom
                }

            val cameraPosition =
                CameraPosition.Builder()
                    .target(
                        LatLng(
                            toilet.latitude,
                            toilet.longitude
                        )
                    )
                    .zoom(targetZoom)
                    .padding(
                        0.0,
                        0.0,
                        0.0,
                        detailCardHeightPx.toDouble()
                    )
                    .build()

            map.easeCamera(
                CameraUpdateFactory.newCameraPosition(
                    cameraPosition
                ),
                700
            )
        }
    }


    /*
     * =====================================
     * トイレ追加時の地図タップ
     * =====================================
     */
    DisposableEffect(
        mapView,
        isSelectingLocation
    ) {

        var disposed = false

        var targetMap: MapLibreMap? = null

        var clickListener:
                MapLibreMap.OnMapClickListener? = null


        if (isSelectingLocation) {

            mapView.getMapAsync { map ->

                if (!disposed) {

                    targetMap =
                        map

                    val listener =
                        MapLibreMap.OnMapClickListener { point ->

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


    /*
     * =====================================
     * 画面全体
     * =====================================
     */
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    FinderPale
                )
    ) {

        /*
         * =====================================
         * 検索ヘッダー
         * =====================================
         */
        FinderHeader(
            toilets = toilets,

            onToiletSelected =
                onSearchToiletSelected,

            onNotificationClick = {
                showNotificationDialog =
                    true
            },

            modifier =
                Modifier.fillMaxWidth()
        )


        /*
         * =====================================
         * 地図
         * =====================================
         */
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
        ) {

            AndroidView(
                factory = {

                    /*
                     * 前の親Viewから外す
                     */
                    (mapView.parent as? ViewGroup)
                        ?.removeView(
                            mapView
                        )

                    /*
                     * 検索欄のフォーカスを
                     * MapViewに奪わせない
                     */
                    mapView.isFocusable =
                        false

                    mapView.isFocusableInTouchMode =
                        false

                    mapView
                },

                modifier =
                    Modifier.fillMaxSize()
            )


            /*
             * 地図操作
             */
            MapControls(
                onZoomIn = {

                    mapView.getMapAsync { map ->

                        map.animateCamera(
                            CameraUpdateFactory.zoomIn()
                        )
                    }
                },

                onZoomOut = {

                    mapView.getMapAsync { map ->

                        map.animateCamera(
                            CameraUpdateFactory.zoomOut()
                        )
                    }
                },

                locationStatus =
                    locationStatus,

                onCurrentLocation = {
                    if (
                        locationStatus !=
                        DeviceLocationStatus.AVAILABLE
                    ) {
                        showLocationStatusDialog =
                            true
                    }

                    onCurrentLocationClick()
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
             * =====================================
             * 場所選択中
             * =====================================
             */
            if (isSelectingLocation) {

                LocationSelectionBanner(
                    onCancel =
                        onCancelLocationSelection,

                    modifier =
                        Modifier
                            .align(
                                Alignment.TopCenter
                            )
                            .padding(
                                top = 12.dp,
                                start = 14.dp,
                                end = 14.dp
                            )
                )
            }


            /*
             * =====================================
             * トイレ詳細
             * =====================================
             */
            if (
                !isSelectingLocation &&
                selectedToilet != null
            ) {

                ToiletDetailCard(
                    toilet =
                        selectedToilet,

                    canDelete =
                        canDeleteSelectedToilet,

                    onDismiss =
                        onDismissSelectedToilet,

                    onRequestCleaning = {
                            rewardPoints ->

                        onRequestCleaning(
                            selectedToilet,
                            rewardPoints
                        )
                    },

                    onMarkCleaned = {
                        onMarkCleaned(
                            selectedToilet
                        )
                    },

                    onDelete = {
                        onDeleteToilet(
                            selectedToilet
                        )
                    },

                    onOpenReviews = {
                        onOpenReviews(
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
    }


    /*
     * =====================================
     * 現在地状態ダイアログ
     * =====================================
     */
    if (
        showLocationStatusDialog
    ) {
        val locationMessage =
            when (
                locationStatus
            ) {
                DeviceLocationStatus.CHECKING ->
                    "現在地を確認しています。数秒待ってからもう一度お試しください。"

                DeviceLocationStatus.AVAILABLE ->
                    "現在地を正常に取得できています。"

                DeviceLocationStatus.PERMISSION_DENIED ->
                    "ToiletMapへの位置情報の許可がありません。現在地ボタンを押して、位置情報を許可してください。"

                DeviceLocationStatus.DEVICE_LOCATION_OFF ->
                    "スマホ本体の位置情報機能がOFFです。端末の設定から位置情報をONにしてください。Googleマップでも現在地が表示されない場合は、この状態の可能性が高いです。"

                DeviceLocationStatus.WAITING_FOR_SIGNAL ->
                    "スマホ本体の位置情報はONですが、現在地データを取得できていません。屋外などGPS信号を受信しやすい場所で試すか、端末の位置情報機能を一度OFF/ONしてください。エミュレータの場合は位置情報を設定してください。"
            }

        AlertDialog(
            onDismissRequest = {
                showLocationStatusDialog =
                    false
            },
            title = {
                Text(
                    text = "現在地の状態",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = locationMessage
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationStatusDialog =
                            false
                    }
                ) {
                    Text(
                        text = "閉じる",
                        color = FinderGreen
                    )
                }
            }
        )
    }


    /*
     * =====================================
     * 通知ダイアログ
     * =====================================
     */
    if (showNotificationDialog) {

        AlertDialog(
            onDismissRequest = {
                showNotificationDialog =
                    false
            },

            title = {
                Text(
                    text = "通知",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    text = "現在、新しい通知はありません。"
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
                        text = "OK",
                        color = FinderGreen
                    )
                }
            }
        )
    }
}


/*
 * =====================================
 * 検索ヘッダー
 * =====================================
 */
@Composable
private fun FinderHeader(
    toilets: List<Toilet>,
    onToiletSelected: (Toilet) -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    /*
     * =====================================
     * 検索文字
     * =====================================
     *
     * StringではなくTextFieldValueを保持。
     *
     * 日本語IMEの変換途中の状態も
     * そのまま保持する。
     */
    var searchValue by remember {

        mutableStateOf(
            TextFieldValue("")
        )
    }


    /*
     * フォーカス状態
     */
    var searchFocused by remember {

        mutableStateOf(
            false
        )
    }


    val focusManager =
        LocalFocusManager.current


    val searchQuery =
        searchValue.text


    /*
     * =====================================
     * 検索
     * =====================================
     */
    val searchResults =
        remember(
            searchQuery,
            toilets
        ) {

            val query =
                searchQuery.trim()

            if (query.isBlank()) {

                emptyList()

            } else {

                toilets
                    .filter { toilet ->

                        toilet.name.contains(
                            query,
                            ignoreCase = true
                        ) ||

                                toilet.comment.contains(
                                    query,
                                    ignoreCase = true
                                )
                    }
                    .take(10)
            }
        }


    /*
     * =====================================
     * 検索結果選択
     * =====================================
     */
    fun selectToilet(
        toilet: Toilet
    ) {

        searchValue =
            TextFieldValue(
                text = toilet.name,

                selection =
                    TextRange(
                        toilet.name.length
                    )
            )


        /*
         * キーボードを閉じる
         */
        focusManager.clearFocus()


        searchFocused =
            false


        /*
         * MainActivityへ渡す
         */
        onToiletSelected(
            toilet
        )
    }


    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .zIndex(10f),

        color =
            Color(0xFFF9FBFA),

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

            /*
             * =====================================
             * タイトル部分
             * =====================================
             */
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                /*
                 * WC
                 */
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .background(
                                color = FinderGreen,
                                shape = RoundedCornerShape(
                                    13.dp
                                )
                            ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "WC",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }


                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )


                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text = "TOILET FINDER",
                        color = FinderGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = "近くのトイレ",
                        color = FinderDark,
                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }


                /*
                 * 通知
                 */
                Box(
                    modifier =
                        Modifier
                            .size(46.dp)
                            .shadow(
                                4.dp,
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
             * =====================================
             * 検索欄
             * =====================================
             */
            OutlinedTextField(
                value =
                    searchValue,

                /*
                 * 日本語入力では
                 * newValueを加工せずそのまま保存
                 */
                onValueChange = {
                        newValue ->

                    searchValue =
                        newValue
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                                focusState ->

                            searchFocused =
                                focusState.isFocused
                        },

                singleLine =
                    true,

                placeholder = {
                    Text(
                        text = "場所や施設名を検索"
                    )
                },

                leadingIcon = {

                    Icon(
                        imageVector =
                            Icons
                                .Outlined
                                .Search,

                        contentDescription =
                            "検索",

                        tint =
                            FinderMuted
                    )
                },

                /*
                 * =====================================
                 * ×
                 * =====================================
                 */
                trailingIcon = {

                    if (searchQuery.isNotEmpty()) {

                        IconButton(
                            onClick = {

                                searchValue =
                                    TextFieldValue("")
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons
                                        .Outlined
                                        .Close,

                                contentDescription =
                                    "検索文字を削除",

                                tint =
                                    FinderMuted
                            )
                        }
                    }
                },

                keyboardOptions =
                    KeyboardOptions(
                        imeAction =
                            ImeAction.Search
                    ),

                keyboardActions =
                    KeyboardActions(
                        onSearch = {

                            val firstResult =
                                searchResults
                                    .firstOrNull()

                            if (firstResult != null) {

                                selectToilet(
                                    firstResult
                                )

                            } else {

                                focusManager
                                    .clearFocus()
                            }
                        }
                    ),

                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor =
                            FinderGreen,

                        unfocusedBorderColor =
                            FinderBorder,

                        focusedContainerColor =
                            Color.White,

                        unfocusedContainerColor =
                            Color.White,

                        cursorColor =
                            FinderGreen
                    )
            )


            /*
             * =====================================
             * 検索候補
             * =====================================
             */
            if (
                searchFocused &&
                searchQuery.isNotBlank()
            ) {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(
                            16.dp
                        ),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color.White
                        ),

                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation =
                                4.dp
                        )
                ) {

                    if (searchResults.isEmpty()) {

                        Text(
                            text =
                                "該当するトイレがありません",

                            modifier =
                                Modifier.padding(
                                    16.dp
                                ),

                            color =
                                FinderMuted,

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium
                        )

                    } else {

                        LazyColumn(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(
                                        max = 280.dp
                                    )
                        ) {

                            items(
                                items =
                                    searchResults,

                                key = {
                                        toilet ->
                                    toilet.id
                                }
                            ) {
                                    toilet ->

                                SearchResultItem(
                                    toilet =
                                        toilet,

                                    onClick = {
                                        selectToilet(
                                            toilet
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


/*
 * =====================================
 * 検索結果
 * =====================================
 */
@Composable
private fun SearchResultItem(
    toilet: Toilet,
    onClick: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 13.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier =
                Modifier
                    .size(38.dp)
                    .background(
                        color = FinderSoftGreen,
                        shape = CircleShape
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
                Modifier.width(12.dp)
        )


        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text =
                    toilet.name,

                color =
                    FinderDark,

                fontWeight =
                    FontWeight.Bold,

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )


            if (toilet.comment.isNotBlank()) {

                Text(
                    text =
                        toilet.comment,

                    color =
                        FinderMuted,

                    fontSize =
                        12.sp,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )

            } else {

                Text(
                    text =
                        "緯度 %.4f / 経度 %.4f".format(
                            toilet.latitude,
                            toilet.longitude
                        ),

                    color =
                        FinderMuted,

                    fontSize =
                        12.sp
                )
            }
        }
    }
}


/*
 * =====================================
 * 地図操作
 * =====================================
 */
@Composable
private fun MapControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    locationStatus: DeviceLocationStatus,
    onCurrentLocation: () -> Unit,
    modifier: Modifier = Modifier
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
         * 現在地
         *
         * 正常       : 緑
         * 確認中     : グレー
         * 権限なし   : 赤 + !
         * 本体OFF    : 赤 + !
         * 測位不能   : オレンジ + !
         */
        val locationIconColor =
            when (
                locationStatus
            ) {
                DeviceLocationStatus.AVAILABLE ->
                    FinderGreen

                DeviceLocationStatus.CHECKING ->
                    Color(0xFF8A8A8A)

                DeviceLocationStatus.PERMISSION_DENIED,
                DeviceLocationStatus.DEVICE_LOCATION_OFF ->
                    Color(0xFFD32F2F)

                DeviceLocationStatus.WAITING_FOR_SIGNAL ->
                    Color(0xFFF57C00)
            }

        val showWarningBadge =
            locationStatus == DeviceLocationStatus.PERMISSION_DENIED ||
                    locationStatus == DeviceLocationStatus.DEVICE_LOCATION_OFF ||
                    locationStatus == DeviceLocationStatus.WAITING_FOR_SIGNAL

        val warningBadgeColor =
            if (
                locationStatus ==
                DeviceLocationStatus.WAITING_FOR_SIGNAL
            ) {
                Color(0xFFF57C00)
            } else {
                Color(0xFFD32F2F)
            }

        Box {
            Box(
                modifier =
                    Modifier
                        .size(52.dp)
                        .shadow(
                            7.dp,
                            RoundedCornerShape(18.dp)
                        )
                        .clip(
                            RoundedCornerShape(18.dp)
                        )
                        .background(
                            Color.White
                        )
                        .clickable(
                            onClick = onCurrentLocation
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.LocationOn,
                    contentDescription =
                        when (
                            locationStatus
                        ) {
                            DeviceLocationStatus.AVAILABLE ->
                                "現在地：正常"
                            DeviceLocationStatus.CHECKING ->
                                "現在地：確認中"
                            DeviceLocationStatus.PERMISSION_DENIED ->
                                "現在地：位置情報権限なし"
                            DeviceLocationStatus.DEVICE_LOCATION_OFF ->
                                "現在地：スマホ本体の位置情報OFF"
                            DeviceLocationStatus.WAITING_FOR_SIGNAL ->
                                "現在地：位置情報を取得できません"
                        },
                    tint =
                        locationIconColor,
                    modifier =
                        Modifier.size(25.dp)
                )
            }

            if (
                showWarningBadge
            ) {
                Surface(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(19.dp),
                    shape = CircleShape,
                    color = warningBadgeColor,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "!",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }


        /*
         * + / -
         */
        Column(
            modifier =
                Modifier
                    .shadow(
                        7.dp,
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

            Box(
                modifier =
                    Modifier
                        .size(
                            width = 52.dp,
                            height = 50.dp
                        )
                        .clickable(
                            onClick =
                                onZoomIn
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "+",
                    color = FinderDark,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Light
                )
            }


            Box(
                modifier =
                    Modifier
                        .width(28.dp)
                        .height(1.dp)
                        .background(
                            Color(
                                0xFFE3E9E7
                            )
                        )
            )


            Box(
                modifier =
                    Modifier
                        .size(
                            width = 52.dp,
                            height = 50.dp
                        )
                        .clickable(
                            onClick =
                                onZoomOut
                        ),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "−",
                    color = FinderDark,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}


/*
 * =====================================
 * 位置選択
 * =====================================
 */
@Composable
private fun LocationSelectionBanner(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(
                    7.dp,
                    RoundedCornerShape(
                        20.dp
                    )
                ),

        shape =
            RoundedCornerShape(
                20.dp
            ),

        colors =
            CardDefaults.cardColors(
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
                        .size(38.dp)
                        .background(
                            color = FinderSoftGreen,
                            shape = CircleShape
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
                    Modifier.width(10.dp)
            )


            Column(
                modifier =
                    Modifier.weight(1f)
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


/*
 * =====================================
 * トイレ詳細
 * =====================================
 */
@Composable
private fun ToiletDetailCard(
    toilet: Toilet,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onRequestCleaning: (Int) -> Unit,
    onMarkCleaned: () -> Unit,
    onDelete: () -> Unit,
    onOpenReviews: () -> Unit,
    modifier: Modifier = Modifier
) {

    /*
     * 現在時刻
     */
    var nowMillis by remember(
        toilet.id,
        toilet.lastCleanedAtMillis
    ) {

        mutableLongStateOf(
            System.currentTimeMillis()
        )
    }


    /*
     * 1分ごと更新
     */
    LaunchedEffect(
        toilet.id,
        toilet.lastCleanedAtMillis
    ) {

        while (true) {

            nowMillis =
                System.currentTimeMillis()

            /*
             * IDEで
             * Legacy Long overload...
             * と表示されることがありますが、
             * 警告でありエラーではありません。
             */
            delay(60_000L)
        }
    }


    val cleanliness =
        toilet.cleanliness
            .coerceIn(
                0,
                5
            )


    val elapsed =
        formatElapsedSinceCleaning(
            lastCleanedAtMillis =
                toilet.lastCleanedAtMillis,

            nowMillis =
                nowMillis
        )


    val requested =
        toilet.cleaningStatus ==
                CleaningStatus.REQUESTED


    val statusText =
        if (requested) {
            "清掃待ち"
        } else {
            "通常"
        }


    val statusColor =
        if (requested) {
            FinderAmber
        } else {
            FinderRed
        }


    val actionText =
        if (requested) {
            "清掃しました（${toilet.cleaningRewardPoints} pt獲得）"
        } else {
            "清掃を依頼する"
        }


    val actionIcon =
        if (requested) {
            Icons.Outlined.CheckCircle
        } else {
            Icons.Outlined.NotificationsNone
        }


    var showRewardDialog by remember(
        toilet.id
    ) {
        mutableStateOf(false)
    }


    var rewardPointText by remember(
        toilet.id
    ) {
        mutableStateOf("10")
    }


    val enteredRewardPoints =
        rewardPointText.toIntOrNull()


    val rewardPointsValid =
        enteredRewardPoints != null &&
                enteredRewardPoints in 1..10_000


    if (showRewardDialog && !requested) {

        AlertDialog(
            onDismissRequest = {
                showRewardDialog = false
            },

            title = {
                Text(
                    text = "清掃依頼の報酬ポイント"
                )
            },

            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {

                    Text(
                        text =
                            "清掃を完了したユーザーへ支払うポイント数を入力してください。依頼時にあなたの所持ポイントから差し引かれます。"
                    )


                    OutlinedTextField(
                        value =
                            rewardPointText,

                        onValueChange = {
                                newValue ->

                            rewardPointText =
                                newValue
                                    .filter {
                                        it.isDigit()
                                    }
                                    .take(5)
                        },

                        label = {
                            Text(
                                "支払うポイント"
                            )
                        },

                        suffix = {
                            Text(
                                "pt"
                            )
                        },

                        singleLine = true,

                        isError =
                            rewardPointText.isNotBlank() &&
                                    !rewardPointsValid,

                        supportingText = {
                            Text(
                                if (rewardPointsValid) {
                                    "1～10000ptの範囲で設定できます"
                                } else {
                                    "1～10000の整数を入力してください"
                                }
                            )
                        },

                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number,

                                imeAction =
                                    ImeAction.Done
                            )
                    )
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {

                        val rewardPoints =
                            enteredRewardPoints
                                ?: return@TextButton

                        if (!rewardPointsValid) {
                            return@TextButton
                        }

                        showRewardDialog = false

                        onRequestCleaning(
                            rewardPoints
                        )
                    },

                    enabled =
                        rewardPointsValid
                ) {
                    Text(
                        "${enteredRewardPoints ?: 0} ptで依頼する"
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showRewardDialog = false
                    }
                ) {
                    Text(
                        "キャンセル"
                    )
                }
            }
        )
    }


    var showDeleteDialog by remember(
        toilet.id
    ) {
        mutableStateOf(false)
    }


    if (showDeleteDialog && canDelete) {

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text(
                    text = "このトイレを削除しますか？"
                )
            },

            text = {
                Text(
                    text =
                        "「${toilet.name}」を地図から削除します。この操作は元に戻せません。清掃依頼中の報酬ポイントがある場合は依頼者へ返金されます。"
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "削除する",
                        color = FinderRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        "キャンセル"
                    )
                }
            }
        )
    }


    /*
     * 詳細カードのスクロール状態。
     * 閉じるボタンとスクロール案内は、このスクロール領域の外に置く。
     */
    val detailScrollState =
        rememberScrollState()

    /*
     * スクロール案内は最初の1回だけ表示する。
     * ユーザーが少しでもスクロールしたら、そのトイレの詳細を閉じるまで再表示しない。
     */
    var hasUserScrolledDetail by
        remember(toilet.id) {
            mutableStateOf(false)
        }

    LaunchedEffect(
        toilet.id,
        detailScrollState.value
    ) {
        if (detailScrollState.value > 0) {
            hasUserScrolledDetail = true
        }
    }


    Card(
        modifier =
            modifier
                .heightIn(
                    max = 300.dp
                )
                .shadow(
                    14.dp,
                    RoundedCornerShape(
                        24.dp
                    )
                ),

        shape =
            RoundedCornerShape(
                24.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    4.dp
            )
    ) {

        Box(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(
                            detailScrollState
                        )
                        .padding(
                            start = 18.dp,
                            top = 18.dp,
                            end = 18.dp,
                            bottom = 62.dp
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

            /*
             * ハンドル
             */
            Box(
                modifier =
                    Modifier
                        .align(
                            Alignment.CenterHorizontally
                        )
                        .width(38.dp)
                        .height(4.dp)
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
             * 状態
             */
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    color =
                        statusColor.copy(
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
                        Modifier.weight(1f)
                )

            }


            /*
             * 名前
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
             * 位置
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
                        Modifier.width(5.dp)
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


            /*
             * コメント
             */
            if (toilet.comment.isNotBlank()) {

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
                                horizontal = 11.dp,
                                vertical = 8.dp
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
             * =====================================
             * 清潔度
             * =====================================
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


                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        repeat(5) { index ->

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
             * 清掃依頼中
             */
            if (requested) {

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


                Text(
                    text =
                        "清掃報酬：${toilet.cleaningRewardPoints} pt",

                    color =
                        FinderGreen,

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }


            /*
             * =====================================
             * 清掃ボタン
             * =====================================
             */
            Button(
                onClick = {
                    if (requested) {
                        onMarkCleaned()
                    } else {
                        showRewardDialog = true
                    }
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
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (requested) {
                                FinderAmber
                            } else {
                                FinderGreen
                            },

                        contentColor =
                            if (requested) {
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


            /*
             * =====================================
             * 口コミ投稿ボタン
             * =====================================
             */
            Button(
                onClick =
                    onOpenReviews,

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
                    ButtonDefaults.buttonColors(
                        containerColor =
                            FinderSoftGreen,

                        contentColor =
                            FinderGreen
                    )
            ) {

                Icon(
                    imageVector =
                        Icons
                            .Filled
                            .Star,

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
                        "口コミを投稿",

                    fontWeight =
                        FontWeight.Bold
                )
            }


            /*
             * =====================================
             * トイレ削除ボタン
             * =====================================
             *
             * ログイン中であれば、作成者に関係なく表示する。
             */
            if (canDelete) {

                Button(
                    onClick = {
                        showDeleteDialog = true
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
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFFFFECEC),

                            contentColor =
                                FinderRed
                        )
                ) {

                    Text(
                        text =
                            "このトイレを削除",

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            /*
             * スクロールする詳細内容はここまで。
             */
            }

            /*
             * =====================================
             * 固定の閉じるボタン
             * =====================================
             *
             * スクロール領域の外に置いているため、
             * 詳細をスクロールしても右上から動かない。
             */
            Surface(
                modifier =
                    Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(
                            top = 8.dp,
                            end = 8.dp
                        )
                        .zIndex(
                            2f
                        ),

                shape =
                    CircleShape,

                color =
                    Color.White.copy(
                        alpha = 0.96f
                    ),

                shadowElevation =
                    3.dp
            ) {

                IconButton(
                    onClick =
                        onDismiss,

                    modifier =
                        Modifier.size(
                            38.dp
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
             * =====================================
             * スクロール案内
             * =====================================
             *
             * 最初だけ薄く表示する。
             * 少しでもスクロールしたら、その詳細を閉じるまで再表示しない。
             */
            if (
                detailScrollState.canScrollForward &&
                !hasUserScrolledDetail
            ) {

                Surface(
                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomCenter
                            )
                            .padding(
                                bottom = 8.dp
                            )
                            .zIndex(
                                2f
                            ),

                    shape =
                        RoundedCornerShape(
                            18.dp
                        ),

                    color =
                        Color.White.copy(
                            alpha = 0.72f
                        ),

                    shadowElevation =
                        1.dp
                ) {

                    Text(
                        text =
                            "▼  スクロールして詳細を見る",

                        modifier =
                            Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),

                        color =
                            FinderDark.copy(
                                alpha = 0.52f
                            ),

                        fontSize =
                            11.sp,

                        fontWeight =
                            FontWeight.Medium
                    )
                }
            }
        }
    }
}


/*
 * =====================================
 * 前回清掃からの経過時間
 * =====================================
 *
 * 今回のReturn type mismatchの
 * 原因だった部分。
 *
 * returnと戻り値を同じ文として
 * 書くように修正している。
 * =====================================
 */
private fun formatElapsedSinceCleaning(
    lastCleanedAtMillis: Long?,
    nowMillis: Long
): String {

    /*
     * 記録なし
     */
    if (lastCleanedAtMillis == null) {
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


    /*
     * 1分未満
     */
    if (totalMinutes < 1L) {
        return "1分未満"
    }


    /*
     * 1時間未満
     */
    if (totalMinutes < 60L) {
        return "${totalMinutes}分前"
    }


    val totalHours =
        totalMinutes /
                60L


    val remainingMinutes =
        totalMinutes %
                60L


    /*
     * 24時間未満
     */
    if (totalHours < 24L) {

        return if (remainingMinutes == 0L) {

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


    /*
     * 1日以上
     */
    return if (remainingHours == 0L) {

        "${totalDays}日前"

    } else {

        "${totalDays}日${remainingHours}時間前"
    }
}