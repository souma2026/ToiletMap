package com.example.toiletmap.screen.map

import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.example.toiletmap.model.CleaningRequest
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import com.example.toiletmap.screen.cleaning.formatCleaningDateTime
import com.example.toiletmap.screen.listofuncleaned.rememberCurrentLocationState
import com.example.toiletmap.screen.map.facilities.ToiletFacilityEditor
import kotlinx.coroutines.delay
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import kotlin.math.*


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
private val FinderBlue = Color(0xFF1976D2)
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
     * 隠しゲーム用ロゴタップ
     */
    onSecretLogoTap: () -> Unit = {},

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
     * 選択中トイレの有効な清掃依頼
     */
    cleaningRequest: CleaningRequest? = null,

    currentUserId: String? = null,

    currentRequestPoints: Int = 0,

    isLoadingCleaning: Boolean = false,

    cleaningActionRequestId: String? = null,

    onDismissSelectedToilet: () -> Unit = {},

    onRequestCleaning: (Toilet, Int) -> Unit = { _, _ -> },

    onAcceptCleaning: (CleaningRequest) -> Unit = {},

    onOpenCleaningScreen: () -> Unit = {},

    /*
     * 未ログイン時にアカウント画面を開く
     */
    onOpenAccount: () -> Unit = {},

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
            mapView = mapView,

            toilets = toilets,

            onToiletSelected =
                onSearchToiletSelected,

            onSecretLogoTap =
                onSecretLogoTap,

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

                onCurrentLocation =
                    onCurrentLocationClick,

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

                    cleaningRequest =
                        cleaningRequest,

                    currentUserId =
                        currentUserId,

                    currentRequestPoints =
                        currentRequestPoints,

                    isLoadingCleaning =
                        isLoadingCleaning,

                    isActionInProgress =
                        cleaningActionRequestId != null &&
                                (
                                        cleaningActionRequestId == selectedToilet.id ||
                                                cleaningActionRequestId == cleaningRequest?.id
                                        ),

                    onDismiss =
                        onDismissSelectedToilet,

                    onRequestCleaning = {
                            selectedRequestPoints ->

                        onRequestCleaning(
                            selectedToilet,
                            selectedRequestPoints
                        )
                    },

                    onAcceptCleaning = {
                            request ->

                        onAcceptCleaning(
                            request
                        )
                    },

                    onOpenCleaningScreen =
                        onOpenCleaningScreen,

                    onOpenAccount =
                        onOpenAccount,

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
    mapView: MapView,
    toilets: List<Toilet>,
    onToiletSelected: (Toilet) -> Unit,
    onSecretLogoTap: () -> Unit,
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

    /*
     * 検索候補表示状態
     * フォーカスだけに依存すると候補が残るため分離管理する。
     */
    var showSearchSuggestions by remember {
        mutableStateOf(false)
    }


    val focusManager =
        LocalFocusManager.current


    val searchQuery =
        searchValue.text


    /*
     * =====================================
     * 現在地付近検索用
     *
     * 未清掃画面と同じ現在地取得処理を再利用し、
     * 端末の現在地を基準に近いトイレを表示する。
     * =====================================
     */
    val currentLocationState =
        rememberCurrentLocationState()


    fun distanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {

        val r = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)

        val dLon = Math.toRadians(lon2 - lon1)

        val a =
            sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) *
                    cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) *
                    sin(dLon / 2)

        return r * 2 * atan2(
            sqrt(a),
            sqrt(1 - a)
        )
    }


    /*
     * 距離表示用(m)
     */
    fun distanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Int {

        return (distanceKm(
            lat1,
            lon1,
            lat2,
            lon2
        ) * 1000).toInt()
    }


    /*
     * =====================================
     * 検索
     * =====================================
     */
    val searchResults =
        remember(
            searchQuery,
            toilets,
            currentLocationState.location
        ) {

            val query =
                searchQuery.trim()

            if (query.isBlank()) {

                val currentLocation =
                    currentLocationState.location

                if (currentLocation == null) {

                    emptyList()

                } else {

                    toilets
                        .sortedBy { toilet ->

                            distanceKm(
                                currentLocation.latitude,
                                currentLocation.longitude,
                                toilet.latitude,
                                toilet.longitude
                            )
                        }
                        .take(5)
                }

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

        showSearchSuggestions =
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
                            .clip(
                                RoundedCornerShape(
                                    13.dp
                                )
                            )
                            .background(
                                color = FinderGreen,
                                shape = RoundedCornerShape(
                                    13.dp
                                )
                            )
                            .clickable(
                                onClick =
                                    onSecretLogoTap
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

                    showSearchSuggestions = true
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                                focusState ->

                            searchFocused =
                                focusState.isFocused

                            if (focusState.isFocused) {
                                showSearchSuggestions = true
                            } else {
                                showSearchSuggestions = false
                            }
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

                    Row {

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

                        IconButton(
                            onClick = {

                                focusManager.clearFocus()
                                searchFocused = false
                                showSearchSuggestions = false
                            }
                        ) {

                            Icon(
                                imageVector =
                                    Icons
                                        .Outlined
                                        .Close,

                                contentDescription =
                                    "検索候補を閉じる",

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
                showSearchSuggestions
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
                                "表示できるトイレがありません",

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

                                    distanceText =
                                        currentLocationState.location?.let { location ->

                                            val meters =
                                                distanceMeters(
                                                    location.latitude,
                                                    location.longitude,
                                                    toilet.latitude,
                                                    toilet.longitude
                                                )

                                            if (meters >= 1000) {
                                                "%.1fkm".format(meters / 1000.0)
                                            } else {
                                                "${meters}m"
                                            }

                                        },

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
    distanceText: String? = null,
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

            }

            if (distanceText != null) {

                Text(
                    text =
                        "現在地から $distanceText",

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
         */
        Box(
            modifier =
                Modifier
                    .size(52.dp)
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
    cleaningRequest: CleaningRequest?,
    currentUserId: String?,
    currentRequestPoints: Int,
    isLoadingCleaning: Boolean,
    isActionInProgress: Boolean,
    onDismiss: () -> Unit,
    onRequestCleaning: (Int) -> Unit,
    onAcceptCleaning: (CleaningRequest) -> Unit,
    onOpenCleaningScreen: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenReviews: () -> Unit,
    modifier: Modifier = Modifier
) {

    var showRequestPointDialog by
    remember(
        toilet.id
    ) {
        mutableStateOf(
            false
        )
    }


    var selectedRequestPoints by
    remember(
        toilet.id,
        currentRequestPoints
    ) {
        mutableIntStateOf(
            preferredCleaningRequestPoints(
                currentRequestPoints
            )
        )
    }


    if (showRequestPointDialog) {

        CleaningRequestPointDialog(
            currentRequestPoints =
                currentRequestPoints,

            selectedRequestPoints =
                selectedRequestPoints,

            onRequestPointsSelected = {
                    points ->

                selectedRequestPoints =
                    points
            },

            onDismiss = {

                showRequestPointDialog =
                    false
            },

            onConfirm = {

                showRequestPointDialog =
                    false

                onRequestCleaning(
                    selectedRequestPoints
                )
            }
        )
    }


    var nowMillis by remember(
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

        while (true) {

            nowMillis =
                System.currentTimeMillis()

            delay(
                60_000L
            )
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


    /*
     * cleaning_requests の状態を優先する。
     * RPC直後に toilets の再読込が完了するまでの間も、
     * 詳細カードを正しい状態で表示するため。
     */
    val cleaningStatus =
        cleaningRequest?.status
            ?: toilet.cleaningStatus


    val isRequester =
        currentUserId != null &&
                cleaningRequest?.requesterId == currentUserId


    val isCleaner =
        currentUserId != null &&
                cleaningRequest?.cleanerId == currentUserId


    /*
     * 清掃依頼・引受はログイン必須。
     * Repository側にも認証チェックを残し、UIとデータ層の二重で防ぐ。
     */
    val isLoggedIn =
        currentUserId != null


    val statusText =
        when (cleaningStatus) {

            CleaningStatus.NORMAL ->
                "通常"

            CleaningStatus.REQUESTED ->
                "清掃依頼中"

            CleaningStatus.IN_PROGRESS ->
                "清掃中"

            CleaningStatus.COMPLETED ->
                "清掃完了"
        }


    val statusColor =
        when (cleaningStatus) {

            CleaningStatus.NORMAL ->
                FinderRed

            CleaningStatus.REQUESTED ->
                FinderAmber

            CleaningStatus.IN_PROGRESS ->
                FinderBlue

            CleaningStatus.COMPLETED ->
                FinderGreen
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

            /*
             * =====================================
             * スクロールする詳細内容
             * =====================================
             */
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
                            Modifier.weight(
                                1f
                            )
                    )

                }


                Text(
                    text =
                        toilet.name,

                    color =
                        FinderDark,

                    style =
                        MaterialTheme.typography.titleLarge,

                    fontWeight =
                        FontWeight.Bold,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )


                if (toilet.sourceType == "USER" && toilet.comment.isNotBlank()) {

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
                                MaterialTheme.typography.bodyMedium
                        )
                    }
                }


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
                                        Icons.Filled.Star,

                                    contentDescription =
                                        null,

                                    tint =
                                        if (index < cleanliness) {

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
                 * =====================================
                 * 設備情報
                 *
                 * 閲覧
                 * 編集
                 * ログイン判定
                 * Supabase保存
                 * =====================================
                 */
                ToiletFacilityEditor(

                    toilet =
                        toilet,

                    currentUserId =
                        currentUserId,

                    onOpenAccount =
                        onOpenAccount
                )


                when (cleaningStatus) {

                    CleaningStatus.NORMAL -> {

                        CleaningStatusNotice(
                            message =
                                if (isLoggedIn) {
                                    "清掃依頼ポイントを1pt・3pt・5ptから選べます。清掃報酬は選択ポイントより2pt多くなります。"
                                } else {
                                    "清掃を依頼するにはログインが必要です。"
                                },

                            backgroundColor =
                                if (isLoggedIn) {
                                    FinderSoftGreen
                                } else {
                                    Color(0xFFF3F5F4)
                                },

                            textColor =
                                if (isLoggedIn) {
                                    FinderGreen
                                } else {
                                    FinderMuted
                                }
                        )
                    }


                    CleaningStatus.REQUESTED -> {

                        CleaningStatusNotice(
                            message =
                                when {
                                    !isLoggedIn ->
                                        "このトイレは清掃担当者を募集しています。引き受けるにはログインが必要です。"

                                    isRequester ->
                                        "自分が出した清掃依頼です。別のユーザーが引き受けるまでお待ちください。"

                                    else ->
                                        "このトイレは清掃担当者を募集しています。"
                                },

                            backgroundColor =
                                Color(
                                    0xFFFFF8E6
                                ),

                            textColor =
                                Color(
                                    0xFF7A6200
                                )
                        )


                        if (cleaningRequest != null) {

                            CleaningRequestInfo(
                                label =
                                    "依頼日時",

                                value =
                                    formatCleaningDateTime(
                                        cleaningRequest.requestedAt
                                    )
                            )


                            CleaningRequestInfo(
                                label =
                                    "使用した依頼ポイント",

                                value =
                                    "${cleaningRequest.requestPointsUsed} pt"
                            )


                            CleaningRequestInfo(
                                label =
                                    "予定報酬",

                                value =
                                    "${cleaningRequest.rewardPoints} pt"
                            )
                        } else {

                            CleaningRequestInfo(
                                label =
                                    "予定報酬",

                                value =
                                    "5 pt"
                            )
                        }
                    }


                    CleaningStatus.IN_PROGRESS -> {

                        CleaningStatusNotice(
                            message =
                                if (isCleaner) {
                                    "あなたがこの清掃を担当しています。清掃画面から担当状況を確認できます。"
                                } else {
                                    "現在、ほかのユーザーが清掃中です。"
                                },

                            backgroundColor =
                                Color(
                                    0xFFEAF2FD
                                ),

                            textColor =
                                FinderBlue
                        )


                        CleaningRequestInfo(
                            label =
                                "引受日時",

                            value =
                                formatCleaningDateTime(
                                    cleaningRequest?.acceptedAt
                                )
                        )


                        CleaningRequestInfo(
                            label =
                                "使用した依頼ポイント",

                            value =
                                "${cleaningRequest?.requestPointsUsed ?: 3} pt"
                        )


                        CleaningRequestInfo(
                            label =
                                "予定報酬",

                            value =
                                "${cleaningRequest?.rewardPoints ?: 5} pt"
                        )
                    }


                    CleaningStatus.COMPLETED -> {

                        CleaningStatusNotice(
                            message =
                                "清掃が完了しました。トイレの状態を更新しています。",

                            backgroundColor =
                                FinderSoftGreen,

                            textColor =
                                FinderGreen
                        )
                    }
                }


                when (cleaningStatus) {

                    CleaningStatus.NORMAL -> {

                        CleaningActionButton(
                            text =
                                when {
                                    !isLoggedIn ->
                                        "ログインして清掃を依頼"

                                    isLoadingCleaning ->
                                        "依頼ポイントを確認中"

                                    isActionInProgress ->
                                        "清掃依頼を送信中"

                                    else ->
                                        "清掃を依頼する"
                                },

                            icon =
                                Icons.Outlined.NotificationsNone,

                            containerColor =
                                FinderGreen,

                            contentColor =
                                Color.White,

                            isLoading =
                                isLoggedIn && isActionInProgress,

                            enabled =
                                !isLoggedIn ||
                                        (
                                                !isLoadingCleaning &&
                                                        !isActionInProgress
                                                ),

                            onClick = {

                                if (
                                    isLoggedIn &&
                                    !isLoadingCleaning
                                ) {

                                    selectedRequestPoints =
                                        preferredCleaningRequestPoints(
                                            currentRequestPoints
                                        )

                                    showRequestPointDialog =
                                        true

                                } else {
                                    onOpenAccount()
                                }
                            }
                        )
                    }


                    CleaningStatus.REQUESTED -> {

                        val request =
                            cleaningRequest


                        CleaningActionButton(
                            text =
                                when {

                                    !isLoggedIn ->
                                        "ログインして清掃を引き受ける"

                                    request == null ->
                                        "清掃依頼を読み込み中"

                                    isRequester ->
                                        "自分の清掃依頼です"

                                    isActionInProgress ->
                                        "清掃を引受中"

                                    else ->
                                        "清掃を引き受ける"
                                },

                            icon =
                                Icons.Outlined.CleaningServices,

                            containerColor =
                                FinderAmber,

                            contentColor =
                                FinderDark,

                            isLoading =
                                isLoggedIn && isActionInProgress,

                            enabled =
                                if (!isLoggedIn) {
                                    true
                                } else {
                                    request != null &&
                                            !isRequester &&
                                            !isActionInProgress
                                },

                            onClick = {

                                if (!isLoggedIn) {

                                    onOpenAccount()

                                } else if (request != null) {

                                    onAcceptCleaning(
                                        request
                                    )
                                }
                            }
                        )
                    }


                    CleaningStatus.IN_PROGRESS -> {

                        CleaningActionButton(
                            text =
                                if (isCleaner) {
                                    "清掃画面を開く"
                                } else {
                                    "ほかのユーザーが清掃中"
                                },

                            icon =
                                Icons.Outlined.CheckCircle,

                            containerColor =
                                FinderBlue,

                            contentColor =
                                Color.White,

                            isLoading =
                                false,

                            enabled =
                                isCleaner,

                            onClick =
                                onOpenCleaningScreen
                        )
                    }


                    CleaningStatus.COMPLETED -> {

                        CleaningActionButton(
                            text =
                                "清掃完了",

                            icon =
                                Icons.Outlined.CheckCircle,

                            containerColor =
                                FinderGreen,

                            contentColor =
                                Color.White,

                            isLoading =
                                false,

                            enabled =
                                false,

                            onClick = {}
                        )
                    }
                }


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
                            Icons.Filled.Star,

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
                 * スクロールする詳細内容はここまで。
                 */
            }

            /*
             * =====================================
             * 固定の閉じるボタン
             * =====================================
             *
             * スクロール領域の外に置くことで、
             * 詳細を下までスクロールしても
             * 右上から動かず、いつでも閉じられる。
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
                            5f
                        ),

                shape =
                    CircleShape,

                color =
                    Color.White.copy(
                        alpha = 0.96f
                    ),

                shadowElevation =
                    4.dp
            ) {

                IconButton(
                    onClick =
                        onDismiss,

                    modifier =
                        Modifier.size(
                            40.dp
                        )
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.Close,

                        contentDescription =
                            "閉じる",

                        tint =
                            FinderDark
                    )
                }
            }
        }
    }
}


/*
 * =====================================
 * 清掃依頼ポイント選択
 * =====================================
 */
private data class CleaningRequestPointOption(
    val label: String,
    val requestPoints: Int
) {

    val rewardPoints: Int
        get() =
            requestPoints + 2
}


private val CleaningRequestPointOptions =
    listOf(
        CleaningRequestPointOption(
            label =
                "通常依頼",

            requestPoints =
                1
        ),

        CleaningRequestPointOption(
            label =
                "優先依頼",

            requestPoints =
                3
        ),

        CleaningRequestPointOption(
            label =
                "高優先依頼",

            requestPoints =
                5
        )
    )


private fun preferredCleaningRequestPoints(
    currentRequestPoints: Int
): Int {

    return when {

        currentRequestPoints >= 3 ->
            3

        currentRequestPoints >= 1 ->
            1

        else ->
            0
    }
}


@Composable
private fun CleaningRequestPointDialog(
    currentRequestPoints: Int,
    selectedRequestPoints: Int,
    onRequestPointsSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {

    val selectedOption =
        CleaningRequestPointOptions
            .firstOrNull {
                it.requestPoints ==
                        selectedRequestPoints
            }


    val canConfirm =
        selectedOption != null &&
                currentRequestPoints >=
                selectedRequestPoints


    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "清掃依頼を出す",

                color =
                    FinderDark,

                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {

            Column(
                modifier =
                    Modifier
                        .heightIn(
                            max = 520.dp
                        )
                        .verticalScroll(
                            rememberScrollState()
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),

                    color =
                        FinderSoftGreen,

                    shape =
                        RoundedCornerShape(
                            12.dp
                        )
                ) {

                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 12.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text =
                                "現在の依頼ポイント",

                            modifier =
                                Modifier.weight(
                                    1f
                                ),

                            color =
                                FinderDark,

                            fontSize =
                                13.sp
                        )


                        Text(
                            text =
                                "${currentRequestPoints} pt",

                            color =
                                FinderGreen,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }


                Text(
                    text =
                        "使用する依頼ポイントを選んでください。使用ポイントが多いほど、清掃する人の報酬も増えます。",

                    color =
                        FinderMuted,

                    fontSize =
                        13.sp
                )


                CleaningRequestPointOptions
                    .forEach {
                            option ->

                        val isEnabled =
                            currentRequestPoints >=
                                    option.requestPoints

                        val isSelected =
                            selectedRequestPoints ==
                                    option.requestPoints


                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        enabled =
                                            isEnabled,

                                        onClick = {

                                            onRequestPointsSelected(
                                                option.requestPoints
                                            )
                                        }
                                    ),

                            color =
                                when {

                                    isSelected ->
                                        FinderSoftGreen

                                    isEnabled ->
                                        Color.White

                                    else ->
                                        Color(
                                            0xFFF3F5F4
                                        )
                                },

                            shape =
                                RoundedCornerShape(
                                    12.dp
                                ),

                            border =
                                BorderStroke(
                                    width =
                                        1.dp,

                                    color =
                                        if (isSelected) {
                                            FinderGreen
                                        } else {
                                            FinderBorder
                                        }
                                )
                        ) {

                            Row(
                                modifier =
                                    Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 7.dp
                                    ),

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                RadioButton(
                                    selected =
                                        isSelected,

                                    onClick =
                                        if (isEnabled) {

                                            {
                                                onRequestPointsSelected(
                                                    option.requestPoints
                                                )
                                            }

                                        } else {
                                            null
                                        },

                                    enabled =
                                        isEnabled
                                )


                                Column(
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                ) {

                                    Text(
                                        text =
                                            option.label,

                                        color =
                                            if (isEnabled) {
                                                FinderDark
                                            } else {
                                                FinderMuted
                                            },

                                        fontWeight =
                                            FontWeight.Bold
                                    )


                                    Text(
                                        text =
                                            "依頼 ${option.requestPoints}pt  →  清掃報酬 ${option.rewardPoints}pt",

                                        color =
                                            FinderMuted,

                                        fontSize =
                                            12.sp
                                    )
                                }


                                if (!isEnabled) {

                                    Text(
                                        text =
                                            "ポイント不足",

                                        color =
                                            FinderRed,

                                        fontSize =
                                            11.sp,

                                        fontWeight =
                                            FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }


                if (selectedOption != null) {

                    Surface(
                        modifier =
                            Modifier.fillMaxWidth(),

                        color =
                            Color(
                                0xFFF8FAF9
                            ),

                        shape =
                            RoundedCornerShape(
                                12.dp
                            )
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(
                                    12.dp
                                ),

                            verticalArrangement =
                                Arrangement.spacedBy(
                                    4.dp
                                )
                        ) {

                            CleaningRequestDialogSummaryRow(
                                label =
                                    "使用する依頼ポイント",

                                value =
                                    "${selectedOption.requestPoints} pt"
                            )


                            CleaningRequestDialogSummaryRow(
                                label =
                                    "清掃する人の報酬",

                                value =
                                    "${selectedOption.rewardPoints} pt"
                            )


                            CleaningRequestDialogSummaryRow(
                                label =
                                    "依頼後の残りポイント",

                                value =
                                    "${(currentRequestPoints - selectedOption.requestPoints).coerceAtLeast(0)} pt"
                            )
                        }
                    }

                } else {

                    Text(
                        text =
                            "清掃依頼を出すためのポイントがありません。依頼ポイントは毎日10ptまで回復します。",

                        color =
                            FinderRed,

                        fontSize =
                            12.sp
                    )
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick =
                    onConfirm,

                enabled =
                    canConfirm
            ) {

                Text(
                    text =
                        "この内容で依頼する",

                    color =
                        if (canConfirm) {
                            FinderGreen
                        } else {
                            FinderMuted
                        },

                    fontWeight =
                        FontWeight.Bold
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    text =
                        "キャンセル",

                    color =
                        FinderMuted
                )
            }
        }
    )
}


@Composable
private fun CleaningRequestDialogSummaryRow(
    label: String,
    value: String
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text =
                label,

            modifier =
                Modifier.weight(
                    1f
                ),

            color =
                FinderMuted,

            fontSize =
                12.sp
        )


        Text(
            text =
                value,

            color =
                FinderDark,

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}


@Composable
private fun CleaningStatusNotice(
    message: String,
    backgroundColor: Color,
    textColor: Color
) {

    Text(
        text =
            message,

        color =
            textColor,

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
                    backgroundColor
                )
                .padding(
                    10.dp
                )
    )
}


@Composable
private fun CleaningRequestInfo(
    label: String,
    value: String
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text =
                label,

            color =
                FinderMuted,

            fontSize =
                12.sp
        )


        Text(
            text =
                value,

            color =
                FinderDark,

            fontSize =
                12.sp,

            fontWeight =
                FontWeight.SemiBold
        )
    }
}


@Composable
private fun CleaningActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {

    Button(
        onClick =
            onClick,

        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    50.dp
                ),

        enabled =
            enabled,

        shape =
            RoundedCornerShape(
                14.dp
            ),

        colors =
            ButtonDefaults.buttonColors(
                containerColor =
                    containerColor,

                contentColor =
                    contentColor,

                disabledContainerColor =
                    containerColor.copy(
                        alpha = 0.35f
                    ),

                disabledContentColor =
                    contentColor.copy(
                        alpha = 0.75f
                    )
            )
    ) {

        if (isLoading) {

            CircularProgressIndicator(
                modifier =
                    Modifier.size(
                        20.dp
                    ),

                color =
                    contentColor,

                strokeWidth =
                    2.dp
            )

        } else {

            Icon(
                imageVector =
                    icon,

                contentDescription =
                    null,

                modifier =
                    Modifier.size(
                        20.dp
                    )
            )
        }


        Spacer(
            modifier =
                Modifier.width(
                    8.dp
                )
        )


        Text(
            text =
                text,

            fontWeight =
                FontWeight.Bold
        )
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