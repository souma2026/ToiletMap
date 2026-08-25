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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView


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

    /*
     * 検索対象
     */
    toilets: List<Toilet> =
        emptyList(),

    /*
     * 検索結果を選択
     */
    onSearchToiletSelected:
        (Toilet) -> Unit =
        {},

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
     * トイレ追加用
     * 地図タップ監視
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

            mapView.getMapAsync { map ->

                if (
                    !disposed
                ) {

                    targetMap =
                        map


                    val listener =

                        MapLibreMap
                            .OnMapClickListener { point ->

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
     *
     * 重要：
     * HeaderとMapViewを重ねない
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
         * 検索欄
         *
         * MapViewとは完全に別領域
         * =====================================
         */
        FinderHeader(

            toilets =
                toilets,

            onToiletSelected =
                onSearchToiletSelected,

            modifier =
                Modifier.fillMaxWidth()
        )


        /*
         * =====================================
         * 地図部分
         * =====================================
         */
        Box(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(
                        1f
                    )

        ) {


            /*
             * =====================================
             * MapLibre
             * =====================================
             */
            AndroidView(

                factory = {

                    /*
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


                    /*
                     * MapViewが検索欄のキーボードフォーカスを
                     * 奪わないようにする。
                     *
                     * 地図のタップ・スクロール・ズームには
                     * 影響しない。
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
             * =====================================
             * 場所選択中
             * =====================================
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
}


/*
 * =====================================
 * ヘッダー
 * =====================================
 */
@Composable
private fun FinderHeader(

    toilets:
    List<Toilet>,

    onToiletSelected:
        (Toilet) -> Unit,

    modifier:
    Modifier = Modifier

) {

    var showNotifications by
    remember {

        mutableStateOf(
            false
        )
    }


    /*
     * =====================================
     * 検索文字
     * =====================================
     *
     * Stringで単純に管理する。
     * 日本語入力・Backspaceの処理は
     * OutlinedTextFieldに任せる。
     */
    var searchQuery by
    rememberSaveable {

        mutableStateOf(
            ""
        )
    }


    /*
     * 検索欄にフォーカスがあるか
     */
    var isSearchFocused by
    remember {

        mutableStateOf(
            false
        )
    }


    val searchFocusRequester =
        remember {

            FocusRequester()
        }


    val focusManager =
        LocalFocusManager.current


    /*
     * =====================================
     * 検索処理
     * =====================================
     *
     * 現在Toiletには住所がないので
     *
     * ・トイレ名
     * ・コメント
     *
     * を検索する。
     */
    val searchResults =

        remember(
            searchQuery,
            toilets
        ) {

            val query =
                searchQuery
                    .trim()


            if (
                query.isBlank()
            ) {

                emptyList()

            } else {

                toilets
                    .filter {
                            toilet ->

                        toilet.name.contains(
                            query,
                            ignoreCase = true
                        ) ||

                                toilet.comment.contains(
                                    query,
                                    ignoreCase = true
                                )
                    }
                    .take(
                        10
                    )
            }
        }


    /*
     * 検索結果選択
     */
    fun selectToilet(
        toilet: Toilet
    ) {

        /*
         * 選択したトイレ名を
         * 検索欄へ表示
         */
        searchQuery =
            toilet.name


        /*
         * キーボードを閉じる
         */
        focusManager
            .clearFocus()


        /*
         * 地図側へ通知
         */
        onToiletSelected(
            toilet
        )
    }


    Surface(

        modifier =
            modifier
                .fillMaxWidth(),

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


            /*
             * =====================================
             * タイトル
             * =====================================
             */
            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {


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
                 * =====================================
                 * 通知
                 * =====================================
                 */
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
                                    "新しい通知はありません"
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


            /*
             * =====================================
             * 検索欄
             * =====================================
             */
            OutlinedTextField(

                value =
                    searchQuery,

                onValueChange = { newText ->

                    /*
                     * 入力・Backspaceの結果を
                     * そのまま反映する。
                     */
                    searchQuery =
                        newText
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .focusRequester(
                            searchFocusRequester
                        )
                        .onFocusChanged { focusState ->

                            isSearchFocused =
                                focusState.isFocused
                        },

                enabled =
                    true,

                readOnly =
                    false,

                singleLine =
                    true,

                placeholder = {

                    Text(
                        text =
                            "場所や施設名を検索"
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

                trailingIcon = {

                    if (
                        searchQuery.isNotEmpty()
                    ) {

                        IconButton(

                            onClick = {

                                /*
                                 * ×ボタンで全削除。
                                 */
                                searchQuery =
                                    ""

                                /*
                                 * 削除後もすぐ再入力できるよう
                                 * 検索欄へフォーカスを戻す。
                                 */
                                searchFocusRequester
                                    .requestFocus()
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

                    } else {

                        Icon(

                            imageVector =
                                Icons
                                    .Outlined
                                    .Menu,

                            contentDescription =
                                "検索条件",

                            tint =
                                FinderMuted
                        )
                    }
                },

                shape =
                    RoundedCornerShape(
                        16.dp
                    ),

                keyboardOptions =
                    KeyboardOptions(

                        imeAction =
                            ImeAction.Search
                    ),

                keyboardActions =
                    KeyboardActions(

                        onSearch = {

                            val first =
                                searchResults
                                    .firstOrNull()


                            if (
                                first != null
                            ) {

                                selectToilet(
                                    first
                                )

                            } else {

                                focusManager
                                    .clearFocus()
                            }
                        }
                    ),

                colors =
                    OutlinedTextFieldDefaults
                        .colors(

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
             * 検索結果
             * =====================================
             */
            if (
                isSearchFocused &&
                searchQuery.isNotBlank()
            ) {

                Card(

                    modifier =
                        Modifier
                            .fillMaxWidth(),

                    shape =
                        RoundedCornerShape(
                            16.dp
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


                    if (
                        searchResults.isEmpty()
                    ) {

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
 * 検索候補1件
 * =====================================
 */
@Composable
private fun SearchResultItem(

    toilet:
    Toilet,

    onClick:
        () -> Unit

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


            if (
                toilet.comment
                    .isNotBlank()
            ) {

                Text(

                    text =
                        toilet.comment,

                    color =
                        FinderMuted,

                    fontSize =
                        12.sp,

                    maxLines =
                        1
                )

            } else {

                Text(

                    text =
                        "緯度 %.4f / 経度 %.4f"
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
        }
    }
}


/*
 * =====================================
 * トイレ追加位置選択
 * =====================================
 */
@Composable
private fun LocationSelectionBanner(

    onCancel:
        () -> Unit,

    modifier:
    Modifier = Modifier

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


/*
 * =====================================
 * トイレ詳細
 * =====================================
 */
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
    Modifier = Modifier

) {

    val cleanliness =
        toilet
            .cleanliness
            .coerceIn(
                0,
                5
            )


    val elapsed =
        formatElapsedSinceCleaning(
            toilet.lastCleanedAtMillis
        )


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
            modifier.shadow(

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

        Column(

            modifier =
                Modifier.padding(
                    18.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )

        ) {


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


                    Text(

                        text =
                            "前回の清掃完了：$elapsed",

                        color =
                            FinderMuted,

                        fontSize =
                            12.sp
                    )


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


/*
 * =====================================
 * 前回清掃からの経過時間
 * =====================================
 */
private fun formatElapsedSinceCleaning(

    lastCleanedAtMillis:
    Long?

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
                0L
            )


    val totalMinutes =
        elapsedMillis /
                60_000L


    if (
        totalMinutes < 1L
    ) {

        return "1分未満"
    }


    if (
        totalMinutes < 60L
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