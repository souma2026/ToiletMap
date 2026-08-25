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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.example.toiletmap.model.CleaningStatus
import com.example.toiletmap.model.Toilet
import kotlinx.coroutines.delay
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

// =============================================
// Map screen colors
// =============================================

private val FinderGreen = Color(0xFF0B8377)
private val FinderDark = Color(0xFF12313A)
private val FinderMuted = Color(0xFF748186)
private val FinderPale = Color(0xFFF5F8F7)
private val FinderAmber = Color(0xFFF2B544)
private val FinderRed = Color(0xFFD94B4B)
private val FinderSoftGreen = Color(0xFFE5F4F1)

// =============================================
// Map screen
// =============================================

@Composable
fun MapScreen(
    mapView: MapView,
    searchText: String = "",
    onSearchTextChange: (String) -> Unit = {},
    isSelectingLocation: Boolean = false,
    selectedToilet: Toilet? = null,
    onDismissSelectedToilet: () -> Unit = {},
    onRequestCleaning: (Toilet) -> Unit = {},
    onMarkCleaned: (Toilet) -> Unit = {},
    onLocationSelected: (Double, Double) -> Unit = { _, _ -> },
    onCancelLocationSelection: () -> Unit = {}
) {
    var showNotificationDialog by remember {
        mutableStateOf(false)
    }

    var localSearchText by remember {
        mutableStateOf(searchText)
    }

    LaunchedEffect(searchText) {
        if (searchText != localSearchText) {
            localSearchText = searchText
        }
    }

    // Register a map-tap listener only while selecting a location.
    DisposableEffect(mapView, isSelectingLocation) {
        var disposed = false
        var targetMap: MapLibreMap? = null
        var clickListener: MapLibreMap.OnMapClickListener? = null

        if (isSelectingLocation) {
            mapView.getMapAsync { map ->
                if (!disposed) {
                    targetMap = map

                    val listener = MapLibreMap.OnMapClickListener { point ->
                        onLocationSelected(point.latitude, point.longitude)
                        true
                    }

                    clickListener = listener
                    map.addOnMapClickListener(listener)
                }
            }
        }

        onDispose {
            disposed = true

            val map = targetMap
            val listener = clickListener

            if (map != null && listener != null) {
                map.removeOnMapClickListener(listener)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FinderPale)
    ) {
        AndroidView(
            factory = {
                (mapView.parent as? ViewGroup)?.removeView(mapView)
                mapView
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 154.dp)
        )

        FinderHeader(
            searchText = localSearchText,
            onSearchTextChange = { newValue ->
                localSearchText = newValue
                onSearchTextChange(newValue)
            },
            onNotificationClick = {
                showNotificationDialog = true
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        MapControls(
            onZoomIn = {
                mapView.getMapAsync { map ->
                    map.animateCamera(CameraUpdateFactory.zoomIn())
                }
            },
            onZoomOut = {
                mapView.getMapAsync { map ->
                    map.animateCamera(CameraUpdateFactory.zoomOut())
                }
            },
            onCurrentLocation = {
                // GPS/current-location movement can be connected here later.
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp, bottom = 40.dp)
        )

        if (isSelectingLocation) {
            LocationSelectionBanner(
                onCancel = onCancelLocationSelection,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 166.dp, start = 14.dp, end = 14.dp)
            )
        }

        if (!isSelectingLocation && selectedToilet != null) {
            ToiletDetailCard(
                toilet = selectedToilet,
                onDismiss = onDismissSelectedToilet,
                onRequestCleaning = {
                    onRequestCleaning(selectedToilet)
                },
                onMarkCleaned = {
                    onMarkCleaned(selectedToilet)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
    }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = {
                showNotificationDialog = false
            },
            title = {
                Text(
                    text = "通知",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("現在、新しい通知はありません。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationDialog = false
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

// =============================================
// Header
// =============================================

@Composable
private fun FinderHeader(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(10f),
        color = Color(0xFFF9FBFA),
        shadowElevation = 5.dp
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = FinderGreen,
                            shape = RoundedCornerShape(13.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "WC",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
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
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = "通知",
                            tint = FinderDark,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }

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
// Map controls
// =============================================

@Composable
private fun MapControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onCurrentLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(
                    elevation = 7.dp,
                    shape = RoundedCornerShape(18.dp)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .clickable(onClick = onCurrentLocation),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = "現在地",
                tint = FinderGreen,
                modifier = Modifier.size(25.dp)
            )
        }

        Column(
            modifier = Modifier
                .shadow(
                    elevation = 7.dp,
                    shape = RoundedCornerShape(18.dp)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 50.dp)
                    .clickable(onClick = onZoomIn),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = FinderDark,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Light
                )
            }

            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(1.dp)
                    .background(Color(0xFFE3E9E7))
            )

            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 50.dp)
                    .clickable(onClick = onZoomOut),
                contentAlignment = Alignment.Center
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

// =============================================
// Location selection banner
// =============================================

@Composable
private fun LocationSelectionBanner(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 7.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 8.dp,
                    top = 12.dp,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = FinderSoftGreen,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = FinderGreen
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "トイレの場所を選択",
                    color = FinderDark,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "地図上の登録したい場所を1回タップしてください",
                    color = FinderMuted,
                    fontSize = 12.sp
                )
            }

            TextButton(onClick = onCancel) {
                Text(
                    text = "キャンセル",
                    color = FinderGreen
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
    toilet: Toilet,
    onDismiss: () -> Unit,
    onRequestCleaning: () -> Unit,
    onMarkCleaned: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nowMillis by remember(
        toilet.id,
        toilet.lastCleanedAtMillis
    ) {
        mutableLongStateOf(System.currentTimeMillis())
    }

    LaunchedEffect(
        toilet.id,
        toilet.lastCleanedAtMillis
    ) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(60_000L)
        }
    }

    val cleanliness = toilet.cleanliness.coerceIn(0, 5)
    val elapsed = formatElapsedSinceCleaning(
        lastCleanedAtMillis = toilet.lastCleanedAtMillis,
        nowMillis = nowMillis
    )
    val requested = toilet.cleaningStatus == CleaningStatus.REQUESTED

    val statusText = if (requested) "清掃待ち" else "通常"
    val statusColor = if (requested) FinderAmber else FinderRed
    val actionText = if (requested) "清掃しました" else "清掃を依頼する"
    val actionIcon = if (requested) {
        Icons.Outlined.CheckCircle
    } else {
        Icons.Outlined.NotificationsNone
    }

    Card(
        modifier = modifier
            // Keep the detail view compact so that the map remains visible.
            .heightIn(max = 380.dp)
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD8DEDC))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.13f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "トイレ情報",
                    modifier = Modifier.padding(start = 8.dp),
                    color = FinderMuted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "閉じる",
                        tint = FinderDark
                    )
                }
            }

            Text(
                text = toilet.name,
                color = FinderDark,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = FinderMuted,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "緯度 %.5f / 経度 %.5f".format(
                        toilet.latitude,
                        toilet.longitude
                    ),
                    color = FinderMuted,
                    fontSize = 12.sp
                )
            }

            if (toilet.comment.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF3F5F4),
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Text(
                        text = toilet.comment,
                        modifier = Modifier.padding(
                            horizontal = 11.dp,
                            vertical = 8.dp
                        ),
                        color = FinderDark,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8FAF9),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text(
                        text = "きれいさ",
                        color = FinderMuted,
                        fontSize = 12.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (index < cleanliness) {
                                    FinderGreen
                                } else {
                                    Color(0xFFD7DEDC)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(7.dp))

                        Text(
                            text = "$cleanliness.0",
                            color = FinderDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "前回の清掃完了：$elapsed",
                        color = FinderMuted,
                        fontSize = 12.sp
                    )
                }
            }

            if (requested) {
                Text(
                    text = "このトイレは清掃依頼中です。清掃したら下のボタンを押してください。",
                    color = Color(0xFF7A6200),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF8E6))
                        .padding(10.dp)
                )
            }

            Button(
                onClick = if (requested) {
                    onMarkCleaned
                } else {
                    onRequestCleaning
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (requested) {
                        FinderAmber
                    } else {
                        FinderGreen
                    },
                    contentColor = if (requested) {
                        FinderDark
                    } else {
                        Color.White
                    }
                )
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = actionText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// =============================================
// Elapsed time since last cleaning
// =============================================

private fun formatElapsedSinceCleaning(
    lastCleanedAtMillis: Long?,
    nowMillis: Long
): String {
    if (lastCleanedAtMillis == null) {
        return "記録なし"
    }

    val elapsedMillis = (nowMillis - lastCleanedAtMillis)
        .coerceAtLeast(0L)
    val totalMinutes = elapsedMillis / 60_000L

    if (totalMinutes < 1L) {
        return "1分未満"
    }

    if (totalMinutes < 60L) {
        return "${totalMinutes}分前"
    }

    val totalHours = totalMinutes / 60L
    val remainingMinutes = totalMinutes % 60L

    if (totalHours < 24L) {
        return if (remainingMinutes == 0L) {
            "${totalHours}時間前"
        } else {
            "${totalHours}時間${remainingMinutes}分前"
        }
    }

    val totalDays = totalHours / 24L
    val remainingHours = totalHours % 24L

    return if (remainingHours == 0L) {
        "${totalDays}日前"
    } else {
        "${totalDays}日${remainingHours}時間前"
    }
}

